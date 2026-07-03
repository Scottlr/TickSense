package com.ticksense.activities.gemmining;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.ActivityRegistry;
import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.ActivityStrategyEngine;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.activities.execution.equipment.GearSwitchTracker;
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.StateChanges;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.events.AnimationTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.MovementTelemetryEvent;
import com.ticksense.telemetry.events.ObjectStateTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class GemMiningStrategyTest
{
    @Test
    public void recordsRespawnToClickLatency()
    {
        final Harness harness = new Harness();

        harness.accept(regionEvent(200, 11410, playerLocation(2840, 9388, 11410), "LOGGED_IN"));
        harness.accept(availableRockEvent(200, 11380, rockLocation()));
        harness.accept(mineClickEvent(202, rockLocation()));
        harness.accept(miningAnimationEvent(203, 624));

        assertTrue(harness.engine.getActiveSession().isPresent());
        final OpportunityMarker completed = harness.completedOpportunity(GemMiningState.OPPORTUNITY_RESPAWN_TO_CLICK);
        assertEquals(202, completed.getTime().getGameTick());
        assertEquals("11380", completed.getContext().get("objectId"));
        assertEquals("11410", completed.getContext().get("regionId"));
        assertEquals(4, harness.opportunityMarkers.size());
    }

    @Test
    public void doesNotPenalizeMiningRngAsIdleTicks()
    {
        final Harness harness = new Harness();

        harness.accept(regionEvent(100, 11410, playerLocation(2840, 9388, 11410), "LOGGED_IN"));
        harness.accept(availableRockEvent(100, 11380, rockLocation()));
        harness.accept(mineClickEvent(100, rockLocation()));
        harness.accept(miningAnimationEvent(101, 624));
        harness.accept(depletedRockEvent(102, 11380, rockLocation()));
        harness.accept(regionEvent(111, 12000, playerLocation(3200, 3200, 12000), "LOGGED_IN"));

        assertFalse(harness.engine.getActiveSession().isPresent());
        assertEquals(1, harness.engine.getCompletedActivityData().size());
        final ActivityReportData data = harness.engine.getCompletedActivityData().get(0);
        assertEquals("0", data.getAttributes().get("idleTicks"));
        assertEquals("1", data.getAttributes().get("respawnOpportunityCount"));
    }

    @Test
    public void lowConfidenceEvidenceDoesNotStartActivity()
    {
        final Harness harness = new Harness();

        harness.accept(regionEvent(200, 11410, playerLocation(2840, 9388, 11410), "LOGGED_IN"));
        harness.accept(availableRockEvent(200, 11380, rockLocation()));

        assertFalse(harness.engine.getActiveSession().isPresent());
        assertTrue(harness.engine.getCompletedSessions().isEmpty());
        assertEquals(1, harness.engine.getDiagnostics().size());
        assertEquals("NO_CONFIDENCE", harness.engine.getDiagnostics().get(0).getDecision());
    }

    @Test
    public void recordsMovementToRockOpportunityWhenPlayerRelocatesBeforeClick()
    {
        final Harness harness = new Harness();

        harness.accept(regionEvent(200, 11410, playerLocation(2838, 9388, 11410), "LOGGED_IN"));
        harness.accept(availableRockEvent(200, 11380, rockLocation()));
        harness.accept(movementEvent(201, playerLocation(2838, 9388, 11410), playerLocation(2840, 9388, 11410)));
        harness.accept(mineClickEvent(202, rockLocation()));
        harness.accept(miningAnimationEvent(203, 624));

        final OpportunityMarker movement = harness.completedOpportunity(GemMiningState.OPPORTUNITY_MOVEMENT_TO_ROCK);
        assertEquals(202, movement.getTime().getGameTick());
        assertEquals("2838", movement.getContext().get("fromX"));
        assertEquals("2840", movement.getContext().get("toX"));
    }

    @Test
    public void emitsReusableGearSwitchOpportunity()
    {
        final Harness harness = new Harness();

        harness.accept(regionEvent(200, 11410, playerLocation(2840, 9388, 11410), "LOGGED_IN"));
        harness.accept(availableRockEvent(200, 11380, rockLocation()));
        harness.accept(mineClickEvent(202, rockLocation()));
        harness.accept(gearSwitchEvent(203, 11802, 11804));
        harness.accept(miningAnimationEvent(204, 624));

        final OpportunityMarker gear = harness.completedOpportunity(
            GearSwitchTracker.ID + "." + GearSwitchTracker.OPPORTUNITY_GEAR_SWITCH);
        assertEquals(203, gear.getTime().getGameTick());
        assertEquals("1", gear.getContext().get("deltaCount"));
    }

    private static final class Harness
    {
        private final List<ActivityMarker> activityMarkers = new ArrayList<>();
        private final List<OpportunityMarker> opportunityMarkers = new ArrayList<>();
        private final ActivityStrategyEngine engine;

        private Harness()
        {
            final ActivityRegistry registry = ActivityRegistry.builder()
                .register(new GemMiningStrategy())
                .build();
            engine = new ActivityStrategyEngine(registry, activityMarkers::add, opportunityMarkers::add, true);
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
            throw new AssertionError("No completed opportunity marker for " + type);
        }
    }

    private static TelemetryEnvelope regionEvent(int tick, int regionId, WorldLocation location, String gameState)
    {
        return envelope(
            "region-" + tick,
            new RegionInstanceTelemetryEvent(time(tick), Collections.singletonMap("source", "WorldViewLoaded"), 301, regionId, "top", false, gameState, location));
    }

    private static TelemetryEnvelope availableRockEvent(int tick, int objectId, WorldLocation location)
    {
        return envelope(
            "rock-available-" + tick,
            new ObjectStateTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "ObjectSnapshot"),
                objectId,
                "Gem rock",
                location,
                "GAME_OBJECT",
                Arrays.asList("Mine", "Prospect"),
                StateChanges.AVAILABLE));
    }

    private static TelemetryEnvelope depletedRockEvent(int tick, int objectId, WorldLocation location)
    {
        return envelope(
            "rock-depleted-" + tick,
            new ObjectStateTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "ObjectSnapshot"),
                objectId,
                "Gem rock",
                location,
                "GAME_OBJECT",
                Arrays.asList("Mine", "Prospect"),
                StateChanges.DEPLETED));
    }

    private static TelemetryEnvelope mineClickEvent(int tick, WorldLocation location)
    {
        return envelope(
            "mine-click-" + tick,
            new PlayerActionTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "MenuOptionClicked"),
                "Mine",
                "Gem rock",
                EntityRef.unknown(),
                "GAME_OBJECT_FIRST_OPTION",
                location,
                1001));
    }

    private static TelemetryEnvelope movementEvent(int tick, WorldLocation fromLocation, WorldLocation toLocation)
    {
        return envelope(
            "move-" + tick,
            new MovementTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "MovementSnapshot"),
                EntityRef.localPlayer(),
                fromLocation,
                toLocation,
                "WALK",
                2));
    }

    private static TelemetryEnvelope miningAnimationEvent(int tick, int animationId)
    {
        return envelope(
            "animation-" + tick,
            new AnimationTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "AnimationChanged"),
                EntityRef.localPlayer(),
                animationId,
                -1));
    }

    private static TelemetryEnvelope gearSwitchEvent(int tick, int beforeItemId, int afterItemId)
    {
        return envelope(
            "gear-switch-" + tick,
            new InventoryDeltaTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "ItemContainerChanged"),
                94,
                Collections.singletonList(new InventoryDeltaTelemetryEvent.ItemDelta(3, beforeItemId, 1, afterItemId, 1))));
    }

    private static TelemetryEnvelope envelope(String eventId, com.ticksense.telemetry.TelemetryEvent event)
    {
        return TelemetryEnvelope.create(eventId, "session-gem", event);
    }

    private static EventTime time(int tick)
    {
        final long wallTimeMillis = 1_000L + (tick * 600L);
        return new EventTime(wallTimeMillis, wallTimeMillis * 1_000_000L, tick, tick * 20L, tick);
    }

    private static WorldLocation rockLocation()
    {
        return playerLocation(2841, 9388, 11410);
    }

    private static WorldLocation playerLocation(int x, int y, int regionId)
    {
        return new WorldLocation(301, 0, x, y, regionId, false);
    }
}
