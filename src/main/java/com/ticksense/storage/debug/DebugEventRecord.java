package com.ticksense.storage.debug;

import com.ticksense.core.EventTime;
import com.ticksense.common.TextValues;
import java.util.Objects;

public final class DebugEventRecord
{
    public static final int SCHEMA_VERSION = 1;

    private final int debugSchemaVersion;
    private final DebugEventKind kind;
    private final String sessionId;
    private final String sourceEventType;
    private final EventTime time;
    private final String payloadJson;

    public DebugEventRecord(
        int debugSchemaVersion,
        DebugEventKind kind,
        String sessionId,
        String sourceEventType,
        EventTime time,
        String payloadJson)
    {
        this.debugSchemaVersion = requireSchemaVersion(debugSchemaVersion);
        this.kind = Objects.requireNonNull(kind, "kind");
        this.sessionId = TextValues.requireText(sessionId, "sessionId");
        this.sourceEventType = TextValues.requireText(sourceEventType, "sourceEventType");
        this.time = Objects.requireNonNull(time, "time");
        this.payloadJson = TextValues.requireText(payloadJson, "payloadJson");
    }

    public static DebugEventRecord normalizedTelemetry(String sessionId, String sourceEventType, EventTime time, String payloadJson)
    {
        return new DebugEventRecord(SCHEMA_VERSION, DebugEventKind.NORMALIZED_TELEMETRY, sessionId, sourceEventType, time, payloadJson);
    }

    public static DebugEventRecord of(DebugEventKind kind, String sessionId, String sourceEventType, EventTime time, String payloadJson)
    {
        return new DebugEventRecord(SCHEMA_VERSION, kind, sessionId, sourceEventType, time, payloadJson);
    }

    public int getDebugSchemaVersion()
    {
        return debugSchemaVersion;
    }

    public DebugEventKind getKind()
    {
        return kind;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public String getSourceEventType()
    {
        return sourceEventType;
    }

    public EventTime getTime()
    {
        return time;
    }

    public String getPayloadJson()
    {
        return payloadJson;
    }

    private static int requireSchemaVersion(int schemaVersion)
    {
        if (schemaVersion != SCHEMA_VERSION)
        {
            throw new IllegalArgumentException("Unsupported debug schema version: " + schemaVersion);
        }
        return schemaVersion;
    }
}
