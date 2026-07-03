package com.ticksense.telemetry;

import com.ticksense.common.TextValues;
import com.ticksense.core.EventTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractTelemetryEvent implements TelemetryEvent
{
    private final String type;
    private final TelemetryCategory category;
    private final EventTime time;
    private final Map<String, String> tags;

    protected AbstractTelemetryEvent(String type, TelemetryCategory category, EventTime time, Map<String, String> tags)
    {
        this.type = TextValues.requireText(type, "type");
        this.category = Objects.requireNonNull(category, "category");
        this.time = Objects.requireNonNull(time, "time");
        this.tags = TelemetryCollections.immutableStringMap(tags);
    }

    @Override
    public final String getType()
    {
        return type;
    }

    @Override
    public final TelemetryCategory getCategory()
    {
        return category;
    }

    @Override
    public final EventTime getTime()
    {
        return time;
    }

    @Override
    public final Map<String, String> getTags()
    {
        return tags;
    }

    protected static String requireText(String value, String fieldName)
    {
        return TextValues.requireText(value, fieldName);
    }

    protected static String safeText(String value)
    {
        return TextValues.rawOrEmpty(value);
    }

    protected static <T> List<T> immutableList(List<T> source)
    {
        return TelemetryCollections.immutableList(source);
    }

    protected static Map<String, String> immutableMap(Map<String, String> source)
    {
        return TelemetryCollections.immutableStringMap(source);
    }
}
