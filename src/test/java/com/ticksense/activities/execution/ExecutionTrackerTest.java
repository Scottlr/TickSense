package com.ticksense.activities.execution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ticksense.activities.OpportunityLifecycle;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.events.DamageTelemetryEvent;
import com.ticksense.telemetry.events.InteractingChangedTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.MovementTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class ExecutionTrackerTest
{
    @Test
    public void foodRecoveryCompletesWhenFoodIsConsumedAfterDamage()
    {
        final Harness harness = new Harness(new FoodRecoveryTracker());

        harness.accept(new DamageTelemetryEvent(
            time(100),
            tags(),
            EntityRef.localPlayer(),
            1,
            22,
            45,
            99));
        harness.accept(new InventoryDeltaTelemetryEvent(
            time(101),
            tags(),
            93,
            Collections.singletonList(new InventoryDeltaTelemetryEvent.ItemDelta(0, 385, 1, -1, 0))));

        assertEquals(OpportunityStatus.OPEN, harness.markers.get(0).getStatus());
        assertEquals(OpportunityStatus.COMPLETED, harness.markers.get(1).getStatus());
        assertEquals("food-recovery.FOOD_RECOVERY", harness.markers.get(1).getOpportunityType());
    }

    @Test
    public void targetReengagementCompletesOnNpcAttackAfterTargetLoss()
    {
        final Harness harness = new Harness(new TargetReengagementTracker());

        harness.accept(new InteractingChangedTelemetryEvent(
            time(200),
            tags(),
            EntityRef.localPlayer(),
            EntityRef.unknown(),
            "TARGET_CLEARED"));
        harness.accept(new PlayerActionTelemetryEvent(
            time(201),
            tags(),
            "Attack",
            "Araxxor",
            EntityRef.npc(1, 13668, "Araxxor"),
            "NPC_FIRST_OPTION",
            location(),
            0));

        assertEquals(OpportunityStatus.OPEN, harness.markers.get(0).getStatus());
        assertEquals(OpportunityStatus.COMPLETED, harness.markers.get(1).getStatus());
        assertEquals("target-reengagement.TARGET_REENGAGEMENT", harness.markers.get(1).getOpportunityType());
    }

    @Test
    public void gearSwitchCompletesForEquipmentStyleReplacement()
    {
        final Harness harness = new Harness(new GearSwitchTracker());

        harness.accept(new InventoryDeltaTelemetryEvent(
            time(300),
            tags(),
            94,
            Collections.singletonList(new InventoryDeltaTelemetryEvent.ItemDelta(3, 11802, 1, 11804, 1))));

        assertEquals(2, harness.markers.size());
        assertEquals(OpportunityStatus.OPEN, harness.markers.get(0).getStatus());
        assertEquals(OpportunityStatus.COMPLETED, harness.markers.get(1).getStatus());
        assertEquals("gear-switch.GEAR_SWITCH", harness.markers.get(1).getOpportunityType());
    }

    @Test
    public void movementResponseCompletesAfterActivityOpensResponseWindow()
    {
        final MovementResponseTracker tracker = new MovementResponseTracker();
        final Harness harness = new Harness(tracker);

        tracker.openResponse(harness.session, time(400), Collections.singletonMap("cue", "blood-splat"));
        harness.accept(new MovementTelemetryEvent(
            time(401),
            tags(),
            EntityRef.localPlayer(),
            location(),
            new WorldLocation(330, 0, 3201, 3200, 12345, false),
            "STEP",
            1));

        assertEquals(OpportunityStatus.OPEN, harness.markers.get(0).getStatus());
        assertEquals(OpportunityStatus.COMPLETED, harness.markers.get(1).getStatus());
        assertEquals("movement-response.MOVEMENT_RESPONSE", harness.markers.get(1).getOpportunityType());
    }

    @Test
    public void prayerSwitchIsNoOpUntilPrayerTelemetryExists()
    {
        final Harness harness = new Harness(new PrayerSwitchTracker());

        harness.accept(new MovementTelemetryEvent(
            time(500),
            tags(),
            EntityRef.localPlayer(),
            location(),
            new WorldLocation(330, 0, 3201, 3200, 12345, false),
            "STEP",
            1));

        assertTrue(harness.markers.isEmpty());
    }

    @Test
    public void trackerSetDispatchesEventsToReusableTrackers()
    {
        final Harness harness = new Harness(ExecutionTrackerSet.of(
            "test-defaults",
            new FoodRecoveryTracker(),
            new TargetReengagementTracker()));

        harness.accept(new DamageTelemetryEvent(
            time(600),
            tags(),
            EntityRef.localPlayer(),
            1,
            12,
            50,
            99));
        harness.accept(new InventoryDeltaTelemetryEvent(
            time(601),
            tags(),
            93,
            Collections.singletonList(new InventoryDeltaTelemetryEvent.ItemDelta(0, 385, 1, -1, 0))));
        harness.accept(new InteractingChangedTelemetryEvent(
            time(602),
            tags(),
            EntityRef.localPlayer(),
            EntityRef.unknown(),
            "TARGET_CLEARED"));
        harness.accept(new PlayerActionTelemetryEvent(
            time(603),
            tags(),
            "Attack",
            "Araxxor",
            EntityRef.npc(1, 13668, "Araxxor"),
            "NPC_FIRST_OPTION",
            location(),
            0));

        assertEquals(4, harness.markers.size());
        assertEquals("food-recovery.FOOD_RECOVERY", harness.markers.get(1).getOpportunityType());
        assertEquals("target-reengagement.TARGET_REENGAGEMENT", harness.markers.get(3).getOpportunityType());
    }

    @Test
    public void combatSupportPresetIncludesFoodRecovery()
    {
        final Harness harness = new Harness(CommonExecutionTrackers.combatSupport());

        harness.accept(new DamageTelemetryEvent(
            time(700),
            tags(),
            EntityRef.localPlayer(),
            1,
            10,
            55,
            99));
        harness.accept(new InventoryDeltaTelemetryEvent(
            time(701),
            tags(),
            93,
            Collections.singletonList(new InventoryDeltaTelemetryEvent.ItemDelta(0, 385, 1, -1, 0))));

        assertEquals(2, harness.markers.size());
        assertEquals("food-recovery.FOOD_RECOVERY", harness.markers.get(1).getOpportunityType());
    }

    private static final class Harness
    {
        private final List<OpportunityMarker> markers = new ArrayList<>();
        private final ActivitySession session = session();
        private final ExecutionTracker tracker;

        private Harness(ExecutionTracker tracker)
        {
            this.tracker = tracker;
            this.tracker.startActivity(session.getActivityId());
            this.tracker.ensureOpportunityLifecycle(new OpportunityLifecycle(markers::add));
        }

        private void accept(com.ticksense.telemetry.TelemetryEvent event)
        {
            tracker.onEvent(null, session, event);
        }
    }

    private static ActivitySession session()
    {
        return new ActivitySession(
            ActivityId.of("activity-1"),
            ActivityType.ARAXXOR,
            time(1),
            null,
            null,
            Collections.emptyList(),
            Collections.emptyMap());
    }

    private static EventTime time(int tick)
    {
        final long wallTimeMillis = 1_000L + tick * 600L;
        return new EventTime(wallTimeMillis, wallTimeMillis * 1_000_000L, tick, tick * 20L, tick);
    }

    private static WorldLocation location()
    {
        return new WorldLocation(330, 0, 3200, 3200, 12345, false);
    }

    private static java.util.Map<String, String> tags()
    {
        return Collections.singletonMap("source", "test");
    }
}
