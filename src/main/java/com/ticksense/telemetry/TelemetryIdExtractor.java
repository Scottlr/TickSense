package com.ticksense.telemetry;

import com.ticksense.common.ImmutableCollections;
import com.ticksense.core.EntityRef;
import com.ticksense.telemetry.events.AnimationTelemetryEvent;
import com.ticksense.telemetry.events.GraphicTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.NpcStateTelemetryEvent;
import com.ticksense.telemetry.events.ObjectStateTelemetryEvent;
import com.ticksense.telemetry.events.ProjectileTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import com.ticksense.telemetry.events.WidgetTelemetryEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TelemetryIdExtractor
{
    private TelemetryIdExtractor()
    {
    }

    public static List<ObservedTelemetryId> extract(TelemetryEvent event)
    {
        final List<ObservedTelemetryId> ids = new ArrayList<>();
        collect(Objects.requireNonNull(event, "event"), ids);
        return ImmutableCollections.immutableList(ids);
    }

    private static void collect(TelemetryEvent event, List<ObservedTelemetryId> ids)
    {
        if (event instanceof NpcStateTelemetryEvent)
        {
            final NpcStateTelemetryEvent npc = (NpcStateTelemetryEvent) event;
            add(ids, "npc", npc.getNpcRef().getId());
            add(ids, "animation", npc.getAnimationId());
            add(ids, "graphic", npc.getGraphicId());
            addEntity(ids, "interacting-npc", npc.getInteractingRef());
            add(ids, "region", npc.getLocation().getRegionId());
        }
        else if (event instanceof AnimationTelemetryEvent)
        {
            final AnimationTelemetryEvent animation = (AnimationTelemetryEvent) event;
            addEntity(ids, "actor-npc", animation.getActorRef());
            add(ids, "animation", animation.getAnimationId());
        }
        else if (event instanceof GraphicTelemetryEvent)
        {
            final GraphicTelemetryEvent graphic = (GraphicTelemetryEvent) event;
            addEntity(ids, "actor-npc", graphic.getActorRef());
            add(ids, "graphic", graphic.getGraphicId());
            add(ids, "region", graphic.getLocation().getRegionId());
        }
        else if (event instanceof ProjectileTelemetryEvent)
        {
            final ProjectileTelemetryEvent projectile = (ProjectileTelemetryEvent) event;
            add(ids, "projectile", projectile.getProjectileId());
            addEntity(ids, "source-npc", projectile.getSourceRef());
            addEntity(ids, "target-npc", projectile.getTargetRef());
            add(ids, "region", projectile.getLocation().getRegionId());
        }
        else if (event instanceof ObjectStateTelemetryEvent)
        {
            final ObjectStateTelemetryEvent object = (ObjectStateTelemetryEvent) event;
            add(ids, "object", object.getObjectId());
            add(ids, "region", object.getLocation().getRegionId());
        }
        else if (event instanceof WidgetTelemetryEvent)
        {
            final WidgetTelemetryEvent widget = (WidgetTelemetryEvent) event;
            add(ids, "widget-group", widget.getGroupId());
            add(ids, "widget-child", widget.getChildId());
            add(ids, "widget-item", widget.getItemId());
        }
        else if (event instanceof InventoryDeltaTelemetryEvent)
        {
            add(ids, "container", ((InventoryDeltaTelemetryEvent) event).getContainerId());
        }
        else if (event instanceof RegionInstanceTelemetryEvent)
        {
            add(ids, "region", ((RegionInstanceTelemetryEvent) event).getRegionId());
        }
    }

    private static void addEntity(List<ObservedTelemetryId> ids, String kind, EntityRef ref)
    {
        if (ref.getType() == EntityRef.Type.NPC)
        {
            add(ids, kind, ref.getId());
        }
    }

    private static void add(List<ObservedTelemetryId> ids, String kind, int id)
    {
        if (id >= 0)
        {
            ids.add(new ObservedTelemetryId(kind, id));
        }
    }
}
