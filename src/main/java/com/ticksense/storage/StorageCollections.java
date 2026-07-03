package com.ticksense.storage;

import com.ticksense.common.ImmutableCollections;
import java.util.Collection;
import java.util.List;

final class StorageCollections
{
    private StorageCollections()
    {
    }

    static <T> List<T> immutableList(Collection<? extends T> values)
    {
        return ImmutableCollections.immutableList(values);
    }

    static <T> List<T> immutableHead(List<? extends T> values, int limit)
    {
        return ImmutableCollections.immutableHead(values, limit);
    }
}
