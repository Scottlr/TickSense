package com.ticksense.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import com.ticksense.core.FinishReason;
import com.ticksense.core.FinishReasonType;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;

public class ActivityStrategyEngineTest
{
    @Test
    public void startsHighestConfidenceCandidate()
    {
        final List<ActivityMarker> markers = new ArrayList<>();
        final StubStrategy high = strategy(ActivityType.ARAXXOR, "Araxxor", 100, 0.90D, false, true);
        final StubStrategy low = strategy(ActivityType.GEM_MINING, "Gem Mining", 10, 0.70D, false, false);
        final ActivityStrategyEngine engine = engine(markers, true, high, low);

        engine.accept(envelope("event-1", 1_000L, 100));

        assertTrue(engine.getActiveSession().isPresent());
        assertEquals(ActivityType.ARAXXOR, engine.getActiveSession().get().getActivityType());
        assertEquals(1, markers.size());
        assertEquals("STARTED", markers.get(0).getMarkerType());
    }

    @Test
    public void keepsActiveStrategyUntilTermination()
    {
        final List<ActivityMarker> markers = new ArrayList<>();
        final StubStrategy active = strategy(ActivityType.GEM_MINING, "Gem Mining", 10, 0.80D, false, false);
        final StubStrategy stronger = strategy(ActivityType.ARAXXOR, "Araxxor", 100, 0.70D, false, true);
        final ActivityStrategyEngine engine = engine(markers, true, active, stronger);

        engine.accept(envelope("event-1", 1_000L, 100));
        active.setConfidence(0.20D);
        stronger.setConfidence(0.99D);

        engine.accept(envelope("event-2", 1_100L, 101));

        assertTrue(engine.getActiveSession().isPresent());
        assertEquals(ActivityType.GEM_MINING, engine.getActiveSession().get().getActivityType());
        assertEquals(1, markers.size());
    }

    @Test
    public void moreSpecificStrategyBeatsGenericCandidate()
    {
        final StubStrategy generic = strategy(ActivityType.GEM_MINING, "Generic Skilling", 10, 0.85D, false, false);
        final StubStrategy specific = strategy(ActivityType.CONSTRUCTION, "Construction", 50, 0.85D, false, false);
        final ActivityStrategyEngine engine = engine(new ArrayList<ActivityMarker>(), true, generic, specific);

        engine.accept(envelope("event-1", 1_000L, 100));

        assertTrue(engine.getActiveSession().isPresent());
        assertEquals(ActivityType.CONSTRUCTION, engine.getActiveSession().get().getActivityType());
    }

    @Test
    public void ambiguousCandidatesDoNotCreateNormalActivity()
    {
        final StubStrategy left = strategy(ActivityType.GEM_MINING, "Alpha", 10, 0.80D, false, false);
        final StubStrategy right = strategy(ActivityType.CONSTRUCTION, "Bravo", 10, 0.80D, false, false);
        final ActivityStrategyEngine engine = engine(new ArrayList<ActivityMarker>(), true, left, right);

        engine.accept(envelope("event-1", 1_000L, 100));

        assertFalse(engine.getActiveSession().isPresent());
        assertTrue(engine.getCompletedSessions().isEmpty());
        assertEquals(2, engine.getDiagnostics().size());
        assertEquals("AMBIGUOUS", engine.getDiagnostics().get(0).getDecision());
    }

    @Test
    public void noCandidateAboveThresholdDoesNotCreateNormalActivity()
    {
        final StubStrategy low = strategy(ActivityType.GEM_MINING, "Gem Mining", 10, 0.40D, false, false);
        final ActivityStrategyEngine engine = engine(new ArrayList<ActivityMarker>(), true, low);

        engine.accept(envelope("event-1", 1_000L, 100));

        assertFalse(engine.getActiveSession().isPresent());
        assertTrue(engine.getCompletedSessions().isEmpty());
        assertEquals(1, engine.getDiagnostics().size());
        assertEquals("NO_CONFIDENCE", engine.getDiagnostics().get(0).getDecision());
    }

    @Test
    public void emitsFinishReasonForTerminatedActivity()
    {
        final List<ActivityMarker> markers = new ArrayList<>();
        final StubStrategy strategy = strategy(ActivityType.ARAXXOR, "Araxxor", 100, 0.90D, true, true);
        strategy.setFinishReason(new FinishReason(
            FinishReasonType.BOSS_DEAD,
            time(1_200L, 102),
            0.95D,
            "Boss despawned",
            Arrays.asList("Boss absent", "Loot appeared")));
        final ActivityStrategyEngine engine = engine(markers, true, strategy);

        engine.accept(envelope("event-1", 1_000L, 100));
        engine.accept(envelope("event-2", 1_200L, 102));

        assertFalse(engine.getActiveSession().isPresent());
        assertEquals(1, engine.getCompletedSessions().size());
        assertEquals(FinishReasonType.BOSS_DEAD, engine.getCompletedSessions().get(0).getFinishReason().getType());
        assertEquals(2, markers.size());
        assertEquals("FINISHED", markers.get(1).getMarkerType());
    }

    @Test
    public void terminatedStrategyDoesNotImmediatelyRestartFromSameEvent()
    {
        final List<ActivityMarker> markers = new ArrayList<>();
        final StubStrategy strategy = strategy(ActivityType.ARAXXOR, "Araxxor", 100, 0.90D, true, true);
        strategy.setFinishReason(new FinishReason(
            FinishReasonType.BOSS_DEAD,
            time(1_200L, 102),
            0.95D,
            "Boss despawned",
            Arrays.asList("Boss absent")));
        final ActivityStrategyEngine engine = engine(markers, true, strategy);

        engine.accept(envelope("event-1", 1_000L, 100));
        engine.accept(envelope("event-2", 1_200L, 102));

        assertFalse(engine.getActiveSession().isPresent());
        assertEquals(1, engine.getCompletedSessions().size());
        assertEquals(2, markers.size());
    }

    private static ActivityStrategyEngine engine(
        List<ActivityMarker> markers,
        boolean diagnosticsEnabled,
        StubStrategy... strategies)
    {
        final ActivityRegistry.Builder builder = ActivityRegistry.builder();
        for (StubStrategy strategy : strategies)
        {
            builder.register(strategy);
        }
        return new ActivityStrategyEngine(builder.build(), markers::add, marker -> { }, diagnosticsEnabled);
    }

    private static StubStrategy strategy(
        ActivityType type,
        String displayName,
        int priority,
        double confidence,
        boolean bossActivity,
        boolean selectedEvidence)
    {
        return new StubStrategy(new ActivityDefinition(type, displayName, priority, 0.75D, bossActivity), confidence, selectedEvidence);
    }

    private static TelemetryEnvelope envelope(String eventId, long wallTimeMillis, int gameTick)
    {
        return TelemetryEnvelope.create(eventId, "session-1", new StubTelemetryEvent(time(wallTimeMillis, gameTick)));
    }

    private static EventTime time(long wallTimeMillis, int gameTick)
    {
        return new EventTime(wallTimeMillis, wallTimeMillis * 1_000_000L, gameTick, gameTick * 10L, gameTick);
    }

    private static final class StubTelemetryEvent implements TelemetryEvent
    {
        private final EventTime time;

        private StubTelemetryEvent(EventTime time)
        {
            this.time = time;
        }

        @Override
        public String getType()
        {
            return "test.event";
        }

        @Override
        public TelemetryCategory getCategory()
        {
            return TelemetryCategory.ENVIRONMENT_PERFORMANCE;
        }

        @Override
        public EventTime getTime()
        {
            return time;
        }

        @Override
        public Map<String, String> getTags()
        {
            return Collections.singletonMap("source", "TestEvent");
        }
    }

    private static final class StubStrategy implements ActivityStrategy
    {
        private final ActivityDefinition definition;
        private final boolean selectedEvidence;
        private double confidence;
        private FinishReason finishReason;

        private StubStrategy(ActivityDefinition definition, double confidence, boolean selectedEvidence)
        {
            this.definition = definition;
            this.confidence = confidence;
            this.selectedEvidence = selectedEvidence;
        }

        private void setConfidence(double confidence)
        {
            this.confidence = confidence;
        }

        private void setFinishReason(FinishReason finishReason)
        {
            this.finishReason = finishReason;
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
                ActivityId.of(definition.getActivityType().name().toLowerCase() + "-session"),
                definition.getActivityType(),
                confidence,
                Collections.singletonList(selectedEvidence ? "selected evidence" : "generic evidence"),
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
            if (finishReason != null && finishReason.getTime().equals(event.getTime()))
            {
                return Optional.of(finishReason);
            }
            return Optional.empty();
        }

        @Override
        public ActivityReportData buildActivityData(ActivityContext context, ActivitySession session)
        {
            return new ActivityReportData(session.getActivityId(), session.getActivityType(), Collections.singletonMap("built", "true"));
        }
    }
}
