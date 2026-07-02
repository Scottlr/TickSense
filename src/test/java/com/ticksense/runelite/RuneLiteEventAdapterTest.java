package com.ticksense.runelite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ticksense.core.EntityRef;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.events.AnimationTelemetryEvent;
import com.ticksense.telemetry.events.DamageTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.MenuInteractionTelemetryEvent;
import com.ticksense.telemetry.events.MovementTelemetryEvent;
import com.ticksense.telemetry.events.NpcStateTelemetryEvent;
import com.ticksense.telemetry.events.ObjectStateTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import com.ticksense.telemetry.events.StatChangedTelemetryEvent;
import com.ticksense.telemetry.events.WidgetTelemetryEvent;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.runelite.api.Actor;
import net.runelite.api.GameState;
import net.runelite.api.Hitsplat;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.events.WorldViewLoaded;
import org.junit.Test;

public class RuneLiteEventAdapterTest
{
    @Test
    public void mapsMenuOptionClickedToPlayerAction()
    {
        final NPC npc = npc(42, 12_345, "Araxxor spider", new WorldPoint(3200, 3201, 0));
        final MenuEntry menuEntry = menuEntry("Attack", "Araxxor spider", 12_345, 1, 2, MenuAction.NPC_FIRST_OPTION, npc);
        final RuneLiteEventAdapter adapter = adapter();

        final TelemetryEnvelope envelope = adapter.mapMenuOptionClicked(new MenuOptionClicked(menuEntry), envelope("MenuOptionClicked")).get();

        assertTrue(envelope.getEvent() instanceof PlayerActionTelemetryEvent);
        final PlayerActionTelemetryEvent event = (PlayerActionTelemetryEvent) envelope.getEvent();
        assertEquals("Attack", event.getOption());
        assertEquals("Araxxor spider", event.getTarget());
        assertEquals(EntityRef.npc(42, 12_345, "Araxxor spider"), event.getTargetRef());
        assertEquals("NPC_FIRST_OPTION", event.getActionKind());
        assertEquals(12_345, event.getMenuActionId());
        assertEnvelopeContext(envelope, "MenuOptionClicked");
    }

    @Test
    public void mapsMenuEntryAndOpenedToMenuInteraction()
    {
        final NPC npc = npc(42, 12_345, "Araxxor spider", new WorldPoint(3200, 3201, 0));
        final MenuEntry entry = menuEntry("Attack", "Araxxor spider", 12_345, 3, 4, MenuAction.NPC_FIRST_OPTION, npc);
        final RuneLiteEventAdapter adapter = adapter();

        final TelemetryEnvelope addedEnvelope = adapter.mapMenuEntryAdded(new MenuEntryAdded(entry), envelope("MenuEntryAdded")).get();
        final MenuInteractionTelemetryEvent added = (MenuInteractionTelemetryEvent) addedEnvelope.getEvent();
        assertEquals("ENTRY_ADDED", added.getInteractionType());
        assertEquals(Collections.singletonList("Attack Araxxor spider"), added.getEntries());
        assertEquals(12_345, added.getIdentifier());

        final MenuOpened menuOpened = new MenuOpened();
        menuOpened.setMenuEntries(new MenuEntry[] { entry });
        final TelemetryEnvelope openedEnvelope = adapter.mapMenuOpened(menuOpened, envelope("MenuOpened")).get();
        final MenuInteractionTelemetryEvent opened = (MenuInteractionTelemetryEvent) openedEnvelope.getEvent();
        assertEquals("OPENED", opened.getInteractionType());
        assertEquals(Collections.singletonList("Attack Araxxor spider"), opened.getEntries());
    }

    @Test
    public void mapsNpcSpawnedToNpcStateSnapshot()
    {
        final NPC npc = npc(7, 999, "Araxxor", new WorldPoint(3210, 3220, 1));

        final TelemetryEnvelope envelope = adapter().mapNpcSpawned(new NpcSpawned(npc), envelope("NpcSpawned")).get();

        assertTrue(envelope.getEvent() instanceof NpcStateTelemetryEvent);
        final NpcStateTelemetryEvent event = (NpcStateTelemetryEvent) envelope.getEvent();
        assertEquals(EntityRef.npc(7, 999, "Araxxor"), event.getNpcRef());
        assertEquals("SPAWNED", event.getStateChange());
        assertEquals(new WorldLocation(301, 1, 3210, 3220, 12_850, false), event.getLocation());
        assertEquals(812, event.getAnimationId());
        assertEquals(301, event.getGraphicId());
        assertEnvelopeContext(envelope, "NpcSpawned");
    }

    @Test
    public void mapsNpcLifecycleEventsToNpcStateSnapshots()
    {
        final RuneLiteEventAdapter adapter = adapter();
        final NPC npc = npc(7, 999, "Araxxor", new WorldPoint(3210, 3220, 1));

        final NpcStateTelemetryEvent despawned = (NpcStateTelemetryEvent) adapter
            .mapNpcDespawned(new NpcDespawned(npc), envelope("NpcDespawned"))
            .get()
            .getEvent();
        assertEquals("DESPAWNED", despawned.getStateChange());

        final NpcStateTelemetryEvent changed = (NpcStateTelemetryEvent) adapter
            .mapNpcChanged(new NpcChanged(npc, null), envelope("NpcChanged"))
            .get()
            .getEvent();
        assertEquals("CHANGED", changed.getStateChange());
    }

    @Test
    public void mapsItemContainerChangedToInventoryDelta()
    {
        final RuneLiteEventAdapter adapter = adapter();
        adapter.mapItemContainerChanged(
            new ItemContainerChanged(93, itemContainer(new Item[] { new Item(100, 1) })),
            envelope("ItemContainerChanged"));

        final TelemetryEnvelope envelope = adapter.mapItemContainerChanged(
            new ItemContainerChanged(93, itemContainer(new Item[] { new Item(101, 2) })),
            envelope("ItemContainerChanged")).get();

        assertTrue(envelope.getEvent() instanceof InventoryDeltaTelemetryEvent);
        final InventoryDeltaTelemetryEvent event = (InventoryDeltaTelemetryEvent) envelope.getEvent();
        assertEquals(93, event.getContainerId());
        assertEquals(1, event.getDeltas().size());
        final InventoryDeltaTelemetryEvent.ItemDelta delta = event.getDeltas().get(0);
        assertEquals(0, delta.getSlot());
        assertEquals(100, delta.getBeforeItemId());
        assertEquals(1, delta.getBeforeQuantity());
        assertEquals(101, delta.getAfterItemId());
        assertEquals(2, delta.getAfterQuantity());
    }

    @Test
    public void mapsStatDamageAnimationAndWidgetCloseEvidence()
    {
        final RuneLiteEventAdapter adapter = adapter();

        final StatChangedTelemetryEvent stat = (StatChangedTelemetryEvent) adapter
            .mapStatChanged(new StatChanged(Skill.MINING, 12_345, 71, 72), envelope("StatChanged"))
            .get()
            .getEvent();
        assertEquals("MINING", stat.getSkill());
        assertEquals(12_345, stat.getXp());

        final AnimationChanged animationChanged = new AnimationChanged();
        animationChanged.setActor(npc(2, 111, "Spider", new WorldPoint(3200, 3200, 0)));
        final AnimationTelemetryEvent animation = (AnimationTelemetryEvent) adapter
            .mapAnimationChanged(animationChanged, envelope("AnimationChanged"))
            .get()
            .getEvent();
        assertEquals(812, animation.getAnimationId());

        final HitsplatApplied hitsplatApplied = new HitsplatApplied();
        hitsplatApplied.setActor(npc(3, 222, "Target", new WorldPoint(3201, 3201, 0)));
        hitsplatApplied.setHitsplat(hitsplat(1, 18));
        final DamageTelemetryEvent damage = (DamageTelemetryEvent) adapter
            .mapHitsplatApplied(hitsplatApplied, envelope("HitsplatApplied"))
            .get()
            .getEvent();
        assertEquals(1, damage.getHitsplatType());
        assertEquals(18, damage.getAmount());

        final WidgetClosed widgetClosed = new WidgetClosed(12, 0, true);
        final WidgetTelemetryEvent widget = (WidgetTelemetryEvent) adapter
            .mapWidgetClosed(widgetClosed, envelope("WidgetClosed"))
            .get()
            .getEvent();
        assertEquals(12, widget.getGroupId());
        assertEquals("UNLOADED", widget.getEventKind());
    }

    @Test
    public void mapsWidgetLoadedToWidgetTelemetry()
    {
        final WidgetLoaded widgetLoaded = new WidgetLoaded();
        widgetLoaded.setGroupId(12);

        final TelemetryEnvelope envelope = adapter().mapWidgetLoaded(widgetLoaded, envelope("WidgetLoaded")).get();

        assertTrue(envelope.getEvent() instanceof WidgetTelemetryEvent);
        final WidgetTelemetryEvent event = (WidgetTelemetryEvent) envelope.getEvent();
        assertEquals(12, event.getGroupId());
        assertEquals("LOADED", event.getEventKind());
        assertTrue(event.isVisible());
    }

    @Test
    public void mapsWorldViewLoadedToRegionInstanceTelemetry()
    {
        final WorldView worldView = proxy(WorldView.class, values(
            "getId", 77,
            "isInstance", true,
            "getMapRegions", new int[] { 12_580 },
            "getPlane", 1,
            "getBaseX", 3210,
            "getBaseY", 3220));

        final TelemetryEnvelope envelope = adapter().mapWorldViewLoaded(new WorldViewLoaded(worldView), envelope("WorldViewLoaded")).get();

        assertTrue(envelope.getEvent() instanceof RegionInstanceTelemetryEvent);
        final RegionInstanceTelemetryEvent event = (RegionInstanceTelemetryEvent) envelope.getEvent();
        assertEquals(301, event.getWorld());
        assertEquals(12_580, event.getRegionId());
        assertEquals("77", event.getWorldViewId());
        assertTrue(event.isInstanced());
        assertEquals(new WorldLocation(301, 1, 3210, 3220, 12_580, true), event.getLocalPlayerLocation());
    }

    @Test
    public void mapsGameStateChangedToRegionTelemetry()
    {
        final GameStateChanged gameStateChanged = new GameStateChanged();
        gameStateChanged.setGameState(GameState.LOGGED_IN);

        final TelemetryEnvelope envelope = adapter().mapGameStateChanged(gameStateChanged, envelope("GameStateChanged")).get();

        assertTrue(envelope.getEvent() instanceof RegionInstanceTelemetryEvent);
        final RegionInstanceTelemetryEvent event = (RegionInstanceTelemetryEvent) envelope.getEvent();
        assertEquals("LOGGED_IN", event.getGameState());
        assertEquals(WorldLocation.unknown(), event.getLocalPlayerLocation());
        assertEnvelopeContext(envelope, "GameStateChanged");
    }

    @Test
    public void objectAndMovementHelpersProduceTelemetry()
    {
        final RuneLiteEventAdapter adapter = adapter();
        final WorldLocation from = new WorldLocation(301, 0, 3200, 3200, 12_580, false);
        final WorldLocation to = new WorldLocation(301, 0, 3201, 3201, 12_580, false);

        final ObjectStateTelemetryEvent object = (ObjectStateTelemetryEvent) adapter.mapObjectSnapshot(
            "ObjectSnapshot",
            1001,
            "Gem rock",
            from,
            "GAME_OBJECT",
            Arrays.asList("Mine", "Prospect"),
            "AVAILABLE",
            envelope("ObjectSnapshot")).get().getEvent();
        assertEquals(1001, object.getObjectId());
        assertEquals(Arrays.asList("Mine", "Prospect"), object.getActions());

        final MovementTelemetryEvent movement = (MovementTelemetryEvent) adapter.mapMovementSnapshot(
            "MovementSnapshot",
            EntityRef.localPlayer(),
            from,
            to,
            "WALK",
            2,
            envelope("MovementSnapshot")).get().getEvent();
        assertEquals(EntityRef.localPlayer(), movement.getEntityRef());
        assertEquals(to, movement.getToLocation());
        assertEquals(Integer.valueOf(2), movement.getDistanceTiles());
    }

    private static RuneLiteEventAdapter adapter()
    {
        return new RuneLiteEventAdapter(new RuneLiteSnapshotter(), "test-session");
    }

    private static RuneLiteEventEnvelope envelope(String source)
    {
        return new RuneLiteEventEnvelope(source, 1_000L, 2_000L, 10, 20, 30, 301, 50, "LOGGED_IN");
    }

    private static void assertEnvelopeContext(TelemetryEnvelope envelope, String source)
    {
        assertEquals("test-session", envelope.getSessionId());
        assertTrue(envelope.getEventId().startsWith(source + "-"));
        assertEquals(source, envelope.getEvent().getTags().get("source"));
        assertEquals(10, envelope.getEvent().getTime().getGameTick());
        assertEquals(20L, envelope.getEvent().getTime().getClientCycle());
        assertEquals(30, envelope.getEvent().getTime().getClientTickSequence());
    }

    private static NPC npc(int index, int id, String name, WorldPoint worldPoint)
    {
        return proxy(NPC.class, values(
            "getIndex", index,
            "getId", id,
            "getName", name,
            "getWorldLocation", worldPoint,
            "getAnimation", 812,
            "getGraphic", 301,
            "getHealthRatio", 25,
            "getHealthScale", 30,
            "getInteracting", null));
    }

    private static Hitsplat hitsplat(int type, int amount)
    {
        return proxy(Hitsplat.class, values("getHitsplatType", type, "getAmount", amount));
    }

    private static ItemContainer itemContainer(Item[] items)
    {
        return proxy(ItemContainer.class, values("getItems", items));
    }

    private static MenuEntry menuEntry(
        String option,
        String target,
        int identifier,
        int param0,
        int param1,
        MenuAction type,
        Actor actor)
    {
        return proxy(MenuEntry.class, values(
            "getOption", option,
            "getTarget", target,
            "getIdentifier", identifier,
            "getParam0", param0,
            "getParam1", param1,
            "getType", type,
            "getNpc", actor instanceof NPC ? actor : null,
            "getActor", actor,
            "getWidget", null,
            "getItemId", -1,
            "getItemOp", -1,
            "isItemOp", false));
    }

    private static Map<String, Object> values(Object... keysAndValues)
    {
        final Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2)
        {
            values.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Map<String, Object> values)
    {
        return (T) Proxy.newProxyInstance(
            type.getClassLoader(),
            new Class<?>[] { type },
            (proxy, method, args) -> {
                if ("toString".equals(method.getName()))
                {
                    return type.getSimpleName() + values;
                }
                if ("hashCode".equals(method.getName()))
                {
                    return System.identityHashCode(proxy);
                }
                if ("equals".equals(method.getName()))
                {
                    return proxy == args[0];
                }
                if (values.containsKey(method.getName()))
                {
                    return values.get(method.getName());
                }
                return defaultValue(method.getReturnType());
            });
    }

    private static Object defaultValue(Class<?> returnType)
    {
        if (returnType == Boolean.TYPE)
        {
            return false;
        }
        if (returnType == Byte.TYPE)
        {
            return (byte) 0;
        }
        if (returnType == Short.TYPE)
        {
            return (short) 0;
        }
        if (returnType == Integer.TYPE)
        {
            return 0;
        }
        if (returnType == Long.TYPE)
        {
            return 0L;
        }
        if (returnType == Float.TYPE)
        {
            return 0.0F;
        }
        if (returnType == Double.TYPE)
        {
            return 0.0D;
        }
        if (returnType == Character.TYPE)
        {
            return '\0';
        }
        return null;
    }
}
