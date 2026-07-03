package com.ticksense.activities.construction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.ActivityRegistry;
import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.ActivityStrategyEngine;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.StateChanges;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.AnimationTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.MenuInteractionTelemetryEvent;
import com.ticksense.telemetry.events.ObjectStateTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import com.ticksense.telemetry.events.StatChangedTelemetryEvent;
import com.ticksense.telemetry.events.WidgetTelemetryEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class ConstructionStrategyTest
{
    @Test
    public void recordsMenuLatency()
    {
        final Harness harness = new Harness();

        harness.accept(regionEvent(400, playerLocation()));
        harness.accept(buildSpotEvent(400, StateChanges.AVAILABLE));
        harness.accept(menuOpenedEvent(401, "Build", "Larder space", buildLocation()));
        harness.accept(buildClickEvent(402));
        harness.accept(constructionWidgetEvent(403, 458, 12, "Oak larder"));

        final OpportunityMarker marker = harness.completedOpportunity(ConstructionState.OPPORTUNITY_MENU_LATENCY);
        assertEquals(402, marker.getTime().getGameTick());
        assertEquals("Build", marker.getContext().get("option"));
        assertEquals("Larder space", marker.getContext().get("target"));
    }

    @Test
    public void recordsInventoryCycleDuration()
    {
        final Harness harness = new Harness();

        harness.accept(regionEvent(400, playerLocation()));
        harness.accept(buildSpotEvent(400, StateChanges.AVAILABLE));
        harness.accept(menuOpenedEvent(401, "Build", "Larder space", buildLocation()));
        harness.accept(buildClickEvent(402));
        harness.accept(constructionWidgetEvent(403, 458, 12, "Oak larder"));
        harness.accept(animationEvent(404, 3676));
        harness.accept(inventoryDeltaEvent(405, 8778, -1, 0));
        harness.accept(constructionXpEvent(406, 480));
        harness.accept(builtLarderEvent(407));
        harness.accept(menuOpenedEvent(415, "Remove", "Oak larder", buildLocation()));
        harness.accept(removeClickEvent(416));
        harness.accept(buildSpotEvent(417, StateChanges.AVAILABLE));
        harness.accept(bankWidgetEvent(439));

        assertFalse(harness.engine.getActiveSession().isPresent());
        assertEquals(1, harness.engine.getCompletedActivityData().size());

        final OpportunityMarker cycle = harness.completedOpportunity(ConstructionState.OPPORTUNITY_INVENTORY_CYCLE);
        final OpportunityMarker banking = harness.completedOpportunity(ConstructionState.OPPORTUNITY_BANKING_DOWNTIME);
        final OpportunityMarker cadence = harness.completedOpportunity(ConstructionState.OPPORTUNITY_BUILD_REMOVE_CADENCE);
        assertEquals(439, cycle.getTime().getGameTick());
        assertEquals(439, banking.getTime().getGameTick());
        assertEquals(416, cadence.getTime().getGameTick());

        final ActivityReportData data = harness.engine.getCompletedActivityData().get(0);
        assertEquals("1", data.getAttributes().get("inventoryCycleCount"));
        assertEquals("1", data.getAttributes().get("bankingDowntimeCount"));
        assertEquals("1", data.getAttributes().get("buildRemoveCadenceCount"));
    }

    @Test
    public void doesNotMutateMenuEntries()
    {
        final Harness harness = new Harness();
        final List<String> entries = new ArrayList<>();
        entries.add("Build <col=ff9040>Oak larder");

        harness.accept(regionEvent(400, playerLocation()));
        harness.accept(buildSpotEvent(400, StateChanges.AVAILABLE));
        harness.accept(envelope(
            "menu-open-mutable",
            new MenuInteractionTelemetryEvent(
                time(401),
                Collections.singletonMap("source", "MenuOpened"),
                "MenuOpened",
                entries,
                "Build",
                "Larder space",
                15403,
                3110,
                3497,
                EntityRef.unknown())));

        assertEquals(1, entries.size());
        assertEquals("Build <col=ff9040>Oak larder", entries.get(0));
    }

    private static final class Harness
    {
        private final List<ActivityMarker> activityMarkers = new ArrayList<>();
        private final List<OpportunityMarker> opportunityMarkers = new ArrayList<>();
        private final ActivityStrategyEngine engine;

        private Harness()
        {
            final ActivityRegistry registry = ActivityRegistry.builder()
                .register(new ConstructionStrategy())
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

    private static TelemetryEnvelope regionEvent(int tick, WorldLocation location)
    {
        return envelope(
            "region-" + tick,
            new RegionInstanceTelemetryEvent(time(tick), Collections.singletonMap("source", "WorldViewLoaded"), 330, location.getRegionId(), "top", false, "LOGGED_IN", location));
    }

    private static TelemetryEnvelope buildSpotEvent(int tick, String stateChange)
    {
        return envelope(
            "build-spot-" + tick + "-" + stateChange,
            new ObjectStateTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "ObjectSnapshot"),
                15403,
                "Larder space",
                buildLocation(),
                "GAME_OBJECT",
                Collections.singletonList("Build"),
                stateChange));
    }

    private static TelemetryEnvelope builtLarderEvent(int tick)
    {
        return envelope(
            "built-larder-" + tick,
            new ObjectStateTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "ObjectSnapshot"),
                13565,
                "Oak larder",
                buildLocation(),
                "GAME_OBJECT",
                Collections.singletonList("Remove"),
                StateChanges.BUILT));
    }

    private static TelemetryEnvelope menuOpenedEvent(int tick, String option, String target, WorldLocation location)
    {
        return envelope(
            "menu-open-" + tick,
            new MenuInteractionTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "MenuOpened"),
                "MenuOpened",
                Collections.singletonList(option + " <col=ff9040>" + target),
                option,
                target,
                "Build".equals(option) ? 15403 : 13565,
                location.getX(),
                location.getY(),
                EntityRef.unknown()));
    }

    private static TelemetryEnvelope buildClickEvent(int tick)
    {
        return envelope(
            "build-click-" + tick,
            new PlayerActionTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "MenuOptionClicked"),
                "Build",
                "Larder space",
                EntityRef.unknown(),
                "GAME_OBJECT_FIRST_OPTION",
                buildLocation(),
                1001));
    }

    private static TelemetryEnvelope removeClickEvent(int tick)
    {
        return envelope(
            "remove-click-" + tick,
            new PlayerActionTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "MenuOptionClicked"),
                "Remove",
                "Oak larder",
                EntityRef.unknown(),
                "GAME_OBJECT_SECOND_OPTION",
                buildLocation(),
                1002));
    }

    private static TelemetryEnvelope constructionWidgetEvent(int tick, int groupId, int childId, String text)
    {
        return envelope(
            "construction-widget-" + tick,
            new WidgetTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "WidgetLoaded"),
                groupId,
                childId,
                8778,
                text,
                "WidgetLoaded",
                true,
                Collections.singletonList("Build")));
    }

    private static TelemetryEnvelope bankWidgetEvent(int tick)
    {
        return envelope(
            "bank-widget-" + tick,
            new WidgetTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "WidgetLoaded"),
                12,
                0,
                -1,
                "The Bank of Gielinor",
                "WidgetLoaded",
                true,
                Collections.emptyList()));
    }

    private static TelemetryEnvelope animationEvent(int tick, int animationId)
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

    private static TelemetryEnvelope inventoryDeltaEvent(int tick, int beforeItemId, int afterItemId, int afterQuantity)
    {
        return envelope(
            "inventory-" + tick,
            new InventoryDeltaTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "ItemContainerChanged"),
                93,
                Collections.singletonList(new InventoryDeltaTelemetryEvent.ItemDelta(4, beforeItemId, 8, afterItemId, afterQuantity))));
    }

    private static TelemetryEnvelope constructionXpEvent(int tick, int xpDelta)
    {
        return envelope(
            "construction-xp-" + tick,
            new StatChangedTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "StatChanged"),
                "CONSTRUCTION",
                800000,
                xpDelta,
                74,
                74));
    }

    private static TelemetryEnvelope envelope(String eventId, TelemetryEvent event)
    {
        return TelemetryEnvelope.create(eventId, "session-construction", event);
    }

    private static EventTime time(int tick)
    {
        final long wallTimeMillis = 1_000L + (tick * 600L);
        return new EventTime(wallTimeMillis, wallTimeMillis * 1_000_000L, tick, tick * 20L, tick);
    }

    private static WorldLocation buildLocation()
    {
        return new WorldLocation(330, 0, 3110, 3497, 7513, false);
    }

    private static WorldLocation playerLocation()
    {
        return new WorldLocation(330, 0, 3108, 3498, 7513, false);
    }
}
