package com.ticksense.telemetry.events;

import com.ticksense.core.EventTime;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.Map;

public final class EnvironmentTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "environment.performance";

    private final int fps;
    private final int world;
    private final String gameState;
    private final String pluginVersion;

    public EnvironmentTelemetryEvent(
        EventTime time,
        Map<String, String> tags,
        int fps,
        int world,
        String gameState,
        String pluginVersion)
    {
        super(TYPE, TelemetryCategory.ENVIRONMENT_PERFORMANCE, time, tags);
        this.fps = fps;
        this.world = world;
        this.gameState = safeText(gameState);
        this.pluginVersion = safeText(pluginVersion);
    }

    public int getFps()
    {
        return fps;
    }

    public int getWorld()
    {
        return world;
    }

    public String getGameState()
    {
        return gameState;
    }

    public String getPluginVersion()
    {
        return pluginVersion;
    }
}
