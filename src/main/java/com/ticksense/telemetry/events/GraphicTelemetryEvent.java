package com.ticksense.telemetry.events;

import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.Map;
import java.util.Objects;

public final class GraphicTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "graphic";

    private final EntityRef actorRef;
    private final int graphicId;
    private final WorldLocation location;

    public GraphicTelemetryEvent(EventTime time, Map<String, String> tags, EntityRef actorRef, int graphicId, WorldLocation location)
    {
        super(TYPE, TelemetryCategory.GRAPHICS, time, tags);
        this.actorRef = Objects.requireNonNull(actorRef, "actorRef");
        this.graphicId = graphicId;
        this.location = Objects.requireNonNull(location, "location");
    }

    public EntityRef getActorRef()
    {
        return actorRef;
    }

    public int getGraphicId()
    {
        return graphicId;
    }

    public WorldLocation getLocation()
    {
        return location;
    }
}
