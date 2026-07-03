package com.ticksense.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ticksense.activities.hunllef.HunllefIds;
import com.ticksense.activities.phantommuspah.PhantomMuspahIds;
import com.ticksense.activities.scurrius.ScurriusIds;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.events.NpcStateTelemetryEvent;
import java.util.EnumSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class BossStubModuleCatalogTest
{
    @Test
    public void catalogsObserveOnlyBossStubs()
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
                assertTrue(module.isEnabled());
            }
        }

        assertTrue(types.contains(ActivityType.SCURRIUS));
        assertTrue(types.contains(ActivityType.PHANTOM_MUSPAH));
        assertTrue(types.contains(ActivityType.HUNLLEF));
        assertTrue(types.contains(ActivityType.CORRUPTED_GAUNTLET));
    }

    @Test
    public void observeOnlyBossStubsEmitSuppressedCandidates()
    {
        assertSuppressedCandidate(new com.ticksense.activities.scurrius.ScurriusModule(), 7221);
        assertSuppressedCandidate(new com.ticksense.activities.phantommuspah.PhantomMuspahModule(), 12077);
        assertSuppressedCandidate(new com.ticksense.activities.hunllef.HunllefModule(), 9021);
        assertSuppressedCandidate(new com.ticksense.activities.hunllef.CorruptedGauntletModule(), 9035);
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

    private static void assertSuppressedCandidate(ActivityModule module, int bossNpcId)
    {
        final ActivityCandidate candidate = module.createStrategy().evaluateActivation(
            new ActivityContext("session-stub", 301, true, Collections.emptyMap()),
            new NpcStateTelemetryEvent(
                new EventTime(100L, 200L, 300, 400L, 500),
                Collections.singletonMap("source", "NpcSpawned"),
                EntityRef.npc(1, bossNpcId, "Boss"),
                "SPAWNED",
                new WorldLocation(301, 0, 3200, 3201, 12345, false),
                999,
                888,
                EntityRef.localPlayer(),
                10,
                20));

        assertTrue(candidate.isSuppressed());
        assertTrue(candidate.getSuppressionReason().contains("Observe-only"));
    }
}
