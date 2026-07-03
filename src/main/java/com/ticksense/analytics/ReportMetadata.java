package com.ticksense.analytics;

import com.ticksense.common.TextValues;
import com.ticksense.core.ActivitySession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReportMetadata
{
    private ReportMetadata()
    {
    }

    public static String displayName(ActivitySession session, String fallbackDisplayName)
    {
        final String displayName = TextValues.trimmedOrEmpty(session.getMetadata().get("displayName"));
        return displayName.isEmpty() ? TextValues.requireText(fallbackDisplayName, "fallbackDisplayName") : displayName;
    }

    public static double confidence(ActivitySession session)
    {
        final String raw = TextValues.trimmedOrEmpty(session.getMetadata().get("confidence"));
        if (raw.isEmpty())
        {
            return 0.0D;
        }
        return Double.parseDouble(raw);
    }

    public static List<String> startEvidence(ActivitySession session)
    {
        final String startEvidence = TextValues.trimmedOrEmpty(session.getMetadata().get("evidenceSummary"));
        if (startEvidence.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<String> evidence = new ArrayList<>();
        for (String part : startEvidence.split("\\|"))
        {
            final String trimmed = part.trim();
            if (!trimmed.isEmpty())
            {
                evidence.add(trimmed);
            }
        }
        return Collections.unmodifiableList(evidence);
    }
}
