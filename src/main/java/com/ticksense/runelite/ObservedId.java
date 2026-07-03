package com.ticksense.runelite;

import com.ticksense.common.TextValues;
import java.util.Objects;

public final class ObservedId
{
    private final String kind;
    private final int id;
    private final String sourceEventType;
    private final int lastSeenTick;
    private final int count;

    public ObservedId(String kind, int id, String sourceEventType, int lastSeenTick, int count)
    {
        this.kind = TextValues.requireText(kind, "kind");
        this.id = id;
        this.sourceEventType = TextValues.requireText(sourceEventType, "sourceEventType");
        this.lastSeenTick = lastSeenTick;
        this.count = requirePositive(count);
    }

    public String getKind()
    {
        return kind;
    }

    public int getId()
    {
        return id;
    }

    public String getSourceEventType()
    {
        return sourceEventType;
    }

    public int getLastSeenTick()
    {
        return lastSeenTick;
    }

    public int getCount()
    {
        return count;
    }

    public ObservedId seenAgain(String sourceEventType, int tick)
    {
        return new ObservedId(kind, id, sourceEventType, tick, count + 1);
    }

    private static int requirePositive(int value)
    {
        if (value <= 0)
        {
            throw new IllegalArgumentException("count must be positive");
        }
        return value;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof ObservedId))
        {
            return false;
        }
        final ObservedId that = (ObservedId) other;
        return id == that.id
            && lastSeenTick == that.lastSeenTick
            && count == that.count
            && kind.equals(that.kind)
            && sourceEventType.equals(that.sourceEventType);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(kind, id, sourceEventType, lastSeenTick, count);
    }
}
