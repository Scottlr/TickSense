package com.ticksense.telemetry.events;

import com.ticksense.core.EventTime;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.Map;

public final class ClientTickTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "client.tick";

    private final int clientTickSequence;

    public ClientTickTelemetryEvent(EventTime time, Map<String, String> tags, int clientTickSequence)
    {
        super(TYPE, TelemetryCategory.ENVIRONMENT_PERFORMANCE, time, tags);
        this.clientTickSequence = clientTickSequence;
    }

    public int getClientTickSequence()
    {
        return clientTickSequence;
    }
}
