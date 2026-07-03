package com.ticksense.activities.execution.equipment;

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

    static Set<Integer> defaultIds()
    {
        return Set.of(DEFAULT_EQUIPMENT_CONTAINER_ID);
    }

    static Set<Integer> of(Set<Integer> containerIds)
    {
        return Collections.unmodifiableSet(new HashSet<>(containerIds));
    }

    static Set<Integer> of(Integer... containerIds)
    {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(containerIds)));
    }
}
