package com.ticksense.activities.vardorvis;

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
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.FinishReasonType;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.DamageTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.MovementTelemetryEvent;
import com.ticksense.telemetry.events.ProjectileTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class VardorvisStrategyTest
{
    @Test
    public void recordsVerifiedRangedHeadResponse()
    {
        final Harness harness = new Harness(verifiedStrategy());

        harness.accept(regionEvent(500, playerLocation()));
        harness.accept(rangedHeadProjectileEvent(501, 9911));
        harness.accept(movementEvent(502));
        harness.accept(regionEvent(503, outsideLocation()));

        final OpportunityMarker marker = harness.completedOpportunity(VardorvisState.OPPORTUNITY_RANGED_HEAD_RESPONSE);
        assertEquals(502, marker.getTime().getGameTick());
        assertEquals("9911", marker.getContext().get("projectileId"));
        assertEquals("12226", marker.getContext().get("sourceNpcId"));
        assertEquals("4405", marker.getContext().get("regionId"));
        assertEquals(FinishReasonType.LEFT_REGION, harness.engine.getCompletedSessions().get(0).getFinishReason().getType());

        final ActivityReportData data = harness.engine.getCompletedActivityData().get(0);
        assertEquals("VERIFIED", data.getAttributes().get("verificationStatus"));
        assertEquals("1", data.getAttributes().get("rangedHeadResponseCount"));
        assertEquals("0", data.getAttributes().get("rangedHeadDamageFailures"));
    }

    @Test
    public void omitsUnverifiedMechanic()
    {
        final Harness harness = new Harness(new VardorvisStrategy());

        harness.accept(regionEvent(500, playerLocation()));
        harness.accept(rangedHeadProjectileEvent(501, 9911));
        harness.accept(movementEvent(502));

        assertFalse(harness.engine.getActiveSession().isPresent());
        assertTrue(harness.engine.getCompletedSessions().isEmpty());
        assertTrue(harness.opportunityMarkers.isEmpty());
    }

    @Test
    public void emitsReusableFoodRecoveryOpportunity()
    {
        final Harness harness = new Harness(verifiedStrategy());

        harness.accept(regionEvent(500, playerLocation()));
        harness.accept(rangedHeadProjectileEvent(501, 9911));
        harness.accept(damageEvent(502, 16, 48));
        harness.accept(foodConsumedEvent(503, 385));
        harness.accept(regionEvent(504, outsideLocation()));

        final OpportunityMarker marker = harness.completedOpportunity(
            FoodRecoveryTracker.ID + "." + FoodRecoveryTracker.OPPORTUNITY_FOOD_RECOVERY);
        assertEquals(503, marker.getTime().getGameTick());
        assertEquals("16", marker.getContext().get("damageAmount"));
    }

    private static VardorvisStrategy verifiedStrategy()
    {
        return new VardorvisStrategy(
            VardorvisVerificationDecision.verified(
                "2026-07-03",
                Collections.singletonList(VardorvisState.MECHANIC_RANGED_HEAD_RESPONSE),
                Collections.singletonList("Synthetic verified ranged-head projectile and region evidence."),
                Collections.singletonList("Synthetic test-only verification decision.")),
            new int[] {12223},
            new int[] {12226},
            new int[] {9911},
            new int[0],
            new int[0],
            new int[] {4405});
    }

    private static final class Harness
    {
        private final List<OpportunityMarker> opportunityMarkers = new ArrayList<>();
        private final ActivityStrategyEngine engine;

        private Harness(VardorvisStrategy strategy)
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

        private OpportunityMarker completedOpportunity(String type)
        {
            for (OpportunityMarker marker : opportunityMarkers)
            {
                if (type.equals(marker.getOpportunityType()) && marker.getStatus() == OpportunityStatus.COMPLETED)
                {
                    return marker;
                }
            }
            throw new AssertionError("No completed opportunity marker for " + type + " in " + Arrays.toString(opportunityMarkers.toArray()));
        }
    }

    private static TelemetryEnvelope regionEvent(int tick, WorldLocation location)
    {
        return envelope(
            "region-" + tick,
            new RegionInstanceTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "WorldViewLoaded"),
                330,
                location.getRegionId(),
                "top",
                true,
                "LOGGED_IN",
                location));
    }

    private static TelemetryEnvelope rangedHeadProjectileEvent(int tick, int projectileId)
    {
        return envelope(
            "projectile-" + tick,
            new ProjectileTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "ProjectileMoved"),
                projectileId,
                EntityRef.npc(14, 12226, "Vardorvis head"),
                EntityRef.localPlayer(),
                playerLocation(),
                tick * 20,
                (tick * 20) + 10));
    }

    private static TelemetryEnvelope movementEvent(int tick)
    {
        return envelope(
            "move-" + tick,
            new MovementTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "MovementSnapshot"),
                EntityRef.localPlayer(),
                playerLocation(),
                dodgedLocation(),
                "WALK",
                2));
    }

    private static TelemetryEnvelope damageEvent(int tick, int amount, int healthRatio)
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
        return TelemetryEnvelope.create(eventId, "session-vardorvis", event);
    }

    private static EventTime time(int tick)
    {
        final long wallTimeMillis = 1_000L + (tick * 600L);
        return new EventTime(wallTimeMillis, wallTimeMillis * 1_000_000L, tick, tick * 20L, tick);
    }

    private static WorldLocation playerLocation()
    {
        return new WorldLocation(330, 0, 1115, 3421, 4405, true);
    }

    private static WorldLocation dodgedLocation()
    {
        return new WorldLocation(330, 0, 1117, 3421, 4405, true);
    }

    private static WorldLocation outsideLocation()
    {
        return new WorldLocation(330, 0, 3200, 3200, 12345, false);
    }
}
