package com.ticksense.core;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class FinishReasonTest
{
    @Test
    public void copiesEvidenceDefensively()
    {
        final List<String> evidence = new ArrayList<>();
        evidence.add("reward widget");

        final FinishReason reason = new FinishReason(
            FinishReasonType.REWARD_RECEIVED,
            time(),
            0.9D,
            "Reward screen opened",
            evidence);
        evidence.add("late mutation");

        assertEquals(1, reason.getEvidence().size());
        assertEquals("reward widget", reason.getEvidence().get(0));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void exposesImmutableEvidence()
    {
        final FinishReason reason = new FinishReason(
            FinishReasonType.COMPLETED,
            time(),
            1.0D,
            "Complete",
            java.util.Collections.singletonList("completion evidence"));

        reason.getEvidence().add("mutation");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsConfidenceOutsideUnitRange()
    {
        new FinishReason(FinishReasonType.UNKNOWN, time(), 1.1D, "Invalid", null);
    }

    private static EventTime time()
    {
        return new EventTime(1L, 2L, 3, 4L, 5);
    }
}
