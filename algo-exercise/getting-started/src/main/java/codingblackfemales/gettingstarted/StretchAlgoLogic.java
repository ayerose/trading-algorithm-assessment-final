package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class StretchAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(StretchAlgoLogic.class);

    private static final double BASE_PROFIT_MARGIN = 0.005;
    private static final double VOLATILITY_MULTIPLIER = 2;
    private static final long SPREAD_THRESHOLD = 5;
    private static final long PRICE_THRESHOLD = 3; // price movement threshold for canceling orders

    private long currentTargetBuyPrice = 100;
    private long currentTargetSellPrice = 102;
    private boolean recentlyCancelledBuyOrder = false;


    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);
        if (orderBookAsString != null) {
            logger.info("[MYPROFITALGO] Order book state:\n" + orderBookAsString);
        } else {
            logger.warn("[MYPROFITALGO] Order book data is missing or incomplete.");
        }

        logger.info("[MYPROFITALGO] Current Target Buy Price: £" + currentTargetBuyPrice);
        logger.info("[MYPROFITALGO] Current Target Sell Price: £" + currentTargetSellPrice);


        // exit if too many child orders exist
        if (state.getChildOrders().size() > 20) {
            return NoAction.NoAction;
        }

        // safely fetch best ask and bid prices
        final AskLevel bestAskLevel = state.getAskAt(0);
        final BidLevel bestBidLevel = state.getBidAt(0);
        if (bestAskLevel == null || bestBidLevel == null) {
            logger.warn("[MYPROFITALGO] No action taken due to missing ask or bid level.");
            return NoAction.NoAction;
        }

        long bestAskPrice = bestAskLevel.price;
        long bestBidPrice = bestBidLevel.price;

        // Check the spread
        long spread = bestAskPrice - bestBidPrice;
        if (spread > SPREAD_THRESHOLD) {
            logger.info("[MYPROFITALGO] Spread too wide: £" + spread + ". No action taken.");
            return NoAction.NoAction;
        }

        // adjust buy and sell targets based on current prices and volatility
        double volatilityFactor = calculateVolatilityFactor(state);
        updateTargetPrices(bestAskPrice, bestBidPrice, volatilityFactor);

        // and retrieve active orders
        List<ChildOrder> activeOrders = state.getActiveChildOrders();
        logger.info("[MYPROFITALGO] Active Orders: " + activeOrders);

        //  Check for existing buy orders
        boolean hasExistingBuyOrderAtAskPrice = activeOrders.stream()
                .anyMatch(order -> order.getSide() == Side.BUY && order.getPrice() == bestAskPrice);


        if (!recentlyCancelledBuyOrder && !hasExistingBuyOrderAtAskPrice && shouldBuy(bestAskPrice, activeOrders)) {
            recentlyCancelledBuyOrder = false;
            logger.info("[MYPROFITALGO] Placing buy order at price: £" + bestAskPrice);
            return new CreateChildOrder(Side.BUY, 100, bestAskPrice); // assuming quantity of 100
        }

        if (shouldSell(bestBidPrice, activeOrders)) {
            logger.info("[MYPROFITALGO] Placing sell order at price: £" + bestBidPrice);
            return new CreateChildOrder(Side.SELL, 100, bestBidPrice); // assuming quantity of 100
        }

        // cancel orders if the market has moved significantly
        for (var activeOrder : activeOrders) {
            if (Math.abs(bestBidPrice - activeOrder.getPrice()) > PRICE_THRESHOLD) {
                logger.info("[MYPROFITALGO] Cancelling order due to price move: £" + activeOrder.getPrice());
                recentlyCancelledBuyOrder = activeOrder.getSide() == Side.BUY;
                return new CancelChildOrder(activeOrder);
            }
        }


        return NoAction.NoAction;
    }



    private void updateTargetPrices(long bestAskPrice, long bestBidPrice, double volatilityFactor) {

        if (bestAskPrice < currentTargetBuyPrice) {
            currentTargetBuyPrice = bestAskPrice;
        }
        // calculate a dynamic sell target using the volatility-adjusted margin
        double dynamicMargin = BASE_PROFIT_MARGIN * (1 + volatilityFactor * VOLATILITY_MULTIPLIER);
        currentTargetSellPrice = Math.round(bestBidPrice * (1 + dynamicMargin));
    }

    private boolean shouldBuy(long bestAskPrice, List<ChildOrder> activeOrders) {
        // buy if ask price is below target and theres room for more active orders
        return bestAskPrice <= currentTargetBuyPrice && activeOrders.size() < 6;
    }

    private boolean shouldSell(long bestBidPrice, List<ChildOrder> activeOrders) {
        // sell if bid price meets target and we have active orders to sell
        return bestBidPrice >= currentTargetSellPrice && !activeOrders.isEmpty();
    }

    private double calculateVolatilityFactor(SimpleAlgoState state) {
        // placeholder for volatility calculation - returning a dummy factor for now
        return 1.0;
    }
}

