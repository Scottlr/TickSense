package com.ticksense.telemetry;

public final class TelemetrySchema
{
    public static final int MVP_SCHEMA_VERSION = 1;

    private TelemetrySchema()
    {
    }

    public static void requireSupported(int schemaVersion)
    {
        if (schemaVersion != MVP_SCHEMA_VERSION)
        {
            throw new IllegalArgumentException("Unsupported telemetry schema version: " + schemaVersion);
        }
    }
}
