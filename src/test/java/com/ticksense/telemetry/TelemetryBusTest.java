package com.ticksense.telemetry;

import static org.junit.Assert.assertEquals;

import com.ticksense.core.EventTime;
import com.ticksense.telemetry.events.GameTickTelemetryEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class TelemetryBusTest
{
    @Test
    public void deliversEventsToSinksInOrder()
    {
        final TelemetryBus telemetryBus = new TelemetryBus();
        final List<String> deliveries = new ArrayList<>();
        telemetryBus.addSink(envelope -> deliveries.add("first:" + envelope.getEventId()));
        telemetryBus.addSink(envelope -> deliveries.add("second:" + envelope.getEventId()));

        telemetryBus.accept(envelope("event-1"));

        assertEquals(2, deliveries.size());
        assertEquals("first:event-1", deliveries.get(0));
        assertEquals("second:event-1", deliveries.get(1));
    }

    @Test
    public void continuesAfterSinkFailure()
    {
        final TelemetryBus telemetryBus = new TelemetryBus();
        final List<String> deliveries = new ArrayList<>();
        telemetryBus.addSink(envelope -> deliveries.add("first:" + envelope.getEventId()));
        telemetryBus.addSink(envelope -> {
            throw new IllegalStateException("boom");
        });
        telemetryBus.addSink(envelope -> deliveries.add("third:" + envelope.getEventId()));

        telemetryBus.accept(envelope("event-2"));

        assertEquals(2, deliveries.size());
        assertEquals("first:event-2", deliveries.get(0));
        assertEquals("third:event-2", deliveries.get(1));
    }

    private static TelemetryEnvelope envelope(String eventId)
    {
        return TelemetryEnvelope.create(
            eventId,
            "session-1",
            new GameTickTelemetryEvent(new EventTime(10L, 20L, 30, 40L, 50), java.util.Collections.singletonMap("source", "GameTick"), 30));
    }
}
