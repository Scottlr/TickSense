package com.ticksense.replay;

import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.AnimationTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.MovementTelemetryEvent;
import com.ticksense.telemetry.events.ObjectStateTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import com.ticksense.telemetry.events.StatChangedTelemetryEvent;
import java.util.Arrays;
import java.util.Collections;

public final class TestEvents
{
    private final String sessionId;

    public TestEvents(String sessionId)
    {
        this.sessionId = sessionId == null ? "test-session" : sessionId;
    }

    public TelemetryEnvelope region(int tick, int regionId, WorldLocation location, String gameState)
    {
        return envelope(
            "region-" + tick,
            new RegionInstanceTelemetryEvent(time(tick), Collections.singletonMap("source", "WorldViewLoaded"), 301, regionId, "top", false, gameState, location));
    }

    public TelemetryEnvelope availableRock(int tick, int objectId, WorldLocation location)
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
                "AVAILABLE"));
    }

    public TelemetryEnvelope depletedRock(int tick, int objectId, WorldLocation location)
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
                "DEPLETED"));
    }

    public TelemetryEnvelope mineClick(int tick, WorldLocation location)
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

    public TelemetryEnvelope movement(int tick, WorldLocation fromLocation, WorldLocation toLocation)
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

    public TelemetryEnvelope miningAnimation(int tick, int animationId)
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

    public TelemetryEnvelope miningXp(int tick, int xp, int xpDelta)
    {
        return envelope(
            "mining-xp-" + tick,
            new StatChangedTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "StatChanged"),
                "MINING",
                xp,
                xpDelta,
                72,
                72));
    }

    public TelemetryEnvelope inventoryGain(int tick, int slot, int itemId)
    {
        return envelope(
            "inventory-gain-" + tick,
            new InventoryDeltaTelemetryEvent(
                time(tick),
                Collections.singletonMap("source", "ItemContainerChanged"),
                93,
                Collections.singletonList(new InventoryDeltaTelemetryEvent.ItemDelta(slot, -1, 0, itemId, 1))));
    }

    public static WorldLocation location(int x, int y, int regionId)
    {
        return new WorldLocation(301, 0, x, y, regionId, false);
    }

    private TelemetryEnvelope envelope(String eventId, TelemetryEvent event)
    {
        return TelemetryEnvelope.create(eventId, sessionId, event);
    }

    private static EventTime time(int tick)
    {
        final long wallTimeMillis = 1_000L + (tick * 600L);
        return new EventTime(wallTimeMillis, wallTimeMillis * 1_000_000L, tick, tick * 20L, tick);
    }
}
