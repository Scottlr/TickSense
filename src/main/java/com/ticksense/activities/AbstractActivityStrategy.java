package com.ticksense.activities;

import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.EventTime;
import com.ticksense.core.FinishReason;
import com.ticksense.core.FinishReasonType;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import java.util.Collections;
import java.util.Objects;

public abstract class AbstractActivityStrategy<S extends ActivityStateSupport> implements ActivityStrategy
{
    private final ActivityDefinition definition;
    private final String activityIdPrefix;
    protected final S state;

    protected AbstractActivityStrategy(ActivityDefinition definition, String activityIdPrefix, S state)
    {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.activityIdPrefix = com.ticksense.common.TextValues.requireText(activityIdPrefix, "activityIdPrefix");
        this.state = Objects.requireNonNull(state, "state");
    }

    @Override
    public final ActivityDefinition getDefinition()
    {
        return definition;
    }

    @Override
    public void onStart(ActivityContext context, ActivitySession session)
    {
        state.startActivity(session.getActivityId());
    }

    @Override
    public final void onEvent(ActivityContext context, ActivitySession session, TelemetryEvent event, OpportunitySink sink)
    {
        state.ensureOpportunityLifecycle(new OpportunityLifecycle(sink));
        onActivityEvent(context, session, event);
    }

    @Override
    public ActivityReportData buildActivityData(ActivityContext context, ActivitySession session)
    {
        final ActivityReportData data =
            new ActivityReportData(session.getActivityId(), session.getActivityType(), state.snapshotAttributes());
        state.resetForNextSession();
        return data;
    }

    protected abstract void onActivityEvent(ActivityContext context, ActivitySession session, TelemetryEvent event);

    protected final ActivityId activityId(ActivityContext context, TelemetryEvent event)
    {
        return ActivityId.of(activityIdPrefix + "-" + context.getSessionId() + "-" + event.getTime().getGameTick());
    }

    protected final FinishReason loggedOutOrHoppedReason(String activityName, RegionInstanceTelemetryEvent region)
    {
        final FinishReasonType type =
            "HOPPING".equals(region.getGameState()) ? FinishReasonType.HOPPED_WORLD : FinishReasonType.LOGGED_OUT;
        return new FinishReason(
            type,
            region.getTime(),
            0.95D,
            activityName + " ended because the client left the logged-in state.",
            Collections.singletonList("Region/game-state evidence changed to " + region.getGameState() + "."));
    }

    protected final FinishReason leftRegionReason(
        FinishReasonType type,
        String activityName,
        RegionInstanceTelemetryEvent region,
        double confidence)
    {
        return new FinishReason(
            type,
            region.getTime(),
            confidence,
            activityName + " ended because the player left the verified " + activityName + " region.",
            Collections.singletonList("Region changed from verified " + activityName + " context to " + region.getRegionId() + "."));
    }

    protected final FinishReason finishReason(
        FinishReasonType type,
        EventTime time,
        double confidence,
        String explanation,
        String evidence)
    {
        return new FinishReason(type, time, confidence, explanation, Collections.singletonList(evidence));
    }
}
