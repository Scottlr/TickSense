package com.ticksense.core;

import java.util.Objects;

public final class EventTime
{
    private final long wallTimeMillis;
    private final long monotonicNanos;
    private final int gameTick;
    private final long clientCycle;
    private final int clientTickSequence;

    public EventTime(long wallTimeMillis, long monotonicNanos, int gameTick, long clientCycle, int clientTickSequence)
    {
        this.wallTimeMillis = wallTimeMillis;
        this.monotonicNanos = monotonicNanos;
        this.gameTick = gameTick;
        this.clientCycle = clientCycle;
        this.clientTickSequence = clientTickSequence;
    }

    public long getWallTimeMillis()
    {
        return wallTimeMillis;
    }

    public long getMonotonicNanos()
    {
        return monotonicNanos;
    }

    public int getGameTick()
    {
        return gameTick;
    }

    public long getClientCycle()
    {
        return clientCycle;
    }

    public int getClientTickSequence()
    {
        return clientTickSequence;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof EventTime))
        {
            return false;
        }
        final EventTime that = (EventTime) other;
        return wallTimeMillis == that.wallTimeMillis
            && monotonicNanos == that.monotonicNanos
            && gameTick == that.gameTick
            && clientCycle == that.clientCycle
            && clientTickSequence == that.clientTickSequence;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(wallTimeMillis, monotonicNanos, gameTick, clientCycle, clientTickSequence);
    }
}
