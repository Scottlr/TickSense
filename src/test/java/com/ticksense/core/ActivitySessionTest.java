package com.ticksense.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class ActivitySessionTest
{
    @Test
    public void finishReturnsNewCompletedSession()
    {
        final ActivitySession open = new ActivitySession(
            ActivityId.of("activity-1"),
            ActivityType.GEM_MINING,
            start(),
            null,
            null,
            null,
            null);
        final FinishReason finishReason = new FinishReason(
            FinishReasonType.COMPLETED,
            end(),
            0.95D,
            "Activity completed",
            java.util.Collections.singletonList("completion marker"));

        final ActivitySession finished = open.finish(finishReason);

        assertFalse(open.isFinished());
        assertTrue(finished.isFinished());
        assertEquals(end(), finished.getEndTime());
        assertEquals(finishReason, finished.getFinishReason());
    }

    @Test
    public void copiesCollectionsDefensively()
    {
        final List<ActivitySpan> spans = new ArrayList<>();
        spans.add(new ActivitySpan("phase", start(), null, null));
        final Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("source", "test");

        final ActivitySession session = new ActivitySession(
            ActivityId.of("activity-2"),
            ActivityType.ARAXXOR,
            start(),
            null,
            null,
            spans,
            metadata);
        spans.clear();
        metadata.put("late", "mutation");

        assertEquals(1, session.getSpans().size());
        assertEquals(1, session.getMetadata().size());
        assertEquals("test", session.getMetadata().get("source"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void exposesImmutableSpans()
    {
        final ActivitySession session = new ActivitySession(
            ActivityId.of("activity-3"),
            ActivityType.INFERNO,
            start(),
            null,
            null,
            java.util.Collections.singletonList(new ActivitySpan("wave", start(), null, null)),
            null);

        session.getSpans().add(new ActivitySpan("mutation", start(), null, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMismatchedFinishState()
    {
        new ActivitySession(
            ActivityId.of("activity-4"),
            ActivityType.VARDORVIS,
            start(),
            end(),
            null,
            null,
            null);
    }

    private static EventTime start()
    {
        return new EventTime(100L, 200L, 10, 20L, 30);
    }

    private static EventTime end()
    {
        return new EventTime(700L, 800L, 11, 21L, 31);
    }
}
