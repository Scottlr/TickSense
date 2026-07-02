package com.ticksense.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import java.util.Arrays;
import org.junit.Test;

public class ActivityStrategyContractTest
{
    @Test
    public void candidateCarriesConfidenceAndEvidenceSummary()
    {
        final EventTime firstEvidenceTime = new EventTime(1_000L, 2_000L, 100, 300L, 4);
        final ActivityCandidate candidate = new ActivityCandidate(
            ActivityId.of("activity-1"),
            ActivityType.ARAXXOR,
            0.86D,
            Arrays.asList("Araxxor NPC present", "Instance evidence present"),
            firstEvidenceTime,
            true,
            "Higher-priority active strategy");

        assertEquals(ActivityType.ARAXXOR, candidate.getActivityType());
        assertEquals(0.86D, candidate.getConfidence(), 0.000001D);
        assertEquals(Arrays.asList("Araxxor NPC present", "Instance evidence present"), candidate.getEvidenceSummary());
        assertEquals(firstEvidenceTime, candidate.getFirstEvidenceTime());
        assertTrue(candidate.isSuppressed());
        assertEquals("Higher-priority active strategy", candidate.getSuppressionReason());
        assertTrue(candidate.isStrong(0.75D));
        assertFalse(candidate.isStrong(0.95D));
    }
}
