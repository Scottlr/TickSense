package com.ticksense.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class StorageCollections
{
    private StorageCollections()
    {
    }

    static <T> List<T> immutableList(Collection<? extends T> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    static <T> List<T> immutableHead(List<? extends T> values, int limit)
    {
        if (values == null || values.isEmpty() || limit <= 0)
        {
            return Collections.emptyList();
        }
        return immutableList(values.subList(0, Math.min(limit, values.size())));
    }
}
