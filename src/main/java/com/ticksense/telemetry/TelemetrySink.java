package com.ticksense.telemetry;

public interface TelemetrySink
{
    void accept(TelemetryEnvelope envelope);
}
