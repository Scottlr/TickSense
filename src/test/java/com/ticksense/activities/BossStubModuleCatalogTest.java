package com.ticksense.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ticksense.activities.hunllef.HunllefIds;
import com.ticksense.activities.phantommuspah.PhantomMuspahIds;
import com.ticksense.activities.scurrius.ScurriusIds;
import com.ticksense.core.ActivityType;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class BossStubModuleCatalogTest
{
    @Test
    public void catalogsDisabledBossStubs()
    {
        final List<ActivityModule> modules = ActivityModuleCatalog.productionModules();
        final Set<ActivityType> types = EnumSet.noneOf(ActivityType.class);
        for (ActivityModule module : modules)
        {
            types.add(module.definition().getActivityType());
            if (module.definition().getActivityType() == ActivityType.SCURRIUS
                || module.definition().getActivityType() == ActivityType.PHANTOM_MUSPAH
                || module.definition().getActivityType() == ActivityType.HUNLLEF
                || module.definition().getActivityType() == ActivityType.CORRUPTED_GAUNTLET)
            {
                assertFalse(module.isEnabled());
            }
        }

        assertTrue(types.contains(ActivityType.SCURRIUS));
        assertTrue(types.contains(ActivityType.PHANTOM_MUSPAH));
        assertTrue(types.contains(ActivityType.HUNLLEF));
        assertTrue(types.contains(ActivityType.CORRUPTED_GAUNTLET));
    }

    @Test
    public void exposesSourceOwnedBossNpcIds()
    {
        assertEquals(4, ScurriusIds.bossNpcIds().length);
        assertEquals(6, PhantomMuspahIds.bossNpcIds().length);
        assertEquals(7, HunllefIds.crystallineHunllefNpcIds().length);
        assertEquals(4, HunllefIds.corruptedHunllefNpcIds().length);
    }
}
