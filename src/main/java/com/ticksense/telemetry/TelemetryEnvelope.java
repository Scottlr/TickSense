package com.ticksense.telemetry;

import java.util.Objects;

public final class TelemetryEnvelope
{
    private final int schemaVersion;
    private final String eventId;
    private final String sessionId;
    private final TelemetryEvent event;

    public TelemetryEnvelope(int schemaVersion, String eventId, String sessionId, TelemetryEvent event)
    {
        TelemetrySchema.requireSupported(schemaVersion);
        this.schemaVersion = schemaVersion;
        this.eventId = TelemetryTexts.requireText(eventId, "eventId");
        this.sessionId = TelemetryTexts.requireText(sessionId, "sessionId");
        this.event = Objects.requireNonNull(event, "event");
    }

    public static TelemetryEnvelope create(String eventId, String sessionId, TelemetryEvent event)
    {
        return new TelemetryEnvelope(TelemetrySchema.MVP_SCHEMA_VERSION, eventId, sessionId, event);
    }

    public int getSchemaVersion()
    {
        return schemaVersion;
    }

    public String getEventId()
    {
        return eventId;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public TelemetryEvent getEvent()
    {
        return event;
    }
}
