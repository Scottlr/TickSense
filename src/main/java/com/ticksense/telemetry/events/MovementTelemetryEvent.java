package com.ticksense.telemetry.events;

import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.Map;
import java.util.Objects;

public final class MovementTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "movement.location";

    private final EntityRef entityRef;
    private final WorldLocation fromLocation;
    private final WorldLocation toLocation;
    private final String movementKind;
    private final Integer distanceTiles;

    public MovementTelemetryEvent(
        EventTime time,
        Map<String, String> tags,
        EntityRef entityRef,
        WorldLocation fromLocation,
        WorldLocation toLocation,
        String movementKind,
        Integer distanceTiles)
    {
        super(TYPE, TelemetryCategory.MOVEMENT_LOCATION, time, tags);
        this.entityRef = Objects.requireNonNull(entityRef, "entityRef");
        this.fromLocation = Objects.requireNonNull(fromLocation, "fromLocation");
        this.toLocation = Objects.requireNonNull(toLocation, "toLocation");
        this.movementKind = safeText(movementKind);
        this.distanceTiles = distanceTiles;
    }

    public EntityRef getEntityRef()
    {
        return entityRef;
    }

    public WorldLocation getFromLocation()
    {
        return fromLocation;
    }

    public WorldLocation getToLocation()
    {
        return toLocation;
    }

    public String getMovementKind()
    {
        return movementKind;
    }

    public Integer getDistanceTiles()
    {
        return distanceTiles;
    }
}
