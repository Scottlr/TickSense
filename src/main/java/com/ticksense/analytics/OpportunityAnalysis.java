package com.ticksense.analytics;

import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.OpportunityStatus;
import java.util.ArrayList;
import java.util.List;

public final class OpportunityAnalysis
{
    private OpportunityAnalysis()
    {
    }

    public static List<ResolvedOpportunity> resolve(List<OpportunityMarker> opportunityMarkers)
    {
        return OpportunityMarkerResolver.resolve(opportunityMarkers);
    }

    public static List<Double> completedLatencies(List<ResolvedOpportunity> opportunities, String opportunityType)
    {
        return latenciesFor(opportunities, opportunityType, OpportunityStatus.COMPLETED);
    }

    public static List<Double> latenciesFor(
        List<ResolvedOpportunity> opportunities,
        String opportunityType,
        OpportunityStatus status)
    {
        final List<Double> latencies = new ArrayList<>();
        for (ResolvedOpportunity opportunity : opportunities)
        {
            if (opportunityType.equals(opportunity.type()) && opportunity.status() == status)
            {
                latencies.add((double) opportunity.latencyTicks());
            }
        }
        return latencies;
    }

    public static int latencyTicksFor(
        List<ResolvedOpportunity> opportunities,
        String opportunityType,
        OpportunityStatus status)
    {
        int total = 0;
        for (ResolvedOpportunity opportunity : opportunities)
        {
            if (opportunityType.equals(opportunity.type()) && opportunity.status() == status)
            {
                total += opportunity.latencyTicks();
            }
        }
        return total;
    }
}
