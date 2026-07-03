package com.ticksense.analytics;

import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.OpportunityStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class OpportunityMarkerResolver
{
    private OpportunityMarkerResolver()
    {
    }

    public static List<ResolvedOpportunity> resolve(List<OpportunityMarker> markers)
    {
        if (markers == null || markers.isEmpty())
        {
            return Collections.emptyList();
        }

        final Map<String, OpportunityMarker> openMarkers = new LinkedHashMap<>();
        final List<ResolvedOpportunity> resolved = new ArrayList<>();
        for (OpportunityMarker marker : markers)
        {
            if (marker.getStatus() == OpportunityStatus.OPEN)
            {
                openMarkers.put(marker.getOpportunityInstanceId(), marker);
                continue;
            }

            final OpportunityMarker open = openMarkers.get(marker.getOpportunityInstanceId());
            if (open != null)
            {
                resolved.add(new ResolvedOpportunity(open, marker));
            }
        }
        resolved.sort(Comparator.comparingInt(ResolvedOpportunity::endTick).thenComparing(ResolvedOpportunity::type));
        return Collections.unmodifiableList(resolved);
    }
}
