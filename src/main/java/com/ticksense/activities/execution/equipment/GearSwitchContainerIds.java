package com.ticksense.activities.execution.equipment;

import java.util.Collection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class GearSwitchContainerIds
{
    private static final int DEFAULT_EQUIPMENT_CONTAINER_ID = 94;

    private GearSwitchContainerIds()
    {
    }

    static Set<Integer> defaultContainerIds()
    {
        return Set.of(DEFAULT_EQUIPMENT_CONTAINER_ID);
    }

    static Set<Integer> of(Set<Integer> containerIds)
    {
        return immutableCopyOf(containerIds);
    }

    static Set<Integer> of(Integer... containerIds)
    {
        return immutableCopyOf(Arrays.asList(containerIds));
    }

    private static Set<Integer> immutableCopyOf(Collection<Integer> containerIds)
    {
        return Collections.unmodifiableSet(new HashSet<>(containerIds));
    }
}
