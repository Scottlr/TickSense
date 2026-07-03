package com.ticksense.activities.araxxor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.ActivityRegistry;
import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.ActivityStrategyEngine;
import com.ticksense.activities.execution.recovery.FoodRecoveryTracker;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.OpportunityStatus;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class AraxxorStrategyTest
{
    @Test
    public void startsWhenVerifiedAraxxorPresent()
    {
        final Harness harness = new Harness(verifiedStrategy());

        harness.accept(regionEvent(600, araxxorLocation(), "LOGGED_IN"));
        harness.accept(bossInteractEvent(601, 13668));

        assertTrue(harness.engine.getActiveSession().isPresent());
        assertEquals("Araxxor", harness.engine.getActiveSession().get().getMetadata().get("displayName"));
    }

    @Test
    public void completesSpiderEngagementOpportunity()
    {
        final Harness harness = new Harness(verifiedStrategy());

        harness.accept(regionEvent(600, araxxorLocation(), "LOGGED_IN"));
        harness.accept(bossInteractEvent(601, 13668));
        harness.accept(spiderSpawnEvent(605, 13671));
        harness.accept(spiderInteractEvent(606, 13671));
        harness.accept(bossDefeatEvent(612, 13668));

        final OpportunityMarker spider = harness.terminalOpportunity(
            AraxxorExecutionTracker.OPPORTUNITY_SPIDER_ENGAGEMENT,
            OpportunityStatus.COMPLETED);
        assertEquals(606, spider.getTime().getGameTick());

        final ActivityReportData data = harness.engine.getCompletedActivityData().get(0);
        assertEquals("1", data.getAttributes().get("spiderEngagementCount"));
    }

    @Test
    public void recordsBossReengagementLatency()
    {
        final Harness harness = new Harness(verifiedStrategy());

        harness.accept(regionEvent(600, araxxorLocation(), "LOGGED_IN"));
        harness.accept(bossInteractEvent(601, 13668));
        harness.accept(spiderSpawnEvent(605, 13671));
        harness.accept(spiderInteractEvent(606, 13671));
        harness.accept(spiderDespawnEvent(608, 13671));
        harness.accept(bossInteractEvent(610, 13668));
        harness.accept(bossDefeatEvent(615, 13668));

        final OpportunityMarker boss = harness.terminalOpportunity(
            AraxxorExecutionTracker.OPPORTUNITY_BOSS_REENGAGEMENT,
            OpportunityStatus.COMPLETED);
        assertEquals(610, boss.getTime().getGameTick());
        assertEquals("13671", boss.getContext().get("spiderNpcId"));

        final ActivityReportData data = harness.engine.getCompletedActivityData().get(0);
        assertEquals("1", data.getAttributes().get("bossReengagementCount"));
    }

    @Test
    public void attributesDamageDuringSpiderWindowOnly()
    {
        final Harness harness = new Harness(verifiedStrategy());

        harness.accept(regionEvent(600, araxxorLocation(), "LOGGED_IN"));
        harness.accept(bossInteractEvent(601, 13668));
        harness.accept(spiderSpawnEvent(605, 13671));
        harness.accept(localDamageEvent(606, 12, 20));
        harness.accept(spiderInteractEvent(607, 13671));
        harness.accept(localDamageEvent(608, 9, 16));
        harness.accept(bossDefeatEvent(612, 13668));

        final ActivityReportData data = harness.engine.getCompletedActivityData().get(0);
        assertEquals("12", data.getAttributes().get("spiderWindowDamage"));
    }

    @Test
    public void emitsReusableFoodRecoveryOpportunity()
    {
        final Harness harness = new Harness(verifiedStrategy());

        harness.accept(regionEvent(600, araxxorLocation(), "LOGGED_IN"));
        harness.accept(bossInteractEvent(601, 13668));
        harness.accept(localDamageEvent(602, 14, 42));
        harness.accept(foodConsumedEvent(603, 385));
        harness.accept(bossDefeatEvent(612, 13668));

        final OpportunityMarker food = harness.terminalOpportunity(
            FoodRecoveryTracker.ID + "." + FoodRecoveryTracker.OPPORTUNITY_FOOD_RECOVERY,
            OpportunityStatus.COMPLETED);
        assertEquals(603, food.getTime().getGameTick());
        assertEquals("14", food.getContext().get("damageAmount"));
    }

    @Test
    public void terminatesOnTeleportMidKill()
    {
        final Harness harness = new Harness(verifiedStrategy());

        harness.accept(regionEvent(600, araxxorLocation(), "LOGGED_IN"));
        harness.accept(bossInteractEvent(601, 13668));
        harness.accept(regionEvent(605, outsideLocation(), "LOGGED_IN"));

        assertEquals(FinishReasonType.TELEPORTED, harness.engine.getCompletedSessions().get(0).getFinishReason().getType());
    }

    @Test
    public void terminatesOnPlayerDeath()
    {
        final Harness harness = new Harness(verifiedStrategy());

        harness.accept(regionEvent(600, araxxorLocation(), "LOGGED_IN"));
        harness.accept(bossInteractEvent(601, 13668));
        harness.accept(localDamageEvent(605, 32, 0));

        assertEquals(FinishReasonType.PLAYER_DEAD, harness.engine.getCompletedSessions().get(0).getFinishReason().getType());
    }

    @Test
    public void doesNotStartWhenVerificationIsPartialOrBlocked()
    {
        final Harness partialHarness = new Harness(partialStrategy());
        partialHarness.accept(regionEvent(600, araxxorLocation(), "LOGGED_IN"));
        partialHarness.accept(bossInteractEvent(601, 13668));
        assertFalse(partialHarness.engine.getActiveSession().isPresent());

        final Harness blockedHarness = new Harness(blockedStrategy());
        blockedHarness.accept(regionEvent(600, araxxorLocation(), "LOGGED_IN"));
        blockedHarness.accept(bossInteractEvent(601, 13668));
        assertFalse(blockedHarness.engine.getActiveSession().isPresent());
    }

    private static AraxxorStrategy verifiedStrategy()
    {
        return new AraxxorStrategy(
            AraxxorVerificationDecision.verified(
                "2026-07-03",
                Collections.singletonList("Synthetic verified Araxxor region and interaction evidence."),
                Collections.singletonList("Synthetic test-only verification decision.")),
            new int[] {13668, 13669},
            new int[] {13671, 13673, 13675, 13680},
            new int[] {13878});
    }

    private static AraxxorStrategy partialStrategy()
    {
        return new AraxxorStrategy(
            AraxxorVerificationDecision.partiallyVerified(
                "2026-07-03",
                Collections.singletonList("Boss and spider IDs known, region evidence not fully verified."),
                Collections.singletonList("Synthetic partial verification.")),
            new int[] {13668, 13669},
            new int[] {13671, 13673, 13675, 13680},
            new int[] {13878});
    }

    private static AraxxorStrategy blockedStrategy()
    {
        return new AraxxorStrategy(
            AraxxorVerificationDecision.blocked(
                "2026-07-03",
                Collections.singletonList("No verified Araxxor fixture."),
                Collections.singletonList("Synthetic blocked verification.")),
            new int[] {13668, 13669},
            new int[] {13671, 13673, 13675, 13680},
            new int[] {13878});
    }

    private static final class Harness
    {
        private final List<OpportunityMarker> opportunityMarkers = new ArrayList<>();
        private final ActivityStrategyEngine engine;

        private Harness(AraxxorStrategy strategy)
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
            throw new AssertionError("No " + status + " marker for " + type + " in " + Arrays.toString(opportunityMarkers.toArray()));
        }
    }

    private static TelemetryEnvelope regionEvent(int tick, WorldLocation location, String gameState)
    {
        return envelope(
            "region-" + tick,
            new RegionInstanceTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "WorldViewLoaded"),
                330,
                location.getRegionId(),
                "top",
                location.isInstanced(),
                gameState,
                location));
    }

    private static TelemetryEnvelope bossInteractEvent(int tick, int npcId)
    {
        return envelope(
            "boss-interact-" + tick,
            new InteractingChangedTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "InteractingChanged"),
                EntityRef.localPlayer(),
                EntityRef.npc(1, npcId, "Araxxor"),
                "TARGET_SET"));
    }

    private static TelemetryEnvelope spiderInteractEvent(int tick, int npcId)
    {
        return envelope(
            "spider-interact-" + tick,
            new InteractingChangedTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "InteractingChanged"),
                EntityRef.localPlayer(),
                EntityRef.npc(2, npcId, "Araxyte spider"),
                "TARGET_SET"));
    }

    private static TelemetryEnvelope spiderSpawnEvent(int tick, int npcId)
    {
        return envelope(
            "spider-spawn-" + tick,
            new NpcStateTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "NpcSpawned"),
                EntityRef.npc(2, npcId, "Araxyte spider"),
                "SPAWNED",
                araxxorLocation(),
                -1,
                -1,
                EntityRef.unknown(),
                10,
                10));
    }

    private static TelemetryEnvelope spiderDespawnEvent(int tick, int npcId)
    {
        return envelope(
            "spider-despawn-" + tick,
            new NpcStateTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "NpcDespawned"),
                EntityRef.npc(2, npcId, "Araxyte spider"),
                "DESPAWNED",
                araxxorLocation(),
                -1,
                -1,
                EntityRef.unknown(),
                0,
                10));
    }

    private static TelemetryEnvelope bossDefeatEvent(int tick, int npcId)
    {
        return envelope(
            "boss-despawn-" + tick,
            new NpcStateTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "NpcDespawned"),
                EntityRef.npc(1, npcId, "Araxxor"),
                "DESPAWNED",
                araxxorLocation(),
                -1,
                -1,
                EntityRef.unknown(),
                0,
                10));
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

    private static TelemetryEnvelope foodConsumedEvent(int tick, int foodItemId)
    {
        return envelope(
            "food-" + tick,
            new InventoryDeltaTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "ItemContainerChanged"),
                93,
                Collections.singletonList(new InventoryDeltaTelemetryEvent.ItemDelta(0, foodItemId, 1, -1, 0))));
    }

    private static TelemetryEnvelope envelope(String eventId, TelemetryEvent event)
    {
        return TelemetryEnvelope.create(eventId, "session-araxxor", event);
    }

    private static EventTime time(int tick)
    {
        final long wallTimeMillis = 1_000L + (tick * 600L);
        return new EventTime(wallTimeMillis, wallTimeMillis * 1_000_000L, tick, tick * 20L, tick);
    }

    private static WorldLocation araxxorLocation()
    {
        return new WorldLocation(330, 0, 3479, 9807, 13878, true);
    }

    private static WorldLocation outsideLocation()
    {
        return new WorldLocation(330, 0, 3200, 3200, 12345, false);
    }
}
