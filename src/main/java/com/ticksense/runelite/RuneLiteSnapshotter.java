package com.ticksense.runelite;

import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;

@Singleton
public final class RuneLiteSnapshotter
{
    private static final int UNKNOWN = -1;
    private static final ItemSnapshot[] NO_ITEM_SNAPSHOTS = new ItemSnapshot[0];

    private final Client client;
    private final Map<Integer, ItemSnapshot[]> previousContainers = new HashMap<>();

    @Inject
    RuneLiteSnapshotter(Client client)
    {
        this.client = client;
    }

    RuneLiteSnapshotter()
    {
        this.client = null;
    }

    EventTime eventTime(RuneLiteEventEnvelope envelope)
    {
        return new EventTime(
            envelope.getWallTimeMillis(),
            envelope.getMonotonicNanos(),
            envelope.getGameTick(),
            envelope.getClientCycle(),
            envelope.getClientTickSequence());
    }

    Map<String, String> sourceTags(String sourceEventType)
    {
        final Map<String, String> tags = new LinkedHashMap<>();
        tags.put("source", sourceEventType);
        return tags;
    }

    EntityRef actorRef(Actor actor)
    {
        if (actor == null)
        {
            return EntityRef.unknown();
        }
        if (client != null && actor == client.getLocalPlayer())
        {
            return EntityRef.localPlayer();
        }
        if (actor instanceof NPC)
        {
            return npcRef((NPC) actor);
        }
        return EntityRef.unknown();
    }

    EntityRef npcRef(NPC npc)
    {
        if (npc == null)
        {
            return EntityRef.unknown();
        }
        return EntityRef.npc(npc.getIndex(), npc.getId(), npc.getName());
    }

    EntityRef widgetRef(Widget widget)
    {
        if (widget == null)
        {
            return EntityRef.unknown();
        }
        return EntityRef.widget(widget.getParentId(), widget.getIndex());
    }

    EntityRef menuTargetRef(MenuEntry menuEntry)
    {
        if (menuEntry == null)
        {
            return EntityRef.unknown();
        }
        final NPC npc = menuEntry.getNpc();
        if (npc != null)
        {
            return npcRef(npc);
        }
        final Widget widget = menuEntry.getWidget();
        if (widget != null)
        {
            return widgetRef(widget);
        }
        return actorRef(menuEntry.getActor());
    }

    WorldLocation actorLocation(Actor actor, RuneLiteEventEnvelope envelope)
    {
        if (actor == null)
        {
            return WorldLocation.unknown();
        }
        return worldLocation(actor.getWorldLocation(), envelope, false);
    }

    WorldLocation localPlayerLocation(RuneLiteEventEnvelope envelope)
    {
        if (client == null || client.getLocalPlayer() == null)
        {
            return WorldLocation.unknown();
        }
        return actorLocation(client.getLocalPlayer(), envelope);
    }

    WorldLocation worldLocation(WorldPoint point, RuneLiteEventEnvelope envelope, boolean instanced)
    {
        if (point == null)
        {
            return WorldLocation.unknown();
        }
        return new WorldLocation(
            envelope.getWorld(),
            point.getPlane(),
            point.getX(),
            point.getY(),
            point.getRegionID(),
            instanced);
    }

    WorldLocation worldViewLocation(WorldView worldView, RuneLiteEventEnvelope envelope)
    {
        if (worldView == null)
        {
            return localPlayerLocation(envelope);
        }
        return new WorldLocation(
            envelope.getWorld(),
            worldView.getPlane(),
            worldView.getBaseX(),
            worldView.getBaseY(),
            regionId(worldView),
            worldView.isInstance());
    }

    int regionId(WorldView worldView)
    {
        if (worldView == null)
        {
            return UNKNOWN;
        }
        return firstRegionId(worldView.getMapRegions());
    }

    String worldViewId(WorldView worldView)
    {
        return worldView == null ? "unknown" : String.valueOf(worldView.getId());
    }

    List<String> menuEntryLabels(MenuEntry[] menuEntries)
    {
        if (menuEntries == null || menuEntries.length == 0)
        {
            return Collections.emptyList();
        }
        final List<String> labels = new ArrayList<>(menuEntries.length);
        for (MenuEntry menuEntry : menuEntries)
        {
            if (menuEntry != null)
            {
                labels.add(menuEntryLabel(menuEntry));
            }
        }
        return immutableList(labels);
    }

    String menuEntryLabel(MenuEntry menuEntry)
    {
        if (menuEntry == null)
        {
            return "";
        }
        final String option = menuEntry.getOption() == null ? "" : menuEntry.getOption();
        final String target = menuEntry.getTarget() == null ? "" : menuEntry.getTarget();
        return target.isEmpty() ? option : option + " " + target;
    }

    List<String> widgetActions(Widget widget)
    {
        if (widget == null || widget.getActions() == null)
        {
            return Collections.emptyList();
        }
        final String[] widgetActions = widget.getActions();
        final List<String> actions = new ArrayList<>(widgetActions.length);
        for (String action : widgetActions)
        {
            if (action != null && !action.isEmpty())
            {
                actions.add(action);
            }
        }
        return immutableList(actions);
    }

    List<InventoryDeltaTelemetryEvent.ItemDelta> itemDeltas(int containerId, ItemContainer itemContainer)
    {
        final ItemSnapshot[] before = previousContainers.get(containerId);
        final ItemSnapshot[] after = itemSnapshots(itemContainer);
        previousContainers.put(containerId, after);

        final int slotCount = Math.max(before == null ? 0 : before.length, after.length);
        final List<InventoryDeltaTelemetryEvent.ItemDelta> deltas = new ArrayList<>();
        for (int slot = 0; slot < slotCount; slot++)
        {
            final ItemSnapshot beforeItem = itemSnapshotAt(before, slot);
            final ItemSnapshot afterItem = itemSnapshotAt(after, slot);
            if (!beforeItem.equals(afterItem))
            {
                deltas.add(new InventoryDeltaTelemetryEvent.ItemDelta(
                    slot,
                    beforeItem.id,
                    beforeItem.quantity,
                    afterItem.id,
                    afterItem.quantity));
            }
        }
        return immutableList(deltas);
    }

    private static ItemSnapshot[] itemSnapshots(ItemContainer itemContainer)
    {
        if (itemContainer == null || itemContainer.getItems() == null)
        {
            return NO_ITEM_SNAPSHOTS;
        }
        final Item[] items = itemContainer.getItems();
        final ItemSnapshot[] snapshots = new ItemSnapshot[items.length];
        for (int i = 0; i < items.length; i++)
        {
            final Item item = items[i];
            snapshots[i] = item == null ? ItemSnapshot.EMPTY : new ItemSnapshot(item.getId(), item.getQuantity());
        }
        return snapshots;
    }

    private static int firstRegionId(int[] mapRegions)
    {
        return mapRegions == null || mapRegions.length == 0 ? UNKNOWN : mapRegions[0];
    }

    private static ItemSnapshot itemSnapshotAt(ItemSnapshot[] snapshots, int slot)
    {
        return snapshots == null || slot >= snapshots.length ? ItemSnapshot.EMPTY : snapshots[slot];
    }

    private static <T> List<T> immutableList(List<T> values)
    {
        return values.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(values);
    }

    private static final class ItemSnapshot
    {
        private static final ItemSnapshot EMPTY = new ItemSnapshot(UNKNOWN, 0);

        private final int id;
        private final int quantity;

        private ItemSnapshot(int id, int quantity)
        {
            this.id = id;
            this.quantity = quantity;
        }

        @Override
        public boolean equals(Object other)
        {
            if (this == other)
            {
                return true;
            }
            if (!(other instanceof ItemSnapshot))
            {
                return false;
            }
            final ItemSnapshot that = (ItemSnapshot) other;
            return id == that.id && quantity == that.quantity;
        }

        @Override
        public int hashCode()
        {
            return 31 * id + quantity;
        }
    }
}
