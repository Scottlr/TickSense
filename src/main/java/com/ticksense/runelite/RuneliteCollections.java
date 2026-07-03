package com.ticksense.runelite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class RuneliteCollections
{
    private RuneliteCollections()
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
}
