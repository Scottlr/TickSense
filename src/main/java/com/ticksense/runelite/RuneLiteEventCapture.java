package com.ticksense.runelite;

import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class RuneLiteEventCapture
{
    private static final Consumer<RuneLiteEventEnvelope> NO_OP_SINK = envelope -> { };

    private final RuneLiteClock clock;
    private final Consumer<RuneLiteEventEnvelope> sink;

    @Inject
    RuneLiteEventCapture(RuneLiteClock clock)
    {
        this(clock, NO_OP_SINK);
    }

    RuneLiteEventCapture(RuneLiteClock clock, Consumer<RuneLiteEventEnvelope> sink)
    {
        this.clock = clock;
        this.sink = sink;
    }

    public void onGameTick()
    {
        clock.advanceGameTick();
        capture("GameTick");
    }

    public void onClientTick()
    {
        clock.advanceClientTick();
        capture("ClientTick");
    }

    public void capture(String sourceEventType)
    {
        captureEnvelope(sourceEventType);
    }

    public RuneLiteEventEnvelope captureEnvelope(String sourceEventType)
    {
        final RuneLiteEventEnvelope envelope = clock.capture(sourceEventType);
        sink.accept(envelope);
        return envelope;
    }
}
