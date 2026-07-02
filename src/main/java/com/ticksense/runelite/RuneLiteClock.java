package com.ticksense.runelite;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;

@Singleton
public final class RuneLiteClock
{
    private final Client client;

    private int gameTick;
    private int clientTickSequence;

    @Inject
    RuneLiteClock(Client client)
    {
        this.client = client;
    }

    void advanceGameTick()
    {
        gameTick++;
    }

    void advanceClientTick()
    {
        clientTickSequence++;
    }

    RuneLiteEventEnvelope capture(String sourceEventType)
    {
        final GameState gameState = client.getGameState();
        return new RuneLiteEventEnvelope(
            sourceEventType,
            System.currentTimeMillis(),
            System.nanoTime(),
            gameTick,
            client.getGameCycle(),
            clientTickSequence,
            client.getWorld(),
            client.getFPS(),
            gameState == null ? "UNKNOWN" : gameState.name());
    }
}
