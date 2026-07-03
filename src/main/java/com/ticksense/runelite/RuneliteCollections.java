package com.ticksense.runelite;

import com.ticksense.common.ImmutableCollections;
import java.util.Collection;
import java.util.List;

final class RuneliteCollections
{
    private RuneliteCollections()
    {
    }

    static <T> List<T> immutableList(Collection<? extends T> values)
    {
        return ImmutableCollections.immutableList(values);
    }
}
