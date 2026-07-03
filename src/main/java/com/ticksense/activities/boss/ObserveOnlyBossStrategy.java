package com.ticksense.activities.boss;

import com.ticksense.activities.ActivityCandidate;
import com.ticksense.activities.ActivityContext;
import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.activities.OpportunitySink;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.FinishReason;
import com.ticksense.telemetry.ObservedTelemetryId;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.TelemetryIdExtractor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntPredicate;

public final class ObserveOnlyBossStrategy implements ActivityStrategy
{
    private static final double OBSERVE_ONLY_CONFIDENCE = 0.74D;

    private final ActivityDefinition definition;
    private final IntPredicate bossNpcMatcher;

    public ObserveOnlyBossStrategy(ActivityDefinition definition, IntPredicate bossNpcMatcher)
    {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.bossNpcMatcher = Objects.requireNonNull(bossNpcMatcher, "bossNpcMatcher");
    }

    @Override
    public ActivityDefinition getDefinition()
    {
        return definition;
    }

    @Override
    public ActivityCandidate evaluateActivation(ActivityContext context, TelemetryEvent event)
    {
        final List<ObservedTelemetryId> ids = TelemetryIdExtractor.extract(event);
        if (!containsBossNpc(ids))
        {
            return null;
        }
        return new ActivityCandidate(
            ActivityId.of(context.getSessionId() + "-" + definition.getActivityType().name().toLowerCase(Locale.ROOT) + "-observe"),
            definition.getActivityType(),
            OBSERVE_ONLY_CONFIDENCE,
            evidence(ids),
            event.getTime(),
            true,
            "Observe-only boss stub; reports stay disabled until replay fixtures verify mechanics");
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

    private boolean containsBossNpc(List<ObservedTelemetryId> ids)
    {
        for (ObservedTelemetryId id : ids)
        {
            if (id.getKind().contains("npc") && bossNpcMatcher.test(id.getId()))
            {
                return true;
            }
        }
        return false;
    }

    private List<String> evidence(List<ObservedTelemetryId> ids)
    {
        final List<String> evidence = new ArrayList<>();
        for (ObservedTelemetryId id : ids)
        {
            if (id.getKind().contains("npc") && bossNpcMatcher.test(id.getId()))
            {
                evidence.add("Known boss NPC observed: " + id.getKind() + ":" + id.getId());
            }
            else if (!"region".equals(id.getKind()))
            {
                evidence.add("Unverified event ID observed: " + id.getKind() + ":" + id.getId());
            }
        }
        return evidence.isEmpty() ? Collections.singletonList("Known boss context observed.") : Collections.unmodifiableList(evidence);
    }
}
