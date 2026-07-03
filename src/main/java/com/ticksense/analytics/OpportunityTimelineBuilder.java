package com.ticksense.analytics;

import com.ticksense.activities.OpportunityEvidence;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OpportunityTimelineBuilder
{
    private OpportunityTimelineBuilder()
    {
    }

    public interface Labeler
    {
        String labelFor(String opportunityType);
    }

    public static List<OpportunityTimelineEntry> build(List<ResolvedOpportunity> opportunities, Labeler labeler)
    {
        if (opportunities == null || opportunities.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<OpportunityTimelineEntry> timeline = new ArrayList<>(opportunities.size());
        for (ResolvedOpportunity opportunity : opportunities)
        {
            timeline.add(new OpportunityTimelineEntry(
                opportunity.type(),
                labeler.labelFor(opportunity.type()),
                opportunity.status().name(),
                opportunity.endTick(),
                opportunity.endWallTimeMillis(),
                opportunity.latencyTicks(),
                opportunity.latencyMillis(),
                evidenceDetails(opportunity.terminalEvidence())));
        }
        return Collections.unmodifiableList(timeline);
    }

    public static List<String> evidenceDetails(List<OpportunityEvidence> evidence)
    {
        if (evidence == null || evidence.isEmpty())
        {
            return Collections.emptyList();
        }
        final List<String> details = new ArrayList<>(evidence.size());
        for (OpportunityEvidence item : evidence)
        {
            details.add(item.getDetail().isEmpty() ? item.getSourceEventType() : item.getDetail());
        }
        return Collections.unmodifiableList(details);
    }
}
