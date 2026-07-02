package com.ticksense.telemetry.events;

import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.Map;
import java.util.Objects;

public final class InteractingChangedTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "interacting.changed";

    private final EntityRef actorRef;
    private final EntityRef interactingRef;
    private final String stateChange;

    public InteractingChangedTelemetryEvent(
        EventTime time,
        Map<String, String> tags,
        EntityRef actorRef,
        EntityRef interactingRef,
        String stateChange)
    {
        super(TYPE, TelemetryCategory.PLAYER_ACTION, time, tags);
        this.actorRef = Objects.requireNonNull(actorRef, "actorRef");
        this.interactingRef = Objects.requireNonNull(interactingRef, "interactingRef");
        this.stateChange = safeText(stateChange);
    }

    public EntityRef getActorRef()
    {
        return actorRef;
    }

    public EntityRef getInteractingRef()
    {
        return interactingRef;
    }

    public String getStateChange()
    {
        return stateChange;
    }
}
