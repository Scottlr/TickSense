package com.ticksense.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class OpportunityLifecycleTest
{
    @Test
    public void completesOpportunityWithLatency()
    {
        final List<OpportunityMarker> markers = new ArrayList<>();
        final OpportunityLifecycle lifecycle = new OpportunityLifecycle(markers::add);
        final OpportunityInstance instance = lifecycle.start(
            ActivityId.of("activity-1"),
            definition(1_500L),
            time(1_000L, 100),
            Collections.singletonMap("phase", "start"));

        final OpportunityEvidence clicked = evidence(1_200L, 101, "MenuOptionClicked", EvidenceStrength.MODERATE, "Clicked target");
        final OpportunityEvidence confirmed = evidence(1_800L, 103, "InteractingChanged", EvidenceStrength.CONFIRMING, "Interaction confirmed");
        lifecycle.complete(instance.getInstanceId(), time(1_800L, 103), Arrays.asList(clicked, confirmed));

        assertEquals(OpportunityStatus.COMPLETED, instance.getStatus());
        assertEquals(800L, instance.latencyMillis());
        assertEquals(3, instance.latencyTicks());
        assertEquals(Arrays.asList(clicked, confirmed), instance.getEvidence());
        assertEquals(2, markers.size());
        assertEquals(OpportunityStatus.OPEN, markers.get(0).getStatus());
        assertEquals(OpportunityStatus.COMPLETED, markers.get(1).getStatus());
        assertEquals(instance.getInstanceId(), markers.get(1).getOpportunityInstanceId());
    }

    @Test
    public void failsOpportunityOnFailureEvidence()
    {
        final List<OpportunityMarker> markers = new ArrayList<>();
        final OpportunityLifecycle lifecycle = new OpportunityLifecycle(markers::add);
        final OpportunityInstance instance = lifecycle.start(ActivityId.of("activity-1"), definition(1_500L), time(1_000L, 100), Collections.emptyMap());
        final OpportunityEvidence damage = evidence(1_400L, 102, "HitsplatApplied", EvidenceStrength.STRONG, "Damage taken");

        lifecycle.fail(instance.getInstanceId(), time(1_400L, 102), Collections.singletonList(damage));

        assertEquals(OpportunityStatus.FAILED, instance.getStatus());
        assertEquals(Collections.singletonList(damage), instance.getEvidence());
        assertEquals(OpportunityStatus.FAILED, markers.get(1).getStatus());
    }

    @Test
    public void expiresOpenOpportunityAfterTimeout()
    {
        final List<OpportunityMarker> markers = new ArrayList<>();
        final OpportunityLifecycle lifecycle = new OpportunityLifecycle(markers::add);
        final OpportunityInstance instance = lifecycle.start(ActivityId.of("activity-1"), definition(600L), time(1_000L, 100), Collections.emptyMap());

        final List<OpportunityInstance> expired = lifecycle.expireTimedOut(time(1_700L, 102));

        assertEquals(1, expired.size());
        assertEquals(instance, expired.get(0));
        assertEquals(OpportunityStatus.EXPIRED, instance.getStatus());
        assertEquals(OpportunityStatus.EXPIRED, markers.get(1).getStatus());
    }

    @Test
    public void cancelsOpenOpportunitiesOnActivityTermination()
    {
        final OpportunityLifecycle lifecycle = new OpportunityLifecycle();
        final ActivityId activityId = ActivityId.of("activity-1");
        final OpportunityInstance first = lifecycle.start(activityId, definition(1_000L), time(1_000L, 100), Collections.emptyMap());
        final OpportunityInstance second = lifecycle.start(activityId, definition(1_000L), time(1_100L, 101), Collections.emptyMap());
        final OpportunityInstance other = lifecycle.start(ActivityId.of("activity-2"), definition(1_000L), time(1_200L, 102), Collections.emptyMap());

        final List<OpportunityInstance> cancelled = lifecycle.cancelOpenOpportunities(
            activityId,
            time(1_900L, 105),
            Collections.singletonList(evidence(1_900L, 105, "FinishReason", EvidenceStrength.CONFIRMING, "Activity ended")));

        assertEquals(2, cancelled.size());
        assertEquals(OpportunityStatus.CANCELLED, first.getStatus());
        assertEquals(OpportunityStatus.CANCELLED, second.getStatus());
        assertEquals(OpportunityStatus.OPEN, other.getStatus());
    }

    @Test
    public void doesNotOverwriteTerminalStatus()
    {
        final List<OpportunityMarker> markers = new ArrayList<>();
        final OpportunityLifecycle lifecycle = new OpportunityLifecycle(markers::add);
        final OpportunityInstance instance = lifecycle.start(ActivityId.of("activity-1"), definition(1_000L), time(1_000L, 100), Collections.emptyMap());

        lifecycle.complete(instance.getInstanceId(), time(1_300L, 101), Collections.singletonList(
            evidence(1_300L, 101, "InteractingChanged", EvidenceStrength.CONFIRMING, "Completed")));
        lifecycle.fail(instance.getInstanceId(), time(1_600L, 103), Collections.singletonList(
            evidence(1_600L, 103, "HitsplatApplied", EvidenceStrength.STRONG, "Late failure")));

        assertEquals(OpportunityStatus.COMPLETED, instance.getStatus());
        assertEquals(2, markers.size());
        assertTrue(instance.getEvidence().get(0).getDetail().contains("Completed"));
    }

    private static OpportunityDefinition definition(long timeoutMillis)
    {
        return new OpportunityDefinition(
            "SPIDER_ENGAGEMENT",
            "Spider engagement",
            ActivityType.ARAXXOR,
            timeoutMillis,
            Arrays.asList("Attack spider", "Re-engage boss"));
    }

    private static OpportunityEvidence evidence(
        long wallTimeMillis,
        int gameTick,
        String sourceEventType,
        EvidenceStrength strength,
        String detail)
    {
        return new OpportunityEvidence(time(wallTimeMillis, gameTick), sourceEventType, strength, detail);
    }

    private static EventTime time(long wallTimeMillis, int gameTick)
    {
        return new EventTime(wallTimeMillis, wallTimeMillis * 1_000_000L, gameTick, gameTick * 30L, gameTick * 2);
    }
}
