package codingblackfemales.gettingstarted;
import messages.marketdata.*;
import org.agrona.concurrent.UnsafeBuffer;
import java.nio.ByteBuffer;
import java.util.List;

public class MarketDataSimulator {

    // simulate a market data sequence
    public static List<UnsafeBuffer> getBullishMarketSequence() {
        return List.of(
                createTick(90L, 92L),
                createTick(95L, 98L),
                createTick(100L, 102L),
                createTick(105L, 108L),
                createTick(110L, 115L)
        );}

    public static List<UnsafeBuffer> getBearishMarketSequence() {
        return List.of(
                createTick(110L, 112L),
                createTick(105L, 108L),
                createTick(100L, 102L),
                createTick(95L, 98L),
                createTick(90L, 92L)
        );
    }
    public static List<UnsafeBuffer> getVolatileMarketSequence() {
        return List.of(
                createTick(100L, 102L),
                createTick(110L, 115L),
                createTick(90L, 92L),
                createTick(105L, 108L),
                createTick(95L, 98L)
        );
    }
    private static UnsafeBuffer createTick(long bidPrice, long askPrice) {
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();
        final UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(1024));
        encoder.wrapAndApplyHeader(buffer, 0, headerEncoder);
        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);
        encoder.source(Source.STREAM);
        encoder.bidBookCount(1).next().price(bidPrice).size(150L);
        encoder.askBookCount(1).next().price(askPrice).size(150L);
        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);

        return buffer;
    }
}