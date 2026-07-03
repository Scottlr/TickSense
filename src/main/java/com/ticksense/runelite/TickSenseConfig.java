package com.ticksense.runelite;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("ticksense")
public interface TickSenseConfig extends Config
{
    int DEFAULT_MAX_DEBUG_FILE_SIZE_MB = 25;
    int DEFAULT_MAX_DEBUG_SESSIONS = 5;

    @ConfigItem(
        keyName = "debugEventRecorder",
        name = "Debug event recorder",
        description = "Write local normalized debug timelines."
    )
    default boolean debugEventRecorder()
    {
        return false;
    }

    @ConfigItem(
        keyName = "debugActivityDiagnostics",
        name = "Activity diagnostics",
        description = "Show developer-only activity diagnostics."
    )
    default boolean debugActivityDiagnostics()
    {
        return false;
    }

    @ConfigItem(
        keyName = "maxDebugFileSizeMb",
        name = "Max debug file size MB",
        description = "Maximum debug file size."
    )
    default int maxDebugFileSizeMb()
    {
        return DEFAULT_MAX_DEBUG_FILE_SIZE_MB;
    }

    @ConfigItem(
        keyName = "maxDebugSessions",
        name = "Max debug sessions",
        description = "Maximum retained debug sessions."
    )
    default int maxDebugSessions()
    {
        return DEFAULT_MAX_DEBUG_SESSIONS;
    }
}
