package com.ticksense.analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

public class ExecutionScoreTest
{
    @Test
    public void clampsScoreBetweenZeroAndOneHundred()
    {
        assertEquals(0.0D, new ExecutionScore(-12.0D).getValue(), 0.0D);
        assertEquals(67.5D, new ExecutionScore(67.5D).getValue(), 0.0D);
        assertEquals(100.0D, new ExecutionScore(140.0D).getValue(), 0.0D);
    }

    @Test
    public void preservesPenaltyAndBonusComponents()
    {
        final ScoreBreakdown breakdown = new ScoreBreakdown(
            80.0D,
            Arrays.asList(
                ScoreBreakdown.penalty("lateClick", "Late click", 12.0D, "Clicked after the expected window"),
                ScoreBreakdown.bonus("fastRecover", "Fast recovery", 4.0D, "Recovered quickly after a miss")));

        assertEquals(2, breakdown.getComponents().size());
        assertTrue(breakdown.getComponents().get(0).isPenalty());
        assertFalse(breakdown.getComponents().get(1).isPenalty());
        assertEquals(-8.0D, breakdown.totalAdjustment(), 0.0D);
        assertEquals(72.0D, breakdown.getExecutionScore().getValue(), 0.0D);
    }
}
