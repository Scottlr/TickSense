package com.ticksense.telemetry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.events.AnimationTelemetryEvent;
import com.ticksense.telemetry.events.ClientTickTelemetryEvent;
import com.ticksense.telemetry.events.DamageTelemetryEvent;
import com.ticksense.telemetry.events.EnvironmentTelemetryEvent;
import com.ticksense.telemetry.events.GameTickTelemetryEvent;
import com.ticksense.telemetry.events.GraphicTelemetryEvent;
import com.ticksense.telemetry.events.InteractingChangedTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.MenuInteractionTelemetryEvent;
import com.ticksense.telemetry.events.MovementTelemetryEvent;
import com.ticksense.telemetry.events.NpcStateTelemetryEvent;
import com.ticksense.telemetry.events.ObjectStateTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import com.ticksense.telemetry.events.ProjectileTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import com.ticksense.telemetry.events.StatChangedTelemetryEvent;
import com.ticksense.telemetry.events.WidgetTelemetryEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class TelemetryJsonTest
{
    @Test
    public void roundTripsPlayerActionEvent()
    {
        final EntityRef target = EntityRef.npc(42, 12_345, "Araxxor spider");
        final WorldLocation location = location();

        final TelemetryEnvelope envelope = roundTrip(new PlayerActionTelemetryEvent(
            time(),
            tags("MenuOptionClicked"),
            "Attack",
            "Araxxor spider",
            target,
            "NPC",
            location,
            11));

        assertTrue(envelope.getEvent() instanceof PlayerActionTelemetryEvent);
        final PlayerActionTelemetryEvent event = (PlayerActionTelemetryEvent) envelope.getEvent();
        assertEquals("Attack", event.getOption());
        assertEquals("Araxxor spider", event.getTarget());
        assertEquals(target, event.getTargetRef());
        assertEquals("NPC", event.getActionKind());
        assertEquals(location, event.getLocation());
        assertEquals(11, event.getMenuActionId());
    }

    @Test
    public void roundTripsNpcStateEvent()
    {
        final EntityRef npcRef = EntityRef.npc(7, 999, "Araxxor");
        final EntityRef interactingRef = EntityRef.localPlayer();

        final TelemetryEnvelope envelope = roundTrip(new NpcStateTelemetryEvent(
            time(),
            tags("NpcSpawned"),
            npcRef,
            "SPAWNED",
            location(),
            812,
            301,
            interactingRef,
            25,
            30));

        assertTrue(envelope.getEvent() instanceof NpcStateTelemetryEvent);
        final NpcStateTelemetryEvent event = (NpcStateTelemetryEvent) envelope.getEvent();
        assertEquals(npcRef, event.getNpcRef());
        assertEquals("SPAWNED", event.getStateChange());
        assertEquals(812, event.getAnimationId());
        assertEquals(301, event.getGraphicId());
        assertEquals(interactingRef, event.getInteractingRef());
        assertEquals(25, event.getHealthRatio());
        assertEquals(30, event.getHealthScale());
    }

    @Test
    public void roundTripsRepresentativeEventsAcrossCategories()
    {
        final Set<TelemetryCategory> categories = EnumSet.noneOf(TelemetryCategory.class);
        final TelemetryEvent[] events = new TelemetryEvent[] {
            new GameTickTelemetryEvent(time(), tags("GameTick"), 123),
            new ClientTickTelemetryEvent(time(), tags("ClientTick"), 456),
            new MenuInteractionTelemetryEvent(
                time(),
                tags("MenuOpened"),
                "OPENED",
                Arrays.asList("Attack", "Walk here"),
                "Attack",
                "Araxxor",
                1,
                2,
                3,
                EntityRef.widget(4, 5)),
            new ObjectStateTelemetryEvent(
                time(),
                tags("MenuEntryAdded"),
                1001,
                "Gem rock",
                location(),
                "GAME_OBJECT",
                Arrays.asList("Mine", "Prospect"),
                "AVAILABLE"),
            new NpcStateTelemetryEvent(
                time(),
                tags("NpcChanged"),
                EntityRef.npc(6, 111, "Araxxor"),
                "CHANGED",
                location(),
                400,
                500,
                EntityRef.unknown(),
                20,
                30),
            new InteractingChangedTelemetryEvent(
                time(),
                tags("InteractingChanged"),
                EntityRef.localPlayer(),
                EntityRef.npc(8, 222, "Target"),
                "TARGET_CHANGED"),
            new AnimationTelemetryEvent(time(), tags("AnimationChanged"), EntityRef.localPlayer(), 624, -1),
            new GraphicTelemetryEvent(time(), tags("GraphicChanged"), EntityRef.localPlayer(), 86, location()),
            new ProjectileTelemetryEvent(
                time(),
                tags("ProjectileMoved"),
                1477,
                EntityRef.npc(9, 333, "Source"),
                EntityRef.localPlayer(),
                location(),
                10,
                20),
            new DamageTelemetryEvent(time(), tags("HitsplatApplied"), EntityRef.localPlayer(), 1, 18, 12, 99),
            new InventoryDeltaTelemetryEvent(
                time(),
                tags("ItemContainerChanged"),
                93,
                Collections.singletonList(new InventoryDeltaTelemetryEvent.ItemDelta(0, 100, 1, 101, 1, Collections.singletonList("Eat")))),
            new StatChangedTelemetryEvent(time(), tags("StatChanged"), "Mining", 12_345, 50, 71, 72),
            new MovementTelemetryEvent(
                time(),
                tags("MovementSnapshot"),
                EntityRef.localPlayer(),
                new WorldLocation(301, 0, 3200, 3201, 12_550, false),
                new WorldLocation(301, 0, 3202, 3203, 12_550, false),
                "WALK",
                4),
            new RegionInstanceTelemetryEvent(time(), tags("WorldViewLoaded"), 301, 12_550, "top", true, "LOGGED_IN", location()),
            new WidgetTelemetryEvent(
                time(),
                tags("WidgetLoaded"),
                12,
                34,
                56,
                "Bank",
                "LOADED",
                true,
                Arrays.asList("Collect", "Close")),
            new EnvironmentTelemetryEvent(time(), tags("ClientSnapshot"), 49, 301, "LOGGED_IN", "0.1.0")
        };

        for (TelemetryEvent event : events)
        {
            final TelemetryEnvelope envelope = roundTrip(event);
            categories.add(envelope.getEvent().getCategory());
        }

        assertEquals(EnumSet.allOf(TelemetryCategory.class), categories);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnknownSchemaVersion()
    {
        final String json = TelemetryJson.toJsonLine(TelemetryEnvelope.create(
            "event-schema",
            "session-1",
            new GameTickTelemetryEvent(time(), tags("GameTick"), 1)));

        TelemetryJson.fromJsonLine(json.replace("\"schemaVersion\":1", "\"schemaVersion\":999"));
    }

    private static TelemetryEnvelope roundTrip(TelemetryEvent event)
    {
        final TelemetryEnvelope envelope = TelemetryEnvelope.create("event-" + event.getType(), "session-1", event);
        final String json = TelemetryJson.toJsonLine(envelope);
        final TelemetryEnvelope parsed = TelemetryJson.fromJsonLine(json);

        assertEquals(envelope.getSchemaVersion(), parsed.getSchemaVersion());
        assertEquals(envelope.getEventId(), parsed.getEventId());
        assertEquals(envelope.getSessionId(), parsed.getSessionId());
        assertEquals(event.getType(), parsed.getEvent().getType());
        assertEquals(event.getCategory(), parsed.getEvent().getCategory());
        assertEquals(event.getTime(), parsed.getEvent().getTime());
        assertEquals(event.getTags(), parsed.getEvent().getTags());
        assertEquals(json, TelemetryJson.toJsonLine(parsed));
        return parsed;
    }

    private static EventTime time()
    {
        return new EventTime(1783010000123L, 987654321L, 8421, 512991L, 77);
    }

    private static WorldLocation location()
    {
        return new WorldLocation(301, 0, 3200, 3201, 12_550, false);
    }

    private static Map<String, String> tags(String source)
    {
        final Map<String, String> tags = new LinkedHashMap<>();
        tags.put("source", source);
        return tags;
    }
}
