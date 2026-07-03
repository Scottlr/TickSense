package com.ticksense.core;

import com.ticksense.common.ImmutableCollections;
import java.util.List;
import java.util.Map;

final class CoreCollections
{
    private CoreCollections()
    {
    }

    static <T> List<T> immutableList(List<T> source)
    {
        return ImmutableCollections.immutableList(source);
    }

    static Map<String, String> immutableStringMap(Map<String, String> source)
    {
        return ImmutableCollections.immutableMap(source);
    }
}
