package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import messages.order.Side;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class StretchAlgoBackTest extends AbstractAlgoBackTest {

    @Override
    public AlgoLogic createAlgoLogic() {
        return new StretchAlgoLogic();
    }

    @Test
    public void testOverallProfitabilityInBacktest() throws Exception {
        send(createTickLowBuyOpportunity());
        send(createTickMedium());
        send(createTickHighSellOpportunity());
        send(createTickStableHigh());

        long totalProfit = calculateTotalProfit(container.getState().getChildOrders());
        System.out.println("Calculated Total Profit: " + totalProfit);
        assertTrue("Total profit should be positive", totalProfit > 0);
    }

    private long calculateTotalProfit(List<ChildOrder> childOrders) {
        long totalProfit = 0;

        System.out.println("Processing child orders for profit calculation...");
        for (ChildOrder sellOrder : childOrders) {
            if (sellOrder.getSide() == Side.SELL) {
                ChildOrder matchingBuyOrder = null;
                for (ChildOrder buyOrder : childOrders) {
                    if (buyOrder.getSide() == Side.BUY && buyOrder.getPrice() < sellOrder.getPrice()) {
                        if (matchingBuyOrder == null || buyOrder.getPrice() < matchingBuyOrder.getPrice()) {
                            matchingBuyOrder = buyOrder;
                        }
                    }
                }

                if (matchingBuyOrder != null) {
                    long profit = sellOrder.getPrice() - matchingBuyOrder.getPrice();
                    System.out.println("Matched Sell Order: " + sellOrder.getPrice() +
                            " with Buy Order: " + matchingBuyOrder.getPrice() +
                            " for profit: " + profit);
                    totalProfit += profit;
                } else {
                    System.out.println("No matching buy order found for Sell Order: " + sellOrder.getPrice());
                }
            }
        }

        System.out.println("Total Calculated Profit: " + totalProfit);
        return totalProfit;
    }

    @Test
    public void testOrderLimitAdherenceInBacktest() throws Exception {
        for (int i = 0; i < 10; i++) {
            send(createTick2());
        }
        int activeOrdersCount = container.getState().getActiveChildOrders().size();
        assertTrue("Active orders should not exceed limit of 6", activeOrdersCount <= 6);
    }

    @Test
    public void testOrderCancellationsOnAdverseMovementsInBacktest() throws Exception {
        send(createTick2());
        var buyOrder = container.getState().getChildOrders().get(0);
        send(createTickLowLiquidity());
        assertTrue(container.getState().getCancelledChildOrders().contains(buyOrder));
    }
}

