package codingblackfemales.gettingstarted;
import codingblackfemales.algo.AlgoLogic;
import messages.order.Side;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StretchAlgoTest extends AbstractAlgoTest {
    private static final double CANCEL_THRESHOLD_PERCENTAGE = 0.05;;
    private StretchAlgoLogic stretchAlgoLogic;

    @Override
    public AlgoLogic createAlgoLogic() {
        return new StretchAlgoLogic();
    }

    @Before
    public void setUp() {
        // instantiate StretchAlgoLogic before each test
        stretchAlgoLogic = new StretchAlgoLogic();
    }

    @Test
    public void testProfitability() throws Exception {
        send(createTick2());
        // verify that buy order was created
        var buyOrders = container.getState().getChildOrders();
        assertEquals("Expected one buy order after first tick", 1, buyOrders.size());
        assertTrue("Order should be a BUY order", buyOrders.get(0).getSide() == Side.BUY);

        send(createTick3());
        // verify that sell order was created
        var sellOrders = container.getState().getChildOrders();
        assertTrue("Expected a sell order after price increase",
                sellOrders.stream().anyMatch(order -> order.getSide() == Side.SELL));

        // ensure profitability by checking that sell price > buy price
        long buyPrice = buyOrders.get(0).getPrice();
        var sellOrderOptional = sellOrders.stream()
                .filter(order -> order.getSide() == Side.SELL)
                .findFirst();
        // check if a sell order exists and then compare prices
        assertTrue("Sell order should exist", sellOrderOptional.isPresent());
        long sellPrice = sellOrderOptional.get().getPrice();
        assertTrue("Sell price should be higher than buy price for profitability", sellPrice > buyPrice);
    }

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

    @Test
    public void testTrendBasedBuying() throws Exception {
        send(createTickWithBullishTrend());
        stretchAlgoLogic.evaluate(container.getState()); // trigger evaluation to update internal state
        double askPrice = stretchAlgoLogic.getVWAP() * 0.99; // slightly below vwap
        boolean shouldBuy = stretchAlgoLogic.shouldBuy((long) askPrice, 0);

        assertTrue("Algo should place a buy order in a bullish trend", shouldBuy);
    }

    @Test
    public void testTrendBasedSelling() throws Exception  {
        send(createTickWithBearishTrend());
        stretchAlgoLogic.evaluate(container.getState()); // trigger evaluation to update internal state

        double bidPrice = stretchAlgoLogic.getVWAP() * 1.01; // slightly above vwap
        boolean shouldSell = stretchAlgoLogic.shouldSell((long) bidPrice, 1);

        assertTrue("Algo should place a sell order in a bearish trend", shouldSell);
    }

    @Test
    public void testBuyCondition() throws Exception  {
        send(createTick2());
        stretchAlgoLogic.evaluate(container.getState()); // trigger vwap initialization
        double askPrice = 90; // below vwap * BUY_THRESHOLD_FACTOR
        boolean shouldBuy = stretchAlgoLogic.shouldBuy((long) askPrice, 0);
        assertTrue("Buy order should be placed under favorable conditions", shouldBuy);
    }

    @Test
    public void testVWAPInitialization() throws Exception {
        send(createTick2());
        stretchAlgoLogic.evaluate(container.getState()); // trigger vwap initialization
        assertTrue("VWAP should be initialized", stretchAlgoLogic.getVWAP() > 0);
    }
}