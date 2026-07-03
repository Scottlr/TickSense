package com.ticksense.replay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ticksense.activities.ActivityCandidate;
import com.ticksense.activities.ActivityContext;
import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityRegistry;
import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.activities.OpportunitySink;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.ActivityType;
import com.ticksense.core.FinishReason;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class ReplayRegressionTest
{
    @Test
    public void gemMiningBasicMatchesGoldenReport() throws IOException
    {
        final TimelineReplayRunner replayRunner = new TimelineReplayRunner();

        final ActivityReport report = replayRunner.run("replays/gem-mining-basic.jsonl");

        GoldenReportAssert.matches("golden/gem-mining-basic-report.json", report);
    }

    @Test
    public void constructionBasicMatchesGoldenReport() throws IOException
    {
        final TimelineReplayRunner replayRunner = new TimelineReplayRunner();

        final ActivityReport report = replayRunner.run("replays/construction-basic.jsonl");

        GoldenReportAssert.matches("golden/construction-basic-report.json", report);
    }

    @Test
    public void ambiguousEvidenceDoesNotCreateNormalReport() throws IOException
    {
        final TimelineReplayRunner replayRunner = new TimelineReplayRunner();

        final TimelineReplayRunner.ReplayResult result = replayRunner.replay("replays/ambiguous-low-confidence-no-report.jsonl");

        assertTrue(result.getReports().isEmpty());
        assertTrue(result.getCompletedSessions().isEmpty());
        assertFalse(result.getDiagnostics().isEmpty());
        assertEquals("NO_CONFIDENCE", result.getDiagnostics().get(0).getDecision());
    }

    @Test
    public void delayedInventoryUpdateStillProducesStableGemMiningReport()
    {
        final TestEvents events = new TestEvents("delayed-inventory");
        final WorldLocation rock = TestEvents.location(2841, 9388, 11410);
        final List<TelemetryEnvelope> replay = Arrays.asList(
            events.region(200, 11410, TestEvents.location(2840, 9388, 11410), "LOGGED_IN"),
            events.availableRock(200, 11380, rock),
            events.mineClick(201, rock),
            events.miningAnimation(202, 624),
            events.inventoryGain(203, 4, 1619),
            events.depletedRock(204, 11380, rock),
            events.region(210, 12000, TestEvents.location(3200, 3200, 12000), "LOGGED_IN"));

        final ActivityReport report = new TimelineReplayRunner().replay(replay).requireSingleReport();

        assertEquals(1.0D, report.getMetrics().get("rockResponse").getValue(), 0.0D);
        assertEquals(1.0D, report.getMetrics().get("idleTicks").getValue(), 0.0D);
        assertEquals(0.0D, report.getMetrics().get("redundantClicks").getValue(), 0.0D);
    }

    @Test
    public void duplicateClicksAreCountedAsExecutionLoss()
    {
        final TestEvents events = new TestEvents("duplicate-clicks");
        final WorldLocation rock = TestEvents.location(2841, 9388, 11410);
        final List<TelemetryEnvelope> replay = Arrays.asList(
            events.region(200, 11410, TestEvents.location(2840, 9388, 11410), "LOGGED_IN"),
            events.availableRock(200, 11380, rock),
            events.mineClick(201, rock),
            events.mineClick(202, rock),
            events.miningAnimation(203, 624),
            events.depletedRock(204, 11380, rock),
            events.region(210, 12000, TestEvents.location(3200, 3200, 12000), "LOGGED_IN"));

        final ActivityReport report = new TimelineReplayRunner().replay(replay).requireSingleReport();

        assertEquals(1.0D, report.getMetrics().get("redundantClicks").getValue(), 0.0D);
        assertTrue(report.getTickLossBreakdown().getTotalTickLoss() >= 1);
    }

    @Test
    public void gemMiningRngWaitStaysOutOfExecutionLoss()
    {
        final TestEvents events = new TestEvents("rng-separation");
        final WorldLocation rock = TestEvents.location(2841, 9388, 11410);
        final List<TelemetryEnvelope> replay = Arrays.asList(
            events.region(200, 11410, TestEvents.location(2840, 9388, 11410), "LOGGED_IN"),
            events.availableRock(200, 11380, rock),
            events.mineClick(200, rock),
            events.miningAnimation(201, 624),
            events.depletedRock(202, 11380, rock),
            events.availableRock(230, 11380, rock),
            events.mineClick(231, rock),
            events.miningXp(232, 12345, 65),
            events.depletedRock(233, 11380, rock),
            events.region(240, 12000, TestEvents.location(3200, 3200, 12000), "LOGGED_IN"));

        final ActivityReport report = new TimelineReplayRunner().replay(replay).requireSingleReport();

        assertEquals(1.0D, report.getMetrics().get("idleTicks").getValue(), 0.0D);
        assertEquals(0.5D, report.getMetrics().get("rockResponse").getValue(), 0.0D);
        assertTrue(report.getEvidenceSummary().get(3).contains("depleted-rock wait"));
    }

    @Test
    public void logoutEndsActiveSessionWithoutProducingBogusOpportunityCompletion()
    {
        final TestEvents events = new TestEvents("logout-mid-session");
        final WorldLocation rock = TestEvents.location(2841, 9388, 11410);
        final List<TelemetryEnvelope> replay = Arrays.asList(
            events.region(200, 11410, TestEvents.location(2840, 9388, 11410), "LOGGED_IN"),
            events.availableRock(200, 11380, rock),
            events.mineClick(201, rock),
            events.miningAnimation(202, 624),
            events.depletedRock(203, 11380, rock),
            events.region(204, 11410, TestEvents.location(2840, 9388, 11410), "LOADING"));

        final ActivityReport report = new TimelineReplayRunner().replay(replay).requireSingleReport();

        assertEquals("LOGGED_OUT", report.getFinishReason().getType().name());
        assertTrue(report.getOpportunities().stream().noneMatch(opportunity -> "CANCELLED".equals(opportunity.getStatus())));
    }

    @Test
    public void ambiguousSyntheticStrategiesSuppressNormalReports()
    {
        final ActivityRegistry registry = ActivityRegistry.builder()
            .register(new TiedStrategy(ActivityType.ARAXXOR, "left"))
            .register(new TiedStrategy(ActivityType.UNKNOWN, "right"))
            .build();
        final TimelineReplayRunner replayRunner = new TimelineReplayRunner(registry);
        final List<TelemetryEnvelope> replay = Collections.singletonList(
            new TestEvents("ambiguous-synthetic").region(200, 11410, TestEvents.location(2840, 9388, 11410), "LOGGED_IN"));

        final TimelineReplayRunner.ReplayResult result = replayRunner.replay(replay);

        assertTrue(result.getReports().isEmpty());
        assertEquals(2, result.getDiagnostics().size());
        assertEquals("AMBIGUOUS", result.getDiagnostics().get(0).getDecision());
        assertEquals("AMBIGUOUS", result.getDiagnostics().get(1).getDecision());
    }

    private static final class TiedStrategy implements ActivityStrategy
    {
        private final ActivityDefinition definition;

        private TiedStrategy(ActivityType activityType, String name)
        {
            this.definition = new ActivityDefinition(activityType, name, 10, 0.75D, false);
        }

        @Override
        public ActivityDefinition getDefinition()
        {
            return definition;
        }

        @Override
        public ActivityCandidate evaluateActivation(ActivityContext context, TelemetryEvent event)
        {
            return new ActivityCandidate(
                ActivityId.of(definition.getDisplayName() + "-" + event.getTime().getGameTick()),
                definition.getActivityType(),
                0.90D,
                Collections.singletonList(definition.getDisplayName()),
                event.getTime(),
                false,
                "");
        }

        @Override
        public void onStart(ActivityContext context, ActivitySession session)
        {
        }

        @Override
        public void onEvent(ActivityContext context, ActivitySession session, TelemetryEvent event, OpportunitySink sink)
        {
        }

        @Override
        public Optional<FinishReason> evaluateTermination(ActivityContext context, ActivitySession session, TelemetryEvent event)
        {
            return Optional.empty();
        }

        @Override
        public ActivityReportData buildActivityData(ActivityContext context, ActivitySession session)
        {
            return new ActivityReportData(session.getActivityId(), session.getActivityType(), Collections.emptyMap());
        }
    }
}
