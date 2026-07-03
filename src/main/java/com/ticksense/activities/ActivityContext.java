package com.ticksense.activities;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
public final class ActivityContext
{
    private final String sessionId;
    private final int world;
    private final boolean debugActivityDiagnostics;
    private final Map<String, String> metadata;

    public ActivityContext(String sessionId, int world, boolean debugActivityDiagnostics, Map<String, String> metadata)
    {
        this.sessionId = ActivityTexts.requireText(sessionId, "sessionId");
        this.world = world;
        this.debugActivityDiagnostics = debugActivityDiagnostics;
        this.metadata = immutableMap(metadata);
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public int getWorld()
    {
        return world;
    }

    public boolean isDebugActivityDiagnostics()
    {
        return debugActivityDiagnostics;
    }

    public Map<String, String> getMetadata()
    {
        return metadata;
    }

    private static Map<String, String> immutableMap(Map<String, String> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}
