package com.ticksense.telemetry.events;

import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.Map;
import java.util.Objects;

public final class ProjectileTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "projectile";

    private final int projectileId;
    private final EntityRef sourceRef;
    private final EntityRef targetRef;
    private final WorldLocation location;
    private final int startCycle;
    private final int endCycle;

    public ProjectileTelemetryEvent(
        EventTime time,
        Map<String, String> tags,
        int projectileId,
        EntityRef sourceRef,
        EntityRef targetRef,
        WorldLocation location,
        int startCycle,
        int endCycle)
    {
        super(TYPE, TelemetryCategory.PROJECTILE, time, tags);
        this.projectileId = projectileId;
        this.sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
        this.targetRef = Objects.requireNonNull(targetRef, "targetRef");
        this.location = Objects.requireNonNull(location, "location");
        this.startCycle = startCycle;
        this.endCycle = endCycle;
    }

    public int getProjectileId()
    {
        return projectileId;
    }

    public EntityRef getSourceRef()
    {
        return sourceRef;
    }

    public EntityRef getTargetRef()
    {
        return targetRef;
    }

    public WorldLocation getLocation()
    {
        return location;
    }

    public int getStartCycle()
    {
        return startCycle;
    }

    public int getEndCycle()
    {
        return endCycle;
    }
}
