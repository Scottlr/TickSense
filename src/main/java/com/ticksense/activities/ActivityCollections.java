package com.ticksense.activities;

import com.ticksense.common.ImmutableCollections;
import java.util.Collection;
import java.util.List;
import java.util.Map;

final class ActivityCollections
{
    private ActivityCollections()
    {
    }

    static <T> List<T> immutableList(Collection<? extends T> values)
    {
        return ImmutableCollections.immutableList(values);
    }

    static Map<String, String> immutableStringMap(Map<String, String> values)
    {
        return ImmutableCollections.immutableMap(values);
    }
}
