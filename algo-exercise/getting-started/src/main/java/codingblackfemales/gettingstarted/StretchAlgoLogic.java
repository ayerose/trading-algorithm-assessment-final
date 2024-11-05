package codingblackfemales.gettingstarted;
import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class StretchAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(StretchAlgoLogic.class);
    private static final int MAX_ORDERS = 5;
    private static final int ORDER_QUANTITY = 100;
    private static final double BUY_THRESHOLD_FACTOR = 0.998; // slightly below vwap for aggressive buying
    private static final double SELL_THRESHOLD_FACTOR = 1.005;


    private double vwap = 0.0;
    private boolean vwapInitialized = false;
    private List<Double> recentBidAverages = new ArrayList<>();
    private List<Double> recentAskAverages = new ArrayList<>();
    private static final int TREND_PERIOD = 6;
    protected static final double CANCEL_THRESHOLD_PERCENTAGE = 0.05;
    protected static final int PRICE_THRESHOLD = 5;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_ORANGE = "\u001B[38;5;214m";
    public static final String ANSI_PINK = "\u001B[38;5;213m";

    @Override
    public Action evaluate(SimpleAlgoState state) {
        if (state == null) {
            logger.warn(ANSI_YELLOW + "[MYSTRETCHALGO] State is null. No action taken." + ANSI_RESET);
            return NoAction.NoAction;
        }

        // log initial vwap setup
        if (!vwapInitialized) {
            initializeVWAP(state);
            vwapInitialized = true;
            logger.info(ANSI_PINK + "[MYSTRETCHALGO] Initial VWAP set to " + vwap + ANSI_RESET);

        } else {
            updateVWAP(state);
        }

        // Log order book state
        var orderBookAsString = state.toString();
        logger.info(ANSI_CYAN + "[MYSTRETCHALGO] Order book state:\n" + orderBookAsString + ANSI_RESET);

        // Get bid and ask levels
        BidLevel bestBid = state.getBidAt(0);
        AskLevel bestAsk = state.getAskAt(0);
        if (bestBid == null || bestAsk == null) {
            logger.warn(ANSI_YELLOW + "[AMYSTRETCHALGO] Best bid or ask is null. No action taken." + ANSI_RESET);
            return NoAction.NoAction;
        }

        long bidPrice = bestBid.price;
        long askPrice = bestAsk.price;

        // calc adaptive buy and sell thresholds
        double buyThreshold = vwap * BUY_THRESHOLD_FACTOR;
        double sellThreshold = vwap * SELL_THRESHOLD_FACTOR;

        // calc recent trends
        updateTrends(bidPrice, askPrice);

        logger.info(ANSI_PURPLE+"[MYSTRETCHALGO] VWAP: {}, Buy Threshold: {}, Sell Threshold: {}"+ ANSI_RESET, vwap, buyThreshold, sellThreshold);

        // get active orders only once at the start
        List<ChildOrder> activeOrders = state.getActiveChildOrders();

        //  logic: Buy if ask price is below the threshold, sell if bid price is above threshold
        if (shouldBuy(askPrice, activeOrders.size())) {
            logger.info(ANSI_GREEN + "[MYSTRETCHALGO] Buy condition met. Placing buy order."+ ANSI_RESET);
            return new CreateChildOrder(Side.BUY, ORDER_QUANTITY, askPrice);
        }

        if (shouldSell(bidPrice, activeOrders.size())) {
            logger.info(ANSI_GREEN +"[AMYSTRETCHALGO] Sell condition met. Placing sell order."+ ANSI_RESET);
            return new CreateChildOrder(Side.SELL, ORDER_QUANTITY, bidPrice);
        }
// ****CANCEL LOGIC*****
        if (!activeOrders.isEmpty()) {
            for (ChildOrder order : activeOrders) {
                double cancelThreshold = vwap * CANCEL_THRESHOLD_PERCENTAGE;
                long orderPrice = order.getPrice();

                // calc cancellation conditions
                boolean cancelDueToVWAPDeviation = Math.abs(orderPrice - askPrice) > cancelThreshold;
                boolean cancelDueToPriceThreshold = Math.abs(bidPrice - orderPrice) > PRICE_THRESHOLD;

                // log debugging messages
                logger.info(ANSI_RED + "[MYSTRETCHALGO] Checking cancellation for order with price: " + orderPrice + ANSI_RESET);
                logger.info(ANSI_RED + "[MYSTRETCHALGO] VWAP: " + vwap + ", Ask Price: " + askPrice + ", Bid Price: " + bidPrice + ANSI_RESET);
                logger.info(ANSI_RED + "[MYSTRETCHALGO] Cancellation Threshold (VWAP Deviation): " + cancelThreshold +
                        ", PRICE_THRESHOLD: " + PRICE_THRESHOLD + ANSI_RESET);
                logger.info(ANSI_RED + "[MYSTRETCHALGO] Condition Check - Cancel Due to VWAP Deviation: " + cancelDueToVWAPDeviation +
                        ", Cancel Due to Price Threshold: " + cancelDueToPriceThreshold + ANSI_RESET);
                if (cancelDueToVWAPDeviation || cancelDueToPriceThreshold) {
                    logger.info(ANSI_RED + "[MYSTRETCHALGO] Canceling order with price: " + orderPrice + " due to threshold deviation." + ANSI_RESET);
                    return new CancelChildOrder(order);
                }
            }
        }

        logger.info(ANSI_BLUE +"[MYSTRETCHALGO] No action taken."+ ANSI_RESET);
        return NoAction.NoAction;

    }
    public double getVWAP() {
        return vwap;
    }

    private void initializeVWAP(SimpleAlgoState state) {
        BidLevel bestBid = state.getBidAt(0);
        AskLevel bestAsk = state.getAskAt(0);
        if (bestBid != null && bestAsk != null) {
            vwap = (bestBid.price + bestAsk.price) / 2.0;
            logger.info("[MYSTRETCHALGO] Initial VWAP set to {}", vwap);
        }
    }

    private void updateVWAP(SimpleAlgoState state) {
        int bidLevels = state.getBidLevels();
        int askLevels = state.getAskLevels();
        long totalQuantity = 0;
        long totalPriceQuantity = 0;

        for (int i = 0; i < Math.max(bidLevels, askLevels); i++) {
            BidLevel bid = state.getBidAt(i);
            AskLevel ask = state.getAskAt(i);
            if (bid != null) {
                totalQuantity += bid.quantity;
                totalPriceQuantity += bid.price * bid.quantity;
            }
            if (ask != null) {
                totalQuantity += ask.quantity;
                totalPriceQuantity += ask.price * ask.quantity;
            }
        }

        if (totalQuantity > 0) {
            vwap = (double) totalPriceQuantity / totalQuantity;
            logger.info(ANSI_YELLOW+"[MYSTRETCHALGO] VWAP updated to {}"+ANSI_RESET, vwap);
        } else {
            logger.warn(ANSI_YELLOW+"[MYSTRETCHALGO] No sufficient data to update VWAP."+ANSI_RESET);
        }
    }

    private void updateTrends(long bidPrice, long askPrice) {
        if (recentBidAverages.size() >= TREND_PERIOD) {
            recentBidAverages.remove(0);
            recentAskAverages.remove(0);
        }
        recentBidAverages.add((double) bidPrice);
        recentAskAverages.add((double) askPrice);

        double bidTrend = calculateTrend(recentBidAverages);
        double askTrend = calculateTrend(recentAskAverages);

        logger.info(ANSI_PURPLE+"[MYSTRETCHALGO] Bid trend: {}, Ask trend: {}"+ANSI_RESET, bidTrend, askTrend);
    }

    private double calculateTrend(List<Double> prices) {
        double sum = 0;
        for (int i = 1; i < prices.size(); i++) {
            sum += prices.get(i) - prices.get(i - 1);
        }
        return sum;
    }

    boolean shouldBuy(double askPrice, int activeOrdersCount) {
        // check if there's already an active buy order
        if (activeOrdersCount >= 1) {
            logger.info(ANSI_ORANGE +"[MYSTRETCHALGO] Maximum buy orders reached. Skipping additional buy order."+ ANSI_RESET);
            return false; // stop more buy orders
        }

        double bidTrend = calculateTrend(recentBidAverages);
        boolean aggressiveBuyCondition = bidTrend > 1.0 && activeOrdersCount < MAX_ORDERS + 2; // allow more buys in bullish trends

        boolean shouldBuy = activeOrdersCount < MAX_ORDERS && askPrice < vwap * BUY_THRESHOLD_FACTOR;
        shouldBuy = shouldBuy || aggressiveBuyCondition; // include trend-based buying

        logger.info("[MYSTRETCHALGO] Should Buy: " + shouldBuy + " | askPrice: " + askPrice
                + " | VWAP * BuyThresholdFactor: " + (vwap * BUY_THRESHOLD_FACTOR)
                + " | Active Orders Count: " + activeOrdersCount);
        return shouldBuy;
    }

    boolean shouldSell(double bidPrice, int activeOrdersCount) {
        double askTrend = calculateTrend(recentAskAverages);
        boolean aggressiveSellCondition = askTrend > 1.0 && activeOrdersCount < MAX_ORDERS + 2; // allow more sells in strong ask trends

        return (activeOrdersCount < MAX_ORDERS && bidPrice > vwap * SELL_THRESHOLD_FACTOR) || aggressiveSellCondition;
    }
}