package com.ticksense.runelite;

public final class RuneLiteEventEnvelope
{
    private final String sourceEventType;
    private final long wallTimeMillis;
    private final long monotonicNanos;
    private final int gameTick;
    private final int clientCycle;
    private final int clientTickSequence;
    private final int world;
    private final int fps;
    private final String gameState;

    RuneLiteEventEnvelope(
        String sourceEventType,
        long wallTimeMillis,
        long monotonicNanos,
        int gameTick,
        int clientCycle,
        int clientTickSequence,
        int world,
        int fps,
        String gameState)
    {
        this.sourceEventType = sourceEventType;
        this.wallTimeMillis = wallTimeMillis;
        this.monotonicNanos = monotonicNanos;
        this.gameTick = gameTick;
        this.clientCycle = clientCycle;
        this.clientTickSequence = clientTickSequence;
        this.world = world;
        this.fps = fps;
        this.gameState = gameState;
    }

    public String getSourceEventType()
    {
        return sourceEventType;
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

    public int getClientCycle()
    {
        return clientCycle;
    }

    public int getClientTickSequence()
    {
        return clientTickSequence;
    }

    public int getWorld()
    {
        return world;
    }

    public int getFps()
    {
        return fps;
    }

    public String getGameState()
    {
        return gameState;
    }
}
