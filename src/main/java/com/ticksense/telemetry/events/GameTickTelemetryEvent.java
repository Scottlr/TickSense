package com.ticksense.telemetry.events;

import com.ticksense.core.EventTime;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.Map;

public final class GameTickTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "game.tick";

    private final int tick;

    public GameTickTelemetryEvent(EventTime time, Map<String, String> tags, int tick)
    {
        super(TYPE, TelemetryCategory.ENVIRONMENT_PERFORMANCE, time, tags);
        this.tick = tick;
    }

    public int getTick()
    {
        return tick;
    }
}
