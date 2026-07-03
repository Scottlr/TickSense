package com.ticksense.telemetry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class TelemetryCollections
{
    private TelemetryCollections()
    {
    }

    static <T> List<T> immutableList(List<T> source)
    {
        if (source == null || source.isEmpty())
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(source));
    }

    static Map<String, String> immutableStringMap(Map<String, String> source)
    {
        if (source == null || source.isEmpty())
        {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
