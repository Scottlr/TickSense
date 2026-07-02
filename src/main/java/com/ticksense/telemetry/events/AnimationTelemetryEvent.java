package com.ticksense.telemetry.events;

import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.Map;
import java.util.Objects;

public final class AnimationTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "animation";

    private final EntityRef actorRef;
    private final int animationId;
    private final int previousAnimationId;

    public AnimationTelemetryEvent(
        EventTime time,
        Map<String, String> tags,
        EntityRef actorRef,
        int animationId,
        int previousAnimationId)
    {
        super(TYPE, TelemetryCategory.ANIMATION, time, tags);
        this.actorRef = Objects.requireNonNull(actorRef, "actorRef");
        this.animationId = animationId;
        this.previousAnimationId = previousAnimationId;
    }

    public EntityRef getActorRef()
    {
        return actorRef;
    }

    public int getAnimationId()
    {
        return animationId;
    }

    public int getPreviousAnimationId()
    {
        return previousAnimationId;
    }
}
