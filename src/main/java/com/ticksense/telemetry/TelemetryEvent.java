package com.ticksense.telemetry;

import com.ticksense.core.EventTime;
import java.util.Map;

public interface TelemetryEvent
{
    String getType();

    TelemetryCategory getCategory();

    EventTime getTime();

    Map<String, String> getTags();
}
