package com.ticksense.telemetry;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public final class TelemetryBus implements TelemetrySink
{
    private final CopyOnWriteArrayList<TelemetrySink> sinks = new CopyOnWriteArrayList<>();

    @Inject
    public TelemetryBus()
    {
    }

    public void addSink(TelemetrySink sink)
    {
        sinks.addIfAbsent(Objects.requireNonNull(sink, "sink"));
    }

    public void removeSink(TelemetrySink sink)
    {
        sinks.remove(sink);
    }

    @Override
    public void accept(TelemetryEnvelope envelope)
    {
        final TelemetryEnvelope normalizedEnvelope = Objects.requireNonNull(envelope, "envelope");
        for (TelemetrySink sink : sinks)
        {
            try
            {
                sink.accept(normalizedEnvelope);
            }
            catch (RuntimeException ex)
            {
                log.warn("Telemetry sink failed for event {}", normalizedEnvelope.getEventId(), ex);
            }
        }
    }
}
