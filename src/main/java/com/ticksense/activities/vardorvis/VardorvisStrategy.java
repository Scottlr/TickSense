package com.ticksense.activities.vardorvis;

import com.ticksense.activities.ActivityCandidate;
import com.ticksense.activities.ActivityContext;
import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.activities.OpportunitySink;
import com.ticksense.activities.OpportunityLifecycle;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.ActivityType;
import com.ticksense.core.FinishReason;
import com.ticksense.core.FinishReasonType;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.DamageTelemetryEvent;
import com.ticksense.telemetry.events.MovementTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import com.ticksense.telemetry.events.ProjectileTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import java.util.Collections;
import java.util.Optional;

public final class VardorvisStrategy implements ActivityStrategy
{
    private static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.VARDORVIS, "Vardorvis", 30, 0.75D, true);

    private final VardorvisState state;

    public VardorvisStrategy()
    {
        this(
            VardorvisIds.verificationDecision(),
            VardorvisIds.bossNpcIds(),
            VardorvisIds.headNpcIds(),
            VardorvisIds.rangedHeadProjectileIds(),
            VardorvisIds.bloodSplatGraphicIds(),
            VardorvisIds.axeMechanicIds(),
            VardorvisIds.verifiedRegionIds());
    }

    public VardorvisStrategy(
        VardorvisVerificationDecision verificationDecision,
        int[] bossNpcIds,
        int[] headNpcIds,
        int[] rangedHeadProjectileIds,
        int[] bloodSplatGraphicIds,
        int[] axeMechanicIds,
        int[] verifiedRegionIds)
    {
        this.state = new VardorvisState(
            verificationDecision,
            bossNpcIds,
            headNpcIds,
            rangedHeadProjectileIds,
            bloodSplatGraphicIds,
            axeMechanicIds,
            verifiedRegionIds);
    }

    @Override
    public ActivityDefinition getDefinition()
    {
        return DEFINITION;
    }

    @Override
    public ActivityCandidate evaluateActivation(ActivityContext context, TelemetryEvent event)
    {
        updatePassiveState(event);

        if (!state.allowsNormalReports() || !(event instanceof ProjectileTelemetryEvent))
        {
            return null;
        }

        final ProjectileTelemetryEvent projectile = (ProjectileTelemetryEvent) event;
        if (!state.canActivateFromRangedHead(projectile))
        {
            return null;
        }

        state.noteActivationProjectile(projectile);
        return new ActivityCandidate(
            ActivityId.of("vardorvis-" + context.getSessionId() + "-" + projectile.getTime().getGameTick()),
            ActivityType.VARDORVIS,
            0.94D,
            state.activationEvidence(projectile),
            state.activationStartTime(projectile),
            false,
            "");
    }

    @Override
    public void onStart(ActivityContext context, ActivitySession session)
    {
        state.startActivity(session.getActivityId());
    }

    @Override
    public void onEvent(ActivityContext context, ActivitySession session, TelemetryEvent event, OpportunitySink sink)
    {
        state.ensureOpportunityLifecycle(new OpportunityLifecycle(sink));
        state.noteReusableExecutionEvent(context, session, event);
        state.flushActivationDerivedOpportunities();

        if (event instanceof RegionInstanceTelemetryEvent)
        {
            final RegionInstanceTelemetryEvent region = (RegionInstanceTelemetryEvent) event;
            state.updateRegion(region.getLocalPlayerLocation());
            state.expireTimedOut(event.getTime());
            return;
        }
        if (event instanceof ProjectileTelemetryEvent)
        {
            state.noteProjectile((ProjectileTelemetryEvent) event);
            state.expireTimedOut(event.getTime());
            return;
        }
        if (event instanceof MovementTelemetryEvent)
        {
            final MovementTelemetryEvent movement = (MovementTelemetryEvent) event;
            state.updateRegion(movement.getToLocation());
            state.noteMovementResponse(movement);
            state.expireTimedOut(event.getTime());
            return;
        }
        if (event instanceof PlayerActionTelemetryEvent)
        {
            state.noteActionResponse((PlayerActionTelemetryEvent) event);
            state.expireTimedOut(event.getTime());
            return;
        }
        if (event instanceof DamageTelemetryEvent)
        {
            state.noteDamage((DamageTelemetryEvent) event);
        }
        state.expireTimedOut(event.getTime());
    }

    @Override
    public Optional<FinishReason> evaluateTermination(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        if (!(event instanceof RegionInstanceTelemetryEvent))
        {
            return Optional.empty();
        }

        final RegionInstanceTelemetryEvent region = (RegionInstanceTelemetryEvent) event;
        state.updateRegion(region.getLocalPlayerLocation());

        if (!"LOGGED_IN".equals(region.getGameState()))
        {
            final FinishReasonType type = "HOPPING".equals(region.getGameState()) ? FinishReasonType.HOPPED_WORLD : FinishReasonType.LOGGED_OUT;
            final FinishReason reason = new FinishReason(
                type,
                region.getTime(),
                0.95D,
                "Vardorvis ended because the client left the logged-in state.",
                Collections.singletonList("Region/game-state evidence changed to " + region.getGameState() + "."));
            state.cancelOpenOpportunities(region.getTime(), reason.getExplanation());
            return Optional.of(reason);
        }

        if (!state.isVerifiedRegion(region.getRegionId()))
        {
            final FinishReason reason = new FinishReason(
                FinishReasonType.LEFT_REGION,
                region.getTime(),
                0.92D,
                "Vardorvis ended because the player left the verified Vardorvis region.",
                Collections.singletonList("Region changed from verified Vardorvis context to " + region.getRegionId() + "."));
            state.cancelOpenOpportunities(region.getTime(), reason.getExplanation());
            return Optional.of(reason);
        }

        return Optional.empty();
    }

    @Override
    public ActivityReportData buildActivityData(ActivityContext context, ActivitySession session)
    {
        final ActivityReportData data = new ActivityReportData(session.getActivityId(), session.getActivityType(), state.snapshotAttributes());
        state.resetForNextSession();
        return data;
    }

    private void updatePassiveState(TelemetryEvent event)
    {
        if (event instanceof RegionInstanceTelemetryEvent)
        {
            state.updateRegion(((RegionInstanceTelemetryEvent) event).getLocalPlayerLocation());
        }
        else if (event instanceof MovementTelemetryEvent)
        {
            state.updateRegion(((MovementTelemetryEvent) event).getToLocation());
        }
    }
}
