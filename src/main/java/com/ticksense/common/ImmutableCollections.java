package com.ticksense.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ImmutableCollections
{
    private ImmutableCollections()
    {
    }

    public static <T> List<T> immutableList(Collection<? extends T> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    public static <K, V> Map<K, V> immutableMap(Map<? extends K, ? extends V> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public static <T> List<T> immutableHead(List<? extends T> values, int limit)
    {
        if (values == null || values.isEmpty() || limit <= 0)
        {
            return Collections.emptyList();
        }
        return immutableList(values.subList(0, Math.min(limit, values.size())));
    }
}
