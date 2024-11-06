
package codingblackfemales.gettingstarted;
import codingblackfemales.action.Action;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import messages.order.Side;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * StretchAlgoBackTest contains  tests for verifying the behavior of the StretchAlgoLogic
 * under various market conditions, such as stable, bullish & and bearish trends, as well as tests
 * for VWAP adjustments and order cancellation logic in volatile markets
 */
public class StretchAlgoBackTest extends AbstractAlgoBackTest {

    @Override
    public AlgoLogic createAlgoLogic() {
        return new StretchAlgoLogic();
    }

    @Mock
    private SimpleAlgoState mockState;

    @Before
    public void setUp() {
        // initialize mocks and set up StretchAlgoLogic instance for testing
        MockitoAnnotations.openMocks(this);
        stretchAlgoLogic = new StretchAlgoLogic();
    }
    // ansi colors for enhancing console output readability in tests
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";


    // helper method to replay a market data sequence to simulate specific market conditions
    private void replayMarketDataSequence(List<UnsafeBuffer> sequence) throws Exception {
        for (UnsafeBuffer tick : sequence) {
            send(tick);
        }
    }
    //  log current algorithm state
    private void logCurrentState(String message) {
        System.out.println(ANSI_RED + "[STATE] " + message + ANSI_RESET);
    }

    @Test
    public void testStableMarketBehavior() throws Exception {
        for (int i = 0; i < 10; i++) {
            send(createTickMedium());
            verifyNoSignificantActions();
        }
    }
    /**
     * Verifies algorithm response to a bullish market with increasing prices.
     * + ensures that buy actions happen at lower price points and sell actions are taken at higher prices
     */
    @Test
    public void testBullishMarketResponse() throws Exception {
        for (int i = 0; i < 5; i++) {
            send(createBullishTickLow());
            logCurrentState("Testing buy condition at low price point");
        }
        for (int i = 0; i < 5; i++) {
            send(createBullishTickMid());
            logCurrentState("Testing response to upward trend at mid-level prices");
        }
        for (int i = 0; i < 5; i++) {
            send(createBullishTickHigh());
            logCurrentState("Testing sell condition at high price point");
        }
        for (int i = 0; i < 5; i++) {
            send(createBullishTickStableHigh());
            logCurrentState("Testing sustained high prices to avoid buying");
        }
        verifyExpectedProfitableActions();
    }

    @Test
    public void testBearishMarketResponse() throws Exception {
        send(createTickHigh());
        send(createTickMedium());
        send(createTickLow());
        var state = container.getState();
        assertEquals("Expected buy orders in bearish market.", 1, state.getChildOrders().size());
        assertEquals("No sell orders expected in bearish market without profit opportunities", 0, state.getCancelledChildOrders().size());
    }

    @Test
    public void testVWAPAdjustmentsUnderFluctuatingMarket() throws Exception {
        send(createTickLowBuyOpportunity());
        send(createTickWithTightSpread());
        send(createTickWithWideSpread());

        StretchAlgoLogic algo = (StretchAlgoLogic) container.getAlgoLogic();
        double initialVWAP = algo.getVWAP();
        send(createTickStableHigh());

        double updatedVWAP = algo.getVWAP();
        assertTrue("VWAP should increase after high price ticks.", updatedVWAP > initialVWAP);
    }

    @Test
    public void testTrendBasedBuyingAndSelling() throws Exception {
        BidLevel bidLevel = new BidLevel();
        bidLevel.setPrice(85);
        bidLevel.setQuantity(150);

        AskLevel askLevel = new AskLevel();
        askLevel.setPrice(80);
        askLevel.setQuantity(100);

        when(mockState.getBidAt(0)).thenReturn(bidLevel);
        when(mockState.getAskAt(0)).thenReturn(askLevel);

        // initialize vwap  based on  mock data
        stretchAlgoLogic.evaluate(mockState);

        // assert that initial buy order is created if conditions are met
        Action buyAction = stretchAlgoLogic.evaluate(mockState);
        assertTrue("Expected a buy order due to favorable ask price", buyAction instanceof CreateChildOrder);
        // udate bid and ask levels to simulate a selling condition
        bidLevel.setPrice(110);
        askLevel.setPrice(105);

        // evaluate new conditions for selling
        Action sellAction = stretchAlgoLogic.evaluate(mockState);
        assertTrue("Expected a sell order due to favorable bid price", sellAction instanceof CreateChildOrder);
    }
    private void verifyNoSignificantActions() {
        var state = container.getState();
        assertTrue("No orders should be created in a stable market.", state.getChildOrders().isEmpty());
    }

    private void verifyExpectedProfitableActions() {
        var state = container.getState();
        long buyOrders = state.getChildOrders().stream().filter(order -> order.getSide() == Side.BUY).count();
        long sellOrders = state.getChildOrders().stream().filter(order -> order.getSide() == Side.SELL).count();
        assertTrue("Expected buy orders in bullish market for profitable sell setup.", buyOrders > 0);
        assertTrue("Expected sell orders after price increase in bullish market.", sellOrders > 0);
    }

    @Test
    public void testCancellationInVolatileMarket() throws Exception {
        List<UnsafeBuffer> volatileMarketSequence = MarketDataSimulator.getVolatileMarketSequence();
        replayMarketDataSequence(volatileMarketSequence);
        var state = container.getState();
        long canceledOrders = state.getCancelledChildOrders().stream().count();

        assertTrue("Orders should be canceled due to rapid price changes in a volatile market", canceledOrders > 0);
    }

    @Test
    public void testCancellationInBearishMarket() throws Exception {
        // force an initial buy order to ensure there's an active order to evaluate
        send(createTickLowBuyOpportunity());
        List<UnsafeBuffer> bearishMarketSequence = MarketDataSimulator.getBearishMarketSequence();
        replayMarketDataSequence(bearishMarketSequence);

        var state = container.getState();
        long canceledOrders = state.getCancelledChildOrders().stream().count();
        final Logger logger = LoggerFactory.getLogger(StretchAlgoBackTest.class);

        logger.info(ANSI_YELLOW+"[BEARISHTEST] Bearish market test - expected cancellations due to price thresholds."+ANSI_RESET);
        state.getCancelledChildOrders().forEach(order ->
                logger.info(ANSI_YELLOW+"[BEARISHTEST] Canceled Order Price: " + order.getPrice() + ", VWAP: "+ANSI_RESET + stretchAlgoLogic.getVWAP())
        );
        assertTrue("Orders should be canceled as prices move below thresholds in a bearish market", canceledOrders > 0);
    }

    /*@Test
    public void testCancellationInBullishMarket() throws Exception {
        List<UnsafeBuffer> bullishMarketSequence = MarketDataSimulator.getBullishMarketSequence();
        replayMarketDataSequence(bullishMarketSequence);

        var state = container.getState();
        long canceledOrders = state.getCancelledChildOrders().stream().count();
        assertTrue("Orders should be canceled as prices move above thresholds in a bullish market", canceledOrders > 0);
    }

    *//**
     * tests order cancellation when price movement exceeds threshold.
     * uses retry logic to allow enough time for state updates
     *//*
    @Test
    public void testOrderCancellationOnPriceMovementBeyondThreshold() throws Exception {
        send(createInitialOrderTick());
        send(createInitialOrderTick());
        send(createTickBeyondVWAPThreshold());
        // retry loop to allow for order cancellation in case of slow state update
        int maxRetries = 20;
        int retries = 0;
        var state = container.getState();
        final Logger logger = LoggerFactory.getLogger(StretchAlgoBackTest.class);
        while (retries < maxRetries && state.getCancelledChildOrders().size() < 1) {
            Thread.sleep(150); // delay to allow for state update
            state = container.getState();
            retries++;
            logger.info("Retry #" + retries + " - Cancelled Orders Count: " + state.getCancelledChildOrders().size());
        }

        assertEquals("One order should have been canceled due to price movement beyond threshold",
                1, state.getCancelledChildOrders().size());
    }
*/
}