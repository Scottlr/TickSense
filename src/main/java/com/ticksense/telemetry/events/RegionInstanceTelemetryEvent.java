package com.ticksense.telemetry.events;

import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.Map;
import java.util.Objects;

public final class RegionInstanceTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "region.instance";

    private final int world;
    private final int regionId;
    private final String worldViewId;
    private final boolean instanced;
    private final String gameState;
    private final WorldLocation localPlayerLocation;

    public RegionInstanceTelemetryEvent(
        EventTime time,
        Map<String, String> tags,
        int world,
        int regionId,
        String worldViewId,
        boolean instanced,
        String gameState,
        WorldLocation localPlayerLocation)
    {
        super(TYPE, TelemetryCategory.REGION_INSTANCE, time, tags);
        this.world = world;
        this.regionId = regionId;
        this.worldViewId = safeText(worldViewId);
        this.instanced = instanced;
        this.gameState = safeText(gameState);
        this.localPlayerLocation = Objects.requireNonNull(localPlayerLocation, "localPlayerLocation");
    }

    public int getWorld()
    {
        return world;
    }

    public int getRegionId()
    {
        return regionId;
    }

    public String getWorldViewId()
    {
        return worldViewId;
    }

    public boolean isInstanced()
    {
        return instanced;
    }

    public String getGameState()
    {
        return gameState;
    }

    public WorldLocation getLocalPlayerLocation()
    {
        return localPlayerLocation;
    }
}
