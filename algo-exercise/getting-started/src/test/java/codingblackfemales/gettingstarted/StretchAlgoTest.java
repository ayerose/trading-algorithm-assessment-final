package codingblackfemales.gettingstarted;
import codingblackfemales.algo.AlgoLogic;
import messages.order.Side;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
/**
 * StretchAlgoTest verifies the behavior of the StretchAlgoLogic class,
 * focusing on profitability, order management, spread conditions and trend-based trading logic.
 */
public class StretchAlgoTest extends AbstractAlgoTest {
    // Threshold for cancelling orders based on unfavorable market conditions
    private static final double CANCEL_THRESHOLD_PERCENTAGE = 0.05;;
    private StretchAlgoLogic stretchAlgoLogic;

    @Override
    public AlgoLogic createAlgoLogic() {
        return new StretchAlgoLogic();
    }

    @Before
    public void setUp() {
        // initialize an instance of StretchAlgoLogic before each test for isolated testing
        stretchAlgoLogic = new StretchAlgoLogic();
    }
    /**
     * Tests if the algorithm creates profitable orders by verifying that
     * the sell price is higher than the buy price after a price increase
     */

    @Test
    public void testProfitability() throws Exception {
        send(createTick2());
        // verify that buy order was created initially
        var buyOrders = container.getState().getChildOrders();
        assertEquals("Expected one buy order after first tick", 1, buyOrders.size());
        assertTrue("Order should be a BUY order", buyOrders.get(0).getSide() == Side.BUY);

        send(createTick3());
        var sellOrders = container.getState().getChildOrders();
        // check that a sell order exists and that it is profitable
        assertTrue("Expected a sell order after price increase",
                sellOrders.stream().anyMatch(order -> order.getSide() == Side.SELL));

        // ensure profitability by checking that sell price > buy price
        long buyPrice = buyOrders.get(0).getPrice();
        var sellOrderOptional = sellOrders.stream()
                .filter(order -> order.getSide() == Side.SELL)
                .findFirst();
        // verify profitability by comparing buy and sell prices
        assertTrue("Sell order should exist", sellOrderOptional.isPresent());
        long sellPrice = sellOrderOptional.get().getPrice();
        assertTrue("Sell price should be higher than buy price for profitability", sellPrice > buyPrice);
    }
    /**
     * ensures that no orders are created when the spread is too wide,
     * as conditions like this are not favorable for trading
     */
    @Test
    public void testNoActionOnWideSpread() throws Exception {
        send(createTickWithWideSpread());
        assertEquals(0, container.getState().getChildOrders().size());
    }
    /**
     * Tests if a buy order is canceled when the price moves unfavorably
     * (due to low liquidity for example)
     */
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
    /**
     * tests if the algorithm correctly places a buy order in a bullish trend,
     * when the ask price is slightly below the vwap
     */
    @Test
    public void testTrendBasedBuying() throws Exception {
        send(createTickWithBullishTrend()); // simulate bullish trend
        stretchAlgoLogic.evaluate(container.getState()); // update VWAP and internal state
        double askPrice = stretchAlgoLogic.getVWAP() * 0.99; // slightly below vwap threshold for buying
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