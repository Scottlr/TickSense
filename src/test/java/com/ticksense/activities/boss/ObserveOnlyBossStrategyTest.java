package com.ticksense.activities.boss;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.ticksense.activities.ActivityCandidate;
import com.ticksense.activities.ActivityContext;
import com.ticksense.activities.ActivityDefinition;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.events.NpcStateTelemetryEvent;
import java.util.Collections;
import org.junit.Test;

public class ObserveOnlyBossStrategyTest
{
    @Test
    public void emitsSuppressedDiagnosticCandidateForKnownBossNpc()
    {
        final ObserveOnlyBossStrategy strategy = new ObserveOnlyBossStrategy(
            new ActivityDefinition(ActivityType.SCURRIUS, "Scurrius", 35, 0.75D, true),
            id -> id == 7221);

        final ActivityCandidate candidate = strategy.evaluateActivation(
            context(),
            npcEvent(7221, 1234, 5678));

        assertNotNull(candidate);
        assertTrue(candidate.isSuppressed());
        assertTrue(candidate.getSuppressionReason().contains("Observe-only"));
        assertTrue(candidate.getEvidenceSummary().contains("Known boss NPC observed: npc:7221"));
        assertTrue(candidate.getEvidenceSummary().contains("Unverified event ID observed: animation:1234"));
        assertTrue(candidate.getEvidenceSummary().contains("Unverified event ID observed: graphic:5678"));
    }

    @Test
    public void ignoresUnrelatedNpc()
    {
        final ObserveOnlyBossStrategy strategy = new ObserveOnlyBossStrategy(
            new ActivityDefinition(ActivityType.SCURRIUS, "Scurrius", 35, 0.75D, true),
            id -> id == 7221);

        assertNull(strategy.evaluateActivation(context(), npcEvent(9999, 1234, 5678)));
    }

    private static ActivityContext context()
    {
        return new ActivityContext("session-observe", 301, true, Collections.emptyMap());
    }

    private static NpcStateTelemetryEvent npcEvent(int npcId, int animationId, int graphicId)
    {
        return new NpcStateTelemetryEvent(
            new EventTime(100L, 200L, 300, 400L, 500),
            Collections.singletonMap("source", "NpcSpawned"),
            EntityRef.npc(1, npcId, "Boss"),
            "SPAWNED",
            new WorldLocation(301, 0, 3200, 3201, 12345, false),
            animationId,
            graphicId,
            EntityRef.localPlayer(),
            10,
            20);
    }
}
