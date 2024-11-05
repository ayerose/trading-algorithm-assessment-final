
package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.container.Actioner;
import codingblackfemales.container.AlgoContainer;
import codingblackfemales.container.RunTrigger;
import codingblackfemales.orderbook.OrderBook;
import codingblackfemales.orderbook.channel.MarketDataChannel;
import codingblackfemales.orderbook.channel.OrderChannel;
import codingblackfemales.orderbook.consumer.OrderBookInboundOrderConsumer;
import codingblackfemales.sequencer.DefaultSequencer;
import codingblackfemales.sequencer.Sequencer;
import codingblackfemales.sequencer.consumer.LoggingConsumer;
import codingblackfemales.sequencer.marketdata.SequencerTestCase;
import codingblackfemales.sequencer.net.TestNetwork;
import codingblackfemales.service.MarketDataService;
import codingblackfemales.service.OrderService;
import messages.marketdata.*;
import codingblackfemales.gettingstarted.StretchAlgoLogic;

import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

public abstract class AbstractAlgoBackTest extends SequencerTestCase {


    protected AlgoContainer container;

    protected StretchAlgoLogic stretchAlgoLogic;


    @Override
    public Sequencer getSequencer() {
        final TestNetwork network = new TestNetwork();
        final Sequencer sequencer = new DefaultSequencer(network);

        final RunTrigger runTrigger = new RunTrigger();
        final Actioner actioner = new Actioner(sequencer);

        final MarketDataChannel marketDataChannel = new MarketDataChannel(sequencer);
        final OrderChannel orderChannel = new OrderChannel(sequencer);
        final OrderBook book = new OrderBook(marketDataChannel, orderChannel);

        final OrderBookInboundOrderConsumer orderConsumer = new OrderBookInboundOrderConsumer(book);

        container = new AlgoContainer(new MarketDataService(runTrigger), new OrderService(runTrigger), runTrigger, actioner);
        //set my algo logic
        container.setLogic(createAlgoLogic());

        network.addConsumer(new LoggingConsumer());
        network.addConsumer(book);
        network.addConsumer(container.getMarketDataService());
        network.addConsumer(container.getOrderService());
        network.addConsumer(orderConsumer);
        network.addConsumer(container);

        return sequencer;
    }

    public abstract AlgoLogic createAlgoLogic();

    protected UnsafeBuffer createTick() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();


        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        //write the encoded output to the direct buffer
        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        //set the fields to desired values
        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);
        encoder.source(Source.STREAM);

        encoder.bidBookCount(3)
                .next().price(98L).size(100L)
                .next().price(95L).size(200L)
                .next().price(91L).size(300L);

        encoder.askBookCount(4)
                .next().price(100L).size(101L)
                .next().price(110L).size(200L)
                .next().price(115L).size(5000L)
                .next().price(119L).size(5600L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);

        return directBuffer;
    }

    protected UnsafeBuffer createTick2() {

        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        //write the encoded output to the direct buffer
        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        //set the fields to desired values
        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);
        encoder.source(Source.STREAM);

        encoder.bidBookCount(3)
                .next().price(95L).size(100L)
                .next().price(93L).size(200L)
                .next().price(91L).size(300L);

        encoder.askBookCount(4)
                .next().price(98L).size(501L)
                .next().price(101L).size(200L)
                .next().price(110L).size(5000L)
                .next().price(119L).size(5600L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);

        return directBuffer;
    }

    protected UnsafeBuffer createTick3() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);

        // prices are increasing
        encoder.askBookCount(3)
                .next().price(110L).size(200L)
                .next().price(115L).size(300L)
                .next().price(120L).size(5000L);

        encoder.bidBookCount(3)
                .next().price(105L).size(100L)
                .next().price(100L).size(250L)
                .next().price(98L).size(300L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        encoder.source(Source.STREAM);

        return directBuffer;
    }

    protected UnsafeBuffer createTickLowLiquidity() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);

        encoder.askBookCount(1)
                .next().price(105L).size(100L);

        encoder.bidBookCount(1)
                .next().price(102L).size(120L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        encoder.source(Source.STREAM);

        return directBuffer;
    }

    protected UnsafeBuffer createTickHighPrices() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);

        encoder.askBookCount(3)
                .next().price(120L).size(50L)
                .next().price(125L).size(150L)
                .next().price(130L).size(1000L);

        encoder.bidBookCount(3)
                .next().price(115L).size(80L)
                .next().price(110L).size(150L)
                .next().price(100L).size(300L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        encoder.source(Source.STREAM);

        return directBuffer;
    }

    protected UnsafeBuffer createTickWithTightSpread() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);
        encoder.source(Source.STREAM);

        // tight spread: bid = 97, ask = 99 (spread = 2, which is within spread_thresh)
        encoder.bidBookCount(3)
                .next().price(97L).size(100L)
                .next().price(96L).size(200L)
                .next().price(95L).size(300L);

        encoder.askBookCount(3)
                .next().price(99L).size(101L)
                .next().price(105L).size(200L)
                .next().price(110L).size(300L);

        return directBuffer;
    }

    protected UnsafeBuffer createTickWithWideSpread() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);
        encoder.source(Source.STREAM);

        // wide spread: bid = 90 // ask = 110 // spread = 20, which exceeds spread_thresh
        encoder.bidBookCount(3)
                .next().price(90L).size(100L)
                .next().price(89L).size(200L)
                .next().price(88L).size(300L);

        encoder.askBookCount(3)
                .next().price(110L).size(101L)
                .next().price(115L).size(200L)
                .next().price(120L).size(300L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);

        return directBuffer;
    }

    protected UnsafeBuffer createTickLow() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);


        encoder.bidBookCount(3)
                .next().price(90L).size(100L)
                .next().price(88L).size(200L)
                .next().price(85L).size(300L);

        encoder.askBookCount(3)
                .next().price(92L).size(150L)
                .next().price(95L).size(300L)
                .next().price(98L).size(500L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        encoder.source(Source.STREAM);

        return directBuffer;
    }


    protected UnsafeBuffer createTickHigh() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);

        // higher prices to encourage selling
        encoder.bidBookCount(3)
                .next().price(105L).size(100L)
                .next().price(102L).size(200L)
                .next().price(100L).size(300L);

        encoder.askBookCount(3)
                .next().price(108L).size(150L)
                .next().price(110L).size(300L)
                .next().price(115L).size(500L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        encoder.source(Source.STREAM);

        return directBuffer;
    }


    // low price scenario to trigger a buy
    protected UnsafeBuffer createTickLowBuyOpportunity() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        encoder.instrumentId(123L);
        encoder.venue(Venue.XLON);
        encoder.source(Source.STREAM);

        encoder.bidBookCount(1)
                .next().price(80L).size(100L);

        encoder.askBookCount(1)
                .next().price(85L).size(150L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        return directBuffer;
    }

    protected UnsafeBuffer createTickMedium() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        encoder.instrumentId(123L);
        encoder.venue(Venue.XLON);
        encoder.source(Source.STREAM);

        encoder.bidBookCount(1)
                .next().price(90L).size(100L);

        encoder.askBookCount(1)
                .next().price(92L).size(150L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        return directBuffer;
    }

    // high price scenario to trigger a profitable sell
    protected UnsafeBuffer createTickHighSellOpportunity() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        encoder.instrumentId(123L);
        encoder.venue(Venue.XLON);
        encoder.source(Source.STREAM);

        encoder.bidBookCount(1)
                .next().price(105L).size(100L);
        encoder.askBookCount(1)
                .next().price(110L).size(150L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        return directBuffer;
    }

    // stable high prices to maintain selling environment
    protected UnsafeBuffer createTickStableHigh() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        encoder.instrumentId(123L);
        encoder.venue(Venue.XLON);
        encoder.source(Source.STREAM);

        encoder.bidBookCount(1)
                .next().price(107L).size(100L);

        encoder.askBookCount(1)
                .next().price(112L).size(150L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        return directBuffer;
    }

    // Initial low prices for buying opportunities
    protected UnsafeBuffer createBullishTickLow() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        encoder.instrumentId(123L);
        encoder.venue(Venue.XLON);
        encoder.source(Source.STREAM);

        encoder.bidBookCount(1)
                .next().price(90L).size(100L);

        encoder.askBookCount(1)
                .next().price(92L).size(150L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        return directBuffer;
    }

    // Mid-level prices showing upward trend
    protected UnsafeBuffer createBullishTickMid() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        encoder.instrumentId(123L);
        encoder.venue(Venue.XLON);
        encoder.source(Source.STREAM);

        encoder.bidBookCount(1)
                .next().price(95L).size(100L);

        encoder.askBookCount(1)
                .next().price(98L).size(150L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        return directBuffer;
    }

    // Higher prices for potential selling opportunities
    protected UnsafeBuffer createBullishTickHigh() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        encoder.instrumentId(123L);
        encoder.venue(Venue.XLON);
        encoder.source(Source.STREAM);

        encoder.bidBookCount(1)
                .next().price(105L).size(100L);

        encoder.askBookCount(1)
                .next().price(108L).size(150L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        return directBuffer;
    }

    // Stable high prices to confirm an ongoing bullish environment
    protected UnsafeBuffer createBullishTickStableHigh() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        encoder.instrumentId(123L);
        encoder.venue(Venue.XLON);
        encoder.source(Source.STREAM);

        encoder.bidBookCount(1)
                .next().price(107L).size(100L);

        encoder.askBookCount(1)
                .next().price(110L).size(150L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        return directBuffer;
    }

    // Tick with initial values for VWAP setup
    protected UnsafeBuffer createInitialOrderTick() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        encoder.instrumentId(123L);
        encoder.venue(Venue.XLON);
        encoder.source(Source.STREAM);

        encoder.bidBookCount(1)
                .next().price(100L).size(150L);
        encoder.askBookCount(1)
                .next().price(102L).size(100L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        return directBuffer;
    }

    // Tick within the cancellation threshold, shouldn't trigger cancellation
    protected UnsafeBuffer createTickWithinThreshold() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        encoder.instrumentId(123L);
        encoder.venue(Venue.XLON);
        encoder.source(Source.STREAM);

        encoder.bidBookCount(1)
                .next().price(101L).size(150L);  // Close to initial VWAP
        encoder.askBookCount(1)
                .next().price(103L).size(100L);  // Close to initial VWAP

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        return directBuffer;
    }

    // Tick beyond the VWAP threshold to trigger cancellation
    protected UnsafeBuffer createTickBeyondVWAPThreshold() {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        encoder.instrumentId(123L);
        encoder.venue(Venue.XLON);
        encoder.source(Source.STREAM);

        encoder.bidBookCount(1)
                .next().price(90L).size(150L);  // Significant deviation to trigger cancellation
        encoder.askBookCount(1)
                .next().price(110L).size(100L); // Significant deviation to trigger cancellation

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        return directBuffer;
    }


}
