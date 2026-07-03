package com.ticksense.analytics;

import com.ticksense.activities.OpportunityEvidence;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.OpportunityStatus;
import java.util.List;
import java.util.Objects;

public final class ResolvedOpportunity
{
    private final OpportunityMarker open;
    private final OpportunityMarker terminal;

    ResolvedOpportunity(OpportunityMarker open, OpportunityMarker terminal)
    {
        this.open = Objects.requireNonNull(open, "open");
        this.terminal = Objects.requireNonNull(terminal, "terminal");
    }

    public String type()
    {
        return terminal.getOpportunityType();
    }

    public OpportunityStatus status()
    {
        return terminal.getStatus();
    }

    public boolean completed()
    {
        return status() == OpportunityStatus.COMPLETED;
    }

    public String contextValue(String key)
    {
        final String value = open.getContext().get(key);
        return value == null ? "" : value.trim();
    }

    public int latencyTicks()
    {
        return terminal.getTime().getGameTick() - open.getTime().getGameTick();
    }

    public long latencyMillis()
    {
        return terminal.getTime().getWallTimeMillis() - open.getTime().getWallTimeMillis();
    }

    public int endTick()
    {
        return terminal.getTime().getGameTick();
    }

    public long endWallTimeMillis()
    {
        return terminal.getTime().getWallTimeMillis();
    }

    public List<OpportunityEvidence> terminalEvidence()
    {
        return terminal.getEvidence();
    }
}
