package com.ticksense.telemetry;

import com.ticksense.common.TextValues;
import java.util.Objects;

public final class ObservedTelemetryId
{
    private final String kind;
    private final int id;

    public ObservedTelemetryId(String kind, int id)
    {
        this.kind = TextValues.requireText(kind, "kind");
        this.id = id;
    }

    public String getKind()
    {
        return kind;
    }

    public int getId()
    {
        return id;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof ObservedTelemetryId))
        {
            return false;
        }
        final ObservedTelemetryId that = (ObservedTelemetryId) other;
        return id == that.id && kind.equals(that.kind);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(kind, id);
    }
}
