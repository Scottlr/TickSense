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
        assertTrue(ScurriusIds.isBossNpcId(7221));
        assertTrue(ScurriusIds.isBossNpcId(15695));
        assertFalse(ScurriusIds.isBossNpcId(-1));

        assertTrue(PhantomMuspahIds.isBossNpcId(12077));
        assertTrue(PhantomMuspahIds.isBossNpcId(15549));
        assertFalse(PhantomMuspahIds.isBossNpcId(-1));

        assertTrue(HunllefIds.isCrystallineHunllef(9021));
        assertTrue(HunllefIds.isCorruptedHunllef(9035));
        assertTrue(HunllefIds.isHunllef(15614));
        assertFalse(HunllefIds.isHunllef(-1));
    }
}
