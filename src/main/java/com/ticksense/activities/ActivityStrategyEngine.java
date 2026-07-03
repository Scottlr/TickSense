package com.ticksense.activities;

import com.ticksense.common.ImmutableCollections;

import com.ticksense.core.ActivitySession;
import com.ticksense.core.EventTime;
import com.ticksense.core.FinishReason;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.TelemetrySink;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class ActivityStrategyEngine implements TelemetrySink
{
    private static final ActivityMarkerSink NO_OP_ACTIVITY_MARKER_SINK = marker -> { };
    private static final OpportunitySink NO_OP_OPPORTUNITY_SINK = marker -> { };

    private final ActivityRegistry registry;
    private final ActivityMarkerSink activityMarkerSink;
    private final OpportunitySink opportunitySink;
    private final boolean diagnosticsEnabled;
    private final AtomicLong markerSequence = new AtomicLong();
    private final ActivityDiagnosticBuffer diagnostics = new ActivityDiagnosticBuffer(50);
    private final List<ActivitySession> completedSessions = new ArrayList<>();
    private final List<ActivityReportData> completedActivityData = new ArrayList<>();

    private ActivityStrategy activeStrategy;
    private ActivitySession activeSession;

    public ActivityStrategyEngine(
        ActivityRegistry registry,
        ActivityMarkerSink activityMarkerSink,
        OpportunitySink opportunitySink,
        boolean diagnosticsEnabled)
    {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.activityMarkerSink = activityMarkerSink == null ? NO_OP_ACTIVITY_MARKER_SINK : activityMarkerSink;
        this.opportunitySink = opportunitySink == null ? NO_OP_OPPORTUNITY_SINK : opportunitySink;
        this.diagnosticsEnabled = diagnosticsEnabled;
    }

    @Override
    public synchronized void accept(TelemetryEnvelope envelope)
    {
        final TelemetryEnvelope normalizedEnvelope = Objects.requireNonNull(envelope, "envelope");
        final ActivityContext context = contextFor(normalizedEnvelope);
        final TelemetryEvent event = normalizedEnvelope.getEvent();

        final List<CandidateEvaluation> evaluations = new ArrayList<>(evaluateCandidates(context, event, activeStrategy));
        if (activeStrategy != null && activeSession != null)
        {
            activeStrategy.onEvent(context, activeSession, event, opportunitySink);
            emitSuppressedDiagnostics(evaluations, event.getTime(), activeStrategy.getDefinition().getActivityType(), "Active strategy persists");

            final Optional<FinishReason> finishReason = activeStrategy.evaluateTermination(context, activeSession, event);
            if (finishReason.isPresent())
            {
                final ActivityStrategy finishedStrategy = activeStrategy;
                finishActiveActivity(context, finishReason.get());
                evaluations.removeIf(evaluation -> evaluation.strategy == finishedStrategy);
            }
            else
            {
                return;
            }
        }

        final Optional<CandidateEvaluation> winner = chooseWinner(evaluations);
        if (!winner.isPresent())
        {
            emitNoStartDiagnostics(evaluations, event.getTime());
            return;
        }

        final CandidateEvaluation selected = winner.get();
        final List<CandidateEvaluation> conflicting = conflictingCandidates(selected, evaluations);
        if (!conflicting.isEmpty())
        {
            emitAmbiguousDiagnostics(selected, conflicting, event.getTime());
            return;
        }

        startActivity(context, selected);
        emitSuppressedDiagnostics(evaluations, event.getTime(), selected.strategy.getDefinition().getActivityType(), "Lower-ranked candidate");
    }

    public synchronized Optional<ActivitySession> getActiveSession()
    {
        return Optional.ofNullable(activeSession);
    }

    public synchronized List<ActivitySession> getCompletedSessions()
    {
        return ImmutableCollections.immutableList(completedSessions);
    }

    public synchronized List<ActivityReportData> getCompletedActivityData()
    {
        return ImmutableCollections.immutableList(completedActivityData);
    }

    public synchronized List<ActivityDiagnostic> getDiagnostics()
    {
        return diagnostics.snapshot();
    }

    private List<CandidateEvaluation> evaluateCandidates(ActivityContext context, TelemetryEvent event, ActivityStrategy excludedStrategy)
    {
        final List<CandidateEvaluation> evaluations = new ArrayList<>();
        for (ActivityStrategy strategy : registry.getStrategies())
        {
            if (strategy == excludedStrategy)
            {
                continue;
            }
            final ActivityCandidate candidate = strategy.evaluateActivation(context, event);
            if (candidate != null)
            {
                evaluations.add(new CandidateEvaluation(strategy, candidate));
            }
        }
        evaluations.sort(candidateComparator());
        return evaluations;
    }

    private Optional<CandidateEvaluation> chooseWinner(List<CandidateEvaluation> evaluations)
    {
        for (CandidateEvaluation evaluation : evaluations)
        {
            if (!evaluation.candidate.isSuppressed()
                && evaluation.candidate.isStrong(evaluation.strategy.getDefinition().getActivationThreshold()))
            {
                return Optional.of(evaluation);
            }
        }
        return Optional.empty();
    }

    private List<CandidateEvaluation> conflictingCandidates(CandidateEvaluation selected, List<CandidateEvaluation> evaluations)
    {
        final List<CandidateEvaluation> conflicts = new ArrayList<>();
        for (CandidateEvaluation evaluation : evaluations)
        {
            if (evaluation == selected
                || evaluation.candidate.isSuppressed()
                || !evaluation.candidate.isStrong(evaluation.strategy.getDefinition().getActivationThreshold()))
            {
                continue;
            }
            if (sameArbitrationRank(selected, evaluation))
            {
                conflicts.add(evaluation);
            }
        }
        return conflicts;
    }

    private boolean sameArbitrationRank(CandidateEvaluation left, CandidateEvaluation right)
    {
        return Double.compare(left.candidate.getConfidence(), right.candidate.getConfidence()) == 0
            && left.strategy.getDefinition().getPriority() == right.strategy.getDefinition().getPriority()
            && left.strategy.getDefinition().isBossActivity() == right.strategy.getDefinition().isBossActivity();
    }

    private void startActivity(ActivityContext context, CandidateEvaluation evaluation)
    {
        final ActivityCandidate candidate = evaluation.candidate;
        final ActivityDefinition definition = evaluation.strategy.getDefinition();
        final Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("displayName", definition.getDisplayName());
        metadata.put("confidence", String.valueOf(candidate.getConfidence()));
        if (!candidate.getEvidenceSummary().isEmpty())
        {
            metadata.put("evidenceSummary", String.join(" | ", candidate.getEvidenceSummary()));
        }

        final ActivitySession session = new ActivitySession(
            candidate.getActivityId(),
            definition.getActivityType(),
            candidate.getFirstEvidenceTime(),
            null,
            null,
            Collections.emptyList(),
            metadata);
        evaluation.strategy.onStart(context, session);
        activeStrategy = evaluation.strategy;
        activeSession = session;
        emitMarker(
            ActivityMarkerTypes.STARTED,
            session.getActivityId(),
            definition.getActivityType(),
            candidate.getFirstEvidenceTime(),
            metadata);
        emitDiagnostic(
            definition.getActivityType(),
            candidate.getConfidence(),
            ActivityMarkerTypes.STARTED,
            "",
            candidate.getFirstEvidenceTime(),
            candidate.getEvidenceSummary());
    }

    private void finishActiveActivity(ActivityContext context, FinishReason finishReason)
    {
        final ActivitySession finishedSession = activeSession.finish(finishReason);
        final ActivityStrategy finishedStrategy = activeStrategy;
        final ActivityReportData reportData = finishedStrategy.buildActivityData(context, finishedSession);

        completedSessions.add(finishedSession);
        completedActivityData.add(reportData);

        final Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("finishReasonType", finishReason.getType().name());
        metadata.put("finishConfidence", String.valueOf(finishReason.getConfidence()));
        if (!finishReason.getExplanation().isEmpty())
        {
            metadata.put("finishExplanation", finishReason.getExplanation());
        }
        if (!finishReason.getEvidence().isEmpty())
        {
            metadata.put("finishEvidence", String.join(" | ", finishReason.getEvidence()));
        }

        emitMarker(
            ActivityMarkerTypes.FINISHED,
            finishedSession.getActivityId(),
            finishedSession.getActivityType(),
            finishReason.getTime(),
            metadata);
        emitDiagnostic(
            finishedSession.getActivityType(),
            finishReason.getConfidence(),
            "TERMINATED",
            finishReason.getType().name(),
            finishReason.getTime(),
            finishReason.getEvidence());

        activeStrategy = null;
        activeSession = null;
    }

    private void emitSuppressedDiagnostics(
        List<CandidateEvaluation> evaluations,
        EventTime time,
        com.ticksense.core.ActivityType selectedType,
        String reason)
    {
        for (CandidateEvaluation evaluation : evaluations)
        {
            if (evaluation.strategy.getDefinition().getActivityType() != selectedType)
            {
                emitDiagnostic(
                    evaluation.strategy.getDefinition().getActivityType(),
                    evaluation.candidate.getConfidence(),
                    "SUPPRESSED",
                    reason,
                    time,
                    evaluation.candidate.getEvidenceSummary());
            }
        }
    }

    private void emitAmbiguousDiagnostics(CandidateEvaluation selected, List<CandidateEvaluation> conflicts, EventTime time)
    {
        emitDiagnostic(
            selected.strategy.getDefinition().getActivityType(),
            selected.candidate.getConfidence(),
            "AMBIGUOUS",
            "Competing candidates remain tied",
            time,
            selected.candidate.getEvidenceSummary());
        for (CandidateEvaluation conflict : conflicts)
        {
            emitDiagnostic(
                conflict.strategy.getDefinition().getActivityType(),
                conflict.candidate.getConfidence(),
                "AMBIGUOUS",
                "Competing candidates remain tied",
                time,
                conflict.candidate.getEvidenceSummary());
        }
    }

    private void emitNoStartDiagnostics(List<CandidateEvaluation> evaluations, EventTime time)
    {
        for (CandidateEvaluation evaluation : evaluations)
        {
            final String reason = evaluation.candidate.isSuppressed()
                ? evaluation.candidate.getSuppressionReason()
                : "Candidate below activation threshold";
            emitDiagnostic(
                evaluation.strategy.getDefinition().getActivityType(),
                evaluation.candidate.getConfidence(),
                evaluation.candidate.isSuppressed() ? "SUPPRESSED" : "NO_CONFIDENCE",
                reason,
                time,
                evaluation.candidate.getEvidenceSummary());
        }
    }

    private void emitMarker(
        String markerType,
        com.ticksense.core.ActivityId activityId,
        com.ticksense.core.ActivityType activityType,
        EventTime time,
        Map<String, String> metadata)
    {
        activityMarkerSink.accept(new ActivityMarker(
            "activity-marker-" + markerSequence.incrementAndGet(),
            activityId,
            activityType,
            markerType,
            time,
            metadata));
    }

    private void emitDiagnostic(
        com.ticksense.core.ActivityType activityType,
        double confidence,
        String decision,
        String reason,
        EventTime time,
        List<String> evidence)
    {
        if (!diagnosticsEnabled)
        {
            return;
        }
        diagnostics.add(new ActivityDiagnostic(activityType, confidence, decision, reason, time, evidence));
    }

    private ActivityContext contextFor(TelemetryEnvelope envelope)
    {
        final Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("eventType", envelope.getEvent().getType());
        metadata.put("category", envelope.getEvent().getCategory().name());
        return new ActivityContext(envelope.getSessionId(), -1, diagnosticsEnabled, metadata);
    }

    private static Comparator<CandidateEvaluation> candidateComparator()
    {
        return Comparator
            .comparingDouble((CandidateEvaluation evaluation) -> evaluation.candidate.getConfidence()).reversed()
            .thenComparing(Comparator.comparingInt(
                (CandidateEvaluation evaluation) -> evaluation.strategy.getDefinition().getPriority()).reversed())
            .thenComparing(Comparator.comparing(
                (CandidateEvaluation evaluation) -> evaluation.strategy.getDefinition().isBossActivity()).reversed())
            .thenComparing(evaluation -> evaluation.strategy.getDefinition().getDisplayName())
            .thenComparing(evaluation -> evaluation.strategy.getDefinition().getActivityType().name());
    }

    private static final class CandidateEvaluation
    {
        private final ActivityStrategy strategy;
        private final ActivityCandidate candidate;

        private CandidateEvaluation(ActivityStrategy strategy, ActivityCandidate candidate)
        {
            this.strategy = strategy;
            this.candidate = candidate;
        }
    }
}
