package com.ticksense.telemetry.events;

import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.Map;
import java.util.Objects;

public final class NpcStateTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "npc.state";

    private final EntityRef npcRef;
    private final String stateChange;
    private final WorldLocation location;
    private final int animationId;
    private final int graphicId;
    private final EntityRef interactingRef;
    private final int healthRatio;
    private final int healthScale;

    public NpcStateTelemetryEvent(
        EventTime time,
        Map<String, String> tags,
        EntityRef npcRef,
        String stateChange,
        WorldLocation location,
        int animationId,
        int graphicId,
        EntityRef interactingRef,
        int healthRatio,
        int healthScale)
    {
        super(TYPE, TelemetryCategory.NPC_STATE, time, tags);
        this.npcRef = Objects.requireNonNull(npcRef, "npcRef");
        this.stateChange = safeText(stateChange);
        this.location = Objects.requireNonNull(location, "location");
        this.animationId = animationId;
        this.graphicId = graphicId;
        this.interactingRef = Objects.requireNonNull(interactingRef, "interactingRef");
        this.healthRatio = healthRatio;
        this.healthScale = healthScale;
    }

    public EntityRef getNpcRef()
    {
        return npcRef;
    }

    public String getStateChange()
    {
        return stateChange;
    }

    public WorldLocation getLocation()
    {
        return location;
    }

    public int getAnimationId()
    {
        return animationId;
    }

    public int getGraphicId()
    {
        return graphicId;
    }

    public EntityRef getInteractingRef()
    {
        return interactingRef;
    }

    public int getHealthRatio()
    {
        return healthRatio;
    }

    public int getHealthScale()
    {
        return healthScale;
    }
}
