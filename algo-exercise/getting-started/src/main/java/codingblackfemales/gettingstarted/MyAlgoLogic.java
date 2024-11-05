package codingblackfemales.gettingstarted;
import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * MyAlgoLogic implements a simple trading algorithm that creates and cancels child orders.
 *
 * Main Features:
 * - Creates buy orders if the best ask price is within the defined target and spread.
 * - Cancels active orders if the market moves unfavorably based on set thresholds.
 * - Ensures there is sufficient liquidity before making trading decisions.
 *
 * Key Parameters:
 * - SPREAD_THRESHOLD: Maximum acceptable spread between bid and ask prices.
 * - PRICE_THRESHOLD: Movement threshold for canceling active orders.
 */

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    // initial target price for creating new orders
    private long currentTargetPrice = 100;
    // thresholds for spread and price movement - affecting buy, sell, and cancel decisions
    private static final long SPREAD_THRESHOLD = 5;
    private static final long PRICE_THRESHOLD = 3;
    // ANSI colors for log messages
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_ORANGE = "\u001B[38;5;214m";
    public static final String ANSI_PINK = "\u001B[38;5;213m";
    public static final String BRIGHT_CYAN = "\u001B[96m";

    @Override
    public Action evaluate(SimpleAlgoState state) {
        // safely handle the order book state logging
        var orderBookAsString = Util.orderBookToString(state);
        if (orderBookAsString != null) {
            logger.info(ANSI_BLUE+"[MYALGO] The state of the order book is:\n"+ANSI_RESET + orderBookAsString);
        } else {
            logger.warn(ANSI_RED+"[MYALGO] Order book data is missing or incomplete."+ANSI_RESET);
        }
        // prevent excessive orders by limiting the total child orders to 20
        if (state.getChildOrders().size() > 20) {
            return NoAction.NoAction;
        }
        // fetch best ask and bid prices, ensuring they are non-null values
        final AskLevel bestAskLevel = state.getAskAt(0);
        final long bestBidPrice = state.getBidAt(0) != null ? state.getBidAt(0).price : 0;
        if (bestAskLevel == null || bestBidPrice == 0) {
            logger.warn(ANSI_ORANGE+"[MYALGO] Best ask or bid price is null, no action will be taken."+ANSI_RESET);
            return NoAction.NoAction;
        }
        long bestAskPrice = bestAskLevel.price;
        logger.info(ANSI_PINK+"[MYALGO] The best ask price is: £"+ANSI_RESET + bestAskPrice);
        logger.info(ANSI_PINK+"[MYALGO] The best bid price is: £"+ANSI_RESET + bestBidPrice);

        // Check if there are fewer than 3 bids or asks - indicating low liquidity
        if (state.getAskAt(2) == null || state.getBidAt(2) == null) {
            logger.info(ANSI_CYAN+"[MYALGO] Low liquidity detected (fewer than 3 bids or asks), therefore no action will be taken."+ANSI_RESET);
            return NoAction.NoAction;
        }
        // calculate spread and check if it's within the acceptable range
        long spread = bestAskPrice - bestBidPrice;
        if (spread > SPREAD_THRESHOLD) {
            logger.info(ANSI_CYAN+"[MYALGO] Spread is too wide: £" + spread + ". No action taken."+ANSI_RESET);
            return NoAction.NoAction;
        }
        // get active orders
        var activeOrders = state.getActiveChildOrders();
        logger.info(ANSI_BLUE+"[MYALGO] Active child orders: "+ANSI_RESET + activeOrders.size());

        // if less than 6 active orders exist, create more orders if the best ask price differs from the current target price
        if (activeOrders.size() < 6 && bestAskPrice != currentTargetPrice) {
            currentTargetPrice = bestAskPrice; // Update current target price to best ask price
            logger.info(BRIGHT_CYAN+"[MYALGO] Creating new order @ new price: £"+ANSI_RESET + bestAskPrice);
            return new CreateChildOrder(Side.BUY, 80, bestAskPrice);
        }
        // iterate through active orders to check if they need to be canceled due to price movement
        if (!activeOrders.isEmpty()) {
            for (var activeOrder : activeOrders) {
                long orderPrice = activeOrder.getPrice();

                // cancel the order if the price has moved by more than the PRICE_THRESHOLD
                if (Math.abs(bestBidPrice - orderPrice) > PRICE_THRESHOLD) {
                    logger.info(ANSI_RED+"[MYALGO] Canceling order with price: £" + orderPrice + " because the price moved by more than the threshold."+ANSI_RESET);
                    return new CancelChildOrder(activeOrder);
                }
            }
        }
        return NoAction.NoAction;
    }
}