package com.ticksense.activities.execution.equipment;

import com.ticksense.activities.ids.InventoryContainerIds;
import java.util.Collection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class GearSwitchContainerIds
{
    private GearSwitchContainerIds()
    {
    }

    static Set<Integer> defaultContainerIds()
    {
        return InventoryContainerIds.equipmentContainerIds();
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
