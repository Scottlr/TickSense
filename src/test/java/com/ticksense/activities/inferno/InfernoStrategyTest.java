package com.ticksense.activities.inferno;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.ActivityRegistry;
import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.ActivityStrategyEngine;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.activities.execution.recovery.FoodRecoveryTracker;
import com.ticksense.activities.execution.recovery.PotionRecoveryTracker;
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.FinishReasonType;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.DamageTelemetryEvent;
import com.ticksense.telemetry.events.InteractingChangedTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.NpcStateTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.runelite.api.ItemID;
import net.runelite.api.NpcID;
import org.junit.Test;

public class InfernoStrategyTest
{
    @Test
    public void recordsWaveSpanLifecycle()
    {
        final Harness harness = new Harness(verifiedStrategy());

        harness.accept(regionEvent(700, infernoLocation(), "LOGGED_IN"));
        harness.accept(waveNpcSpawnEvent(701, NpcID.JALNIB));
        harness.accept(regionEvent(720, outsideLocation(), "LOGGED_IN"));

        final OpportunityMarker wave = harness.terminalOpportunity(InfernoState.OPPORTUNITY_WAVE, OpportunityStatus.COMPLETED);
        assertEquals(720, wave.getTime().getGameTick());
        assertEquals(String.valueOf(NpcID.JALNIB), wave.getContext().get("waveNpcId"));
        assertEquals(FinishReasonType.LEFT_REGION, harness.engine.getCompletedSessions().get(0).getFinishReason().getType());

        final ActivityReportData data = harness.engine.getCompletedActivityData().get(0);
        assertEquals("1", data.getAttributes().get("waveSpanCount"));
    }

    @Test
    public void recordsNibblerResponseOpportunity()
    {
        final Harness harness = new Harness(verifiedStrategy());

        harness.accept(regionEvent(700, infernoLocation(), "LOGGED_IN"));
        harness.accept(waveNpcSpawnEvent(701, NpcID.JALNIB));
        harness.accept(nibblerSpawnEvent(705, NpcID.JALNIBREK));
        harness.accept(nibblerInteractEvent(706, NpcID.JALNIBREK));
        harness.accept(supplyUseEvent(707, ItemID.PRAYER_POTION4));
        harness.accept(regionEvent(720, outsideLocation(), "LOGGED_IN"));

        final OpportunityMarker nibbler = harness.terminalOpportunity(InfernoState.OPPORTUNITY_NIBBLER_WINDOW, OpportunityStatus.COMPLETED);
        assertEquals(706, nibbler.getTime().getGameTick());

        final ActivityReportData data = harness.engine.getCompletedActivityData().get(0);
        assertEquals("1", data.getAttributes().get("nibblerResponseCount"));
        assertEquals("1", data.getAttributes().get("supplyUseCount"));

        final OpportunityMarker potion = harness.terminalOpportunity(
            PotionRecoveryTracker.ID + "." + PotionRecoveryTracker.OPPORTUNITY_POTION_RECOVERY,
            OpportunityStatus.COMPLETED);
        assertEquals(707, potion.getTime().getGameTick());
    }

    @Test
    public void emitsReusableFoodRecoveryOpportunity()
    {
        final Harness harness = new Harness(verifiedStrategy());

        harness.accept(regionEvent(700, infernoLocation(), "LOGGED_IN"));
        harness.accept(waveNpcSpawnEvent(701, NpcID.JALNIB));
        harness.accept(localDamageEvent(702, 31, 55));
        harness.accept(supplyUseEvent(703, 385));
        harness.accept(regionEvent(720, outsideLocation(), "LOGGED_IN"));

        final OpportunityMarker recovery = harness.terminalOpportunity(
            FoodRecoveryTracker.ID + "." + FoodRecoveryTracker.OPPORTUNITY_FOOD_RECOVERY,
            OpportunityStatus.COMPLETED);
        assertEquals(703, recovery.getTime().getGameTick());
    }

    @Test
    public void omitsPrayerTimingWhenUnverified()
    {
        final Harness harness = new Harness(verifiedStrategy());

        harness.accept(regionEvent(700, infernoLocation(), "LOGGED_IN"));
        harness.accept(waveNpcSpawnEvent(701, NpcID.JALNIB));
        harness.accept(nibblerSpawnEvent(705, NpcID.JALNIBREK));
        harness.accept(regionEvent(720, outsideLocation(), "LOGGED_IN"));

        assertTrue(harness.opportunityMarkers.stream().noneMatch(marker -> InfernoState.OPPORTUNITY_PRAYER_WINDOW.equals(marker.getOpportunityType())));
        final ActivityReportData data = harness.engine.getCompletedActivityData().get(0);
        assertEquals("0", data.getAttributes().get("prayerWindowCount"));
        assertEquals(InfernoVerificationDecision.EvidenceStatus.BLOCKED.name(), data.getAttributes().get("prayerEvidenceStatus"));
        assertFalse(verifiedStrategy().getDefinition().isBossActivity() == false);
    }

    private static InfernoStrategy verifiedStrategy()
    {
        return new InfernoStrategy(
            InfernoVerificationDecision.verified(
                "2026-07-03",
                InfernoVerificationDecision.EvidenceStatus.VERIFIED,
                InfernoVerificationDecision.EvidenceStatus.VERIFIED,
                InfernoVerificationDecision.EvidenceStatus.BLOCKED,
                InfernoVerificationDecision.EvidenceStatus.VERIFIED,
                InfernoVerificationDecision.EvidenceStatus.BLOCKED,
                Collections.singletonList("Synthetic verified Inferno wave and nibbler evidence."),
                Collections.singletonList("Prayer timing intentionally stays blocked in tests.")),
            new int[] {NpcID.JALNIBREK, NpcID.JALNIBREK_7675},
            new int[] {NpcID.JALNIB},
            new int[] {ItemID.PRAYER_POTION4},
            new int[] {9043});
    }

    private static final class Harness
    {
        private final List<OpportunityMarker> opportunityMarkers = new ArrayList<>();
        private final ActivityStrategyEngine engine;

        private Harness(InfernoStrategy strategy)
        {
            final List<ActivityMarker> activityMarkers = new ArrayList<>();
            engine = new ActivityStrategyEngine(
                ActivityRegistry.builder().register(strategy).build(),
                activityMarkers::add,
                opportunityMarkers::add,
                true);
        }

        private void accept(TelemetryEnvelope envelope)
        {
            engine.accept(envelope);
        }

        private OpportunityMarker terminalOpportunity(String type, OpportunityStatus status)
        {
            for (OpportunityMarker marker : opportunityMarkers)
            {
                if (type.equals(marker.getOpportunityType()) && marker.getStatus() == status)
                {
                    return marker;
                }
            }
            throw new AssertionError("No " + status + " marker for " + type);
        }
    }

    private static TelemetryEnvelope regionEvent(int tick, WorldLocation location, String gameState)
    {
        return envelope(
            "region-" + tick,
            new RegionInstanceTelemetryEvent(time(tick), Collections.singletonMap("source", "WorldViewLoaded"), 330, location.getRegionId(), "top", true, gameState, location));
    }

    private static TelemetryEnvelope waveNpcSpawnEvent(int tick, int npcId)
    {
        return envelope(
            "wave-npc-" + tick,
            new NpcStateTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "NpcSpawned"),
                EntityRef.npc(1, npcId, "Inferno wave NPC"),
                "SPAWNED",
                infernoLocation(),
                -1,
                -1,
                EntityRef.unknown(),
                -1,
                -1));
    }

    private static TelemetryEnvelope nibblerSpawnEvent(int tick, int npcId)
    {
        return envelope(
            "nibbler-npc-" + tick,
            new NpcStateTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "NpcSpawned"),
                EntityRef.npc(2, npcId, "Jal-Nib"),
                "SPAWNED",
                infernoLocation(),
                -1,
                -1,
                EntityRef.unknown(),
                -1,
                -1));
    }

    private static TelemetryEnvelope nibblerInteractEvent(int tick, int npcId)
    {
        return envelope(
            "nibbler-interact-" + tick,
            new InteractingChangedTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "InteractingChanged"),
                EntityRef.localPlayer(),
                EntityRef.npc(2, npcId, "Jal-Nib"),
                "TARGET_SET"));
    }

    private static TelemetryEnvelope supplyUseEvent(int tick, int itemId)
    {
        return envelope(
            "supply-use-" + tick,
            new InventoryDeltaTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "ItemContainerChanged"),
                93,
                Collections.singletonList(new InventoryDeltaTelemetryEvent.ItemDelta(0, itemId, 2, itemId, 1))));
    }

    private static TelemetryEnvelope localDamageEvent(int tick, int amount, int healthRatio)
    {
        return envelope(
            "damage-" + tick,
            new DamageTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "HitsplatApplied"),
                EntityRef.localPlayer(),
                1,
                amount,
                healthRatio,
                99));
    }

    private static TelemetryEnvelope envelope(String eventId, TelemetryEvent event)
    {
        return TelemetryEnvelope.create(eventId, "session-inferno", event);
    }

    private static EventTime time(int tick)
    {
        final long wallTimeMillis = 1_000L + (tick * 600L);
        return new EventTime(wallTimeMillis, wallTimeMillis * 1_000_000L, tick, tick * 20L, tick);
    }

    private static WorldLocation infernoLocation()
    {
        return new WorldLocation(330, 0, 2270, 5320, 9043, true);
    }

    private static WorldLocation outsideLocation()
    {
        return new WorldLocation(330, 0, 3200, 3200, 12345, false);
    }
}
