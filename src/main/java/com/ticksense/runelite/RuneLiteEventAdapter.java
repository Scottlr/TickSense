package com.ticksense.runelite;

import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryEvent;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Actor;
import net.runelite.api.Hitsplat;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Projectile;
import net.runelite.api.WorldView;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PostClientTick;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.events.WorldViewLoaded;
import net.runelite.api.events.WorldViewUnloaded;

@Singleton
public final class RuneLiteEventAdapter
{
    private static final int UNKNOWN = -1;

    private final RuneLiteSnapshotter snapshotter;
    private final String sessionId;
    private final AtomicLong eventSequence = new AtomicLong();

    @Inject
    RuneLiteEventAdapter(RuneLiteSnapshotter snapshotter)
    {
        this(snapshotter, "runelite-" + UUID.randomUUID());
    }

    RuneLiteEventAdapter(RuneLiteSnapshotter snapshotter, String sessionId)
    {
        this.snapshotter = snapshotter;
        this.sessionId = sessionId;
    }

    public Optional<TelemetryEnvelope> mapGameTick(GameTick event, RuneLiteEventEnvelope envelope)
    {
        return mapped(envelope, new GameTickTelemetryEvent(time(envelope), tags(envelope), envelope.getGameTick()));
    }

    public Optional<TelemetryEnvelope> mapClientTick(ClientTick event, RuneLiteEventEnvelope envelope)
    {
        return mapped(envelope, new ClientTickTelemetryEvent(time(envelope), tags(envelope), envelope.getClientTickSequence()));
    }

    public Optional<TelemetryEnvelope> mapPostClientTick(PostClientTick event, RuneLiteEventEnvelope envelope)
    {
        return mapped(envelope, new EnvironmentTelemetryEvent(
            time(envelope),
            tags(envelope),
            envelope.getFps(),
            envelope.getWorld(),
            envelope.getGameState(),
            ""));
    }

    public Optional<TelemetryEnvelope> mapGameStateChanged(GameStateChanged event, RuneLiteEventEnvelope envelope)
    {
        final String gameState = event.getGameState() == null ? envelope.getGameState() : event.getGameState().name();
        return mapped(envelope, new RegionInstanceTelemetryEvent(
            time(envelope),
            tags(envelope),
            envelope.getWorld(),
            snapshotter.localPlayerLocation(envelope).getRegionId(),
            "game-state",
            snapshotter.localPlayerLocation(envelope).isInstanced(),
            gameState,
            snapshotter.localPlayerLocation(envelope)));
    }

    public Optional<TelemetryEnvelope> mapMenuOptionClicked(MenuOptionClicked event, RuneLiteEventEnvelope envelope)
    {
        final MenuEntry menuEntry = event.getMenuEntry();
        final EntityRef targetRef = snapshotter.menuTargetRef(menuEntry);
        return mapped(envelope, new PlayerActionTelemetryEvent(
            time(envelope),
            tags(envelope),
            event.getMenuOption(),
            event.getMenuTarget(),
            targetRef,
            menuActionName(event.getMenuAction()),
            snapshotter.localPlayerLocation(envelope),
            event.getId()));
    }

    public Optional<TelemetryEnvelope> mapMenuEntryAdded(MenuEntryAdded event, RuneLiteEventEnvelope envelope)
    {
        final MenuEntry menuEntry = event.getMenuEntry();
        return mapped(envelope, new MenuInteractionTelemetryEvent(
            time(envelope),
            tags(envelope),
            "ENTRY_ADDED",
            Collections.singletonList(snapshotter.menuEntryLabel(menuEntry)),
            event.getOption(),
            event.getTarget(),
            event.getIdentifier(),
            event.getActionParam0(),
            event.getActionParam1(),
            menuEntry == null ? EntityRef.unknown() : snapshotter.widgetRef(menuEntry.getWidget())));
    }

    public Optional<TelemetryEnvelope> mapMenuOpened(MenuOpened event, RuneLiteEventEnvelope envelope)
    {
        final MenuEntry firstEntry = event.getFirstEntry();
        return mapped(envelope, new MenuInteractionTelemetryEvent(
            time(envelope),
            tags(envelope),
            "OPENED",
            snapshotter.menuEntryLabels(event.getMenuEntries()),
            firstEntry == null ? "" : firstEntry.getOption(),
            firstEntry == null ? "" : firstEntry.getTarget(),
            firstEntry == null ? UNKNOWN : firstEntry.getIdentifier(),
            firstEntry == null ? UNKNOWN : firstEntry.getParam0(),
            firstEntry == null ? UNKNOWN : firstEntry.getParam1(),
            firstEntry == null ? EntityRef.unknown() : snapshotter.widgetRef(firstEntry.getWidget())));
    }

    public Optional<TelemetryEnvelope> mapNpcSpawned(NpcSpawned event, RuneLiteEventEnvelope envelope)
    {
        return mapNpcState(event.getNpc(), "SPAWNED", envelope);
    }

    public Optional<TelemetryEnvelope> mapNpcDespawned(NpcDespawned event, RuneLiteEventEnvelope envelope)
    {
        return mapNpcState(event.getNpc(), "DESPAWNED", envelope);
    }

    public Optional<TelemetryEnvelope> mapNpcChanged(NpcChanged event, RuneLiteEventEnvelope envelope)
    {
        return mapNpcState(event.getNpc(), "CHANGED", envelope);
    }

    public Optional<TelemetryEnvelope> mapInteractingChanged(InteractingChanged event, RuneLiteEventEnvelope envelope)
    {
        return mapped(envelope, new InteractingChangedTelemetryEvent(
            time(envelope),
            tags(envelope),
            snapshotter.actorRef(event.getSource()),
            snapshotter.actorRef(event.getTarget()),
            "TARGET_CHANGED"));
    }

    public Optional<TelemetryEnvelope> mapAnimationChanged(AnimationChanged event, RuneLiteEventEnvelope envelope)
    {
        final Actor actor = event.getActor();
        return mapped(envelope, new AnimationTelemetryEvent(
            time(envelope),
            tags(envelope),
            snapshotter.actorRef(actor),
            actor == null ? UNKNOWN : actor.getAnimation(),
            UNKNOWN));
    }

    public Optional<TelemetryEnvelope> mapHitsplatApplied(HitsplatApplied event, RuneLiteEventEnvelope envelope)
    {
        final Actor actor = event.getActor();
        final Hitsplat hitsplat = event.getHitsplat();
        return mapped(envelope, new DamageTelemetryEvent(
            time(envelope),
            tags(envelope),
            snapshotter.actorRef(actor),
            hitsplat == null ? UNKNOWN : hitsplat.getHitsplatType(),
            hitsplat == null ? 0 : hitsplat.getAmount(),
            actor == null ? UNKNOWN : actor.getHealthRatio(),
            actor == null ? UNKNOWN : actor.getHealthScale()));
    }

    public Optional<TelemetryEnvelope> mapItemContainerChanged(ItemContainerChanged event, RuneLiteEventEnvelope envelope)
    {
        return mapped(envelope, new InventoryDeltaTelemetryEvent(
            time(envelope),
            tags(envelope),
            event.getContainerId(),
            snapshotter.itemDeltas(event.getContainerId(), event.getItemContainer())));
    }

    public Optional<TelemetryEnvelope> mapStatChanged(StatChanged event, RuneLiteEventEnvelope envelope)
    {
        return mapped(envelope, new StatChangedTelemetryEvent(
            time(envelope),
            tags(envelope),
            event.getSkill() == null ? "" : event.getSkill().name(),
            event.getXp(),
            0,
            event.getLevel(),
            event.getBoostedLevel()));
    }

    public Optional<TelemetryEnvelope> mapWidgetLoaded(WidgetLoaded event, RuneLiteEventEnvelope envelope)
    {
        return mapped(envelope, new WidgetTelemetryEvent(
            time(envelope),
            tags(envelope),
            event.getGroupId(),
            UNKNOWN,
            UNKNOWN,
            "",
            "LOADED",
            true,
            Collections.emptyList()));
    }

    public Optional<TelemetryEnvelope> mapWidgetClosed(WidgetClosed event, RuneLiteEventEnvelope envelope)
    {
        return mapped(envelope, new WidgetTelemetryEvent(
            time(envelope),
            tags(envelope),
            event.getGroupId(),
            UNKNOWN,
            UNKNOWN,
            "",
            event.isUnload() ? "UNLOADED" : "CLOSED",
            false,
            Collections.emptyList()));
    }

    public Optional<TelemetryEnvelope> mapWorldViewLoaded(WorldViewLoaded event, RuneLiteEventEnvelope envelope)
    {
        return mapWorldView(event.getWorldView(), "LOADED", envelope);
    }

    public Optional<TelemetryEnvelope> mapWorldViewUnloaded(WorldViewUnloaded event, RuneLiteEventEnvelope envelope)
    {
        return mapWorldView(event.getWorldView(), "UNLOADED", envelope);
    }

    public Optional<TelemetryEnvelope> mapProjectileMoved(ProjectileMoved event, RuneLiteEventEnvelope envelope)
    {
        final Projectile projectile = event.getProjectile();
        return mapped(envelope, new ProjectileTelemetryEvent(
            time(envelope),
            tags(envelope),
            projectile == null ? UNKNOWN : projectile.getId(),
            projectile == null ? EntityRef.unknown() : snapshotter.actorRef(projectile.getSourceActor()),
            projectile == null ? EntityRef.unknown() : snapshotter.actorRef(projectile.getTargetActor()),
            projectile == null ? WorldLocation.unknown() : snapshotter.worldLocation(projectile.getTargetPoint(), envelope, false),
            projectile == null ? UNKNOWN : projectile.getStartCycle(),
            projectile == null ? UNKNOWN : projectile.getEndCycle()));
    }

    public Optional<TelemetryEnvelope> mapGraphicChanged(GraphicChanged event, RuneLiteEventEnvelope envelope)
    {
        final Actor actor = event.getActor();
        return mapped(envelope, new GraphicTelemetryEvent(
            time(envelope),
            tags(envelope),
            snapshotter.actorRef(actor),
            actor == null ? UNKNOWN : actor.getGraphic(),
            snapshotter.actorLocation(actor, envelope)));
    }

    public Optional<TelemetryEnvelope> mapObjectSnapshot(
        String sourceEventType,
        int objectId,
        String objectName,
        WorldLocation location,
        String objectType,
        List<String> actions,
        String stateChange,
        RuneLiteEventEnvelope envelope)
    {
        return mapped(envelope, sourceEventType, new ObjectStateTelemetryEvent(
            time(envelope),
            snapshotter.sourceTags(sourceEventType),
            objectId,
            objectName,
            location,
            objectType,
            actions,
            stateChange));
    }

    public Optional<TelemetryEnvelope> mapMovementSnapshot(
        String sourceEventType,
        EntityRef entityRef,
        WorldLocation fromLocation,
        WorldLocation toLocation,
        String movementKind,
        Integer distanceTiles,
        RuneLiteEventEnvelope envelope)
    {
        return mapped(envelope, sourceEventType, new MovementTelemetryEvent(
            time(envelope),
            snapshotter.sourceTags(sourceEventType),
            entityRef,
            fromLocation,
            toLocation,
            movementKind,
            distanceTiles));
    }

    private Optional<TelemetryEnvelope> mapNpcState(net.runelite.api.NPC npc, String stateChange, RuneLiteEventEnvelope envelope)
    {
        return mapped(envelope, new NpcStateTelemetryEvent(
            time(envelope),
            tags(envelope),
            snapshotter.npcRef(npc),
            stateChange,
            snapshotter.actorLocation(npc, envelope),
            npc == null ? UNKNOWN : npc.getAnimation(),
            npc == null ? UNKNOWN : npc.getGraphic(),
            npc == null ? EntityRef.unknown() : snapshotter.actorRef(npc.getInteracting()),
            npc == null ? UNKNOWN : npc.getHealthRatio(),
            npc == null ? UNKNOWN : npc.getHealthScale()));
    }

    private Optional<TelemetryEnvelope> mapWorldView(WorldView worldView, String stateChange, RuneLiteEventEnvelope envelope)
    {
        return mapped(envelope, new RegionInstanceTelemetryEvent(
            time(envelope),
            tagsWithState(envelope, stateChange),
            envelope.getWorld(),
            snapshotter.regionId(worldView),
            snapshotter.worldViewId(worldView),
            worldView != null && worldView.isInstance(),
            envelope.getGameState(),
            snapshotter.worldViewLocation(worldView, envelope)));
    }

    private Optional<TelemetryEnvelope> mapped(RuneLiteEventEnvelope envelope, TelemetryEvent event)
    {
        return mapped(envelope, envelope.getSourceEventType(), event);
    }

    private Optional<TelemetryEnvelope> mapped(RuneLiteEventEnvelope envelope, String sourceEventType, TelemetryEvent event)
    {
        return Optional.of(new TelemetryEnvelope(
            com.ticksense.telemetry.TelemetrySchema.MVP_SCHEMA_VERSION,
            sourceEventType + "-" + eventSequence.incrementAndGet(),
            sessionId,
            event));
    }

    private EventTime time(RuneLiteEventEnvelope envelope)
    {
        return snapshotter.eventTime(envelope);
    }

    private java.util.Map<String, String> tags(RuneLiteEventEnvelope envelope)
    {
        return snapshotter.sourceTags(envelope.getSourceEventType());
    }

    private java.util.Map<String, String> tagsWithState(RuneLiteEventEnvelope envelope, String stateChange)
    {
        final java.util.Map<String, String> tags = snapshotter.sourceTags(envelope.getSourceEventType());
        tags.put("stateChange", stateChange);
        return tags;
    }

    private static String menuActionName(MenuAction menuAction)
    {
        return menuAction == null ? "" : menuAction.name();
    }
}
