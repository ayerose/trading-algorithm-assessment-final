package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import messages.order.Side;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StretchAlgoTest extends AbstractAlgoTest {

    @Override
    public AlgoLogic createAlgoLogic() {
        return new StretchAlgoLogic();
    }

/*
    @Test
    public void testProfitability() throws Exception {
        send(createTick2());
        // confirm a buy order was created
        var buyOrders = container.getState().getChildOrders();
        assertEquals(1, buyOrders.size());
        assertTrue(buyOrders.get(0).getSide() == Side.BUY);

        // price increase to trigger a sell order
        send(createTick3());

        // confirm a sell order was created
        var sellOrders = container.getState().getChildOrders();
        assertTrue(sellOrders.stream().anyMatch(order -> order.getSide() == Side.SELL));

        // verify the last sell price is greater than the initial buy price
        long buyPrice = buyOrders.get(0).getPrice();
        long sellPrice = sellOrders.stream()
                .filter(order -> order.getSide() == Side.SELL)
                .findFirst()
                .get()
                .getPrice();
        assertTrue("Sell price should be higher than buy price", sellPrice > buyPrice);
    }
*/

    @Test
    public void testNoActionOnWideSpread() throws Exception {
        send(createTickWithWideSpread());
        assertEquals(0, container.getState().getChildOrders().size());
    }

    @Test
    public void testOrderCancellationOnUnfavorablePriceMove() throws Exception {
        send(createTick2());
        var buyOrder = container.getState().getChildOrders().get(0);
        send(createTickLowLiquidity());

        assertTrue(container.getState().getCancelledChildOrders().contains(buyOrder));
    }

    @Test
    public void testOrderLimitEnforcement() throws Exception {
        for (int i = 0; i < 10; i++) {
            send(createTick2());
        }
        assertTrue(container.getState().getActiveChildOrders().size() <= 6);
    }
}

