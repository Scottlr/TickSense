package com.ticksense.telemetry;

import com.ticksense.core.EventTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
        this.type = requireText(type, "type");
        this.category = Objects.requireNonNull(category, "category");
        this.time = Objects.requireNonNull(time, "time");
        this.tags = immutableMap(tags);
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
        final String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    protected static String safeText(String value)
    {
        return value == null ? "" : value;
    }

    protected static <T> List<T> immutableList(List<T> source)
    {
        if (source == null || source.isEmpty())
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(source));
    }

    protected static Map<String, String> immutableMap(Map<String, String> source)
    {
        if (source == null || source.isEmpty())
        {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
