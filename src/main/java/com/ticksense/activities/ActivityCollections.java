package com.ticksense.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ActivityCollections
{
    private ActivityCollections()
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

    static Map<String, String> immutableStringMap(Map<String, String> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}
