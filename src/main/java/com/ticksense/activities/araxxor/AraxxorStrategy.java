package com.ticksense.activities.araxxor;

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
import com.ticksense.telemetry.events.InteractingChangedTelemetryEvent;
import com.ticksense.telemetry.events.NpcStateTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import java.util.Collections;
import java.util.Optional;

public final class AraxxorStrategy implements ActivityStrategy
{
    private static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.ARAXXOR, "Araxxor", 40, 0.75D, true);

    private final AraxxorState state;

    public AraxxorStrategy()
    {
        this(
            AraxxorVerificationDecision.current(),
            AraxxorIds.araxxorNpcIds(),
            AraxxorIds.spiderNpcIds(),
            AraxxorIds.verifiedRegionIds());
    }

    public AraxxorStrategy(
        AraxxorVerificationDecision verificationDecision,
        int[] araxxorNpcIds,
        int[] spiderNpcIds,
        int[] verifiedRegionIds)
    {
        this.state = new AraxxorState(
            verificationDecision,
            araxxorNpcIds,
            spiderNpcIds,
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

        if (!state.allowsNormalStrategyEnablement() || !(event instanceof InteractingChangedTelemetryEvent))
        {
            return null;
        }

        final InteractingChangedTelemetryEvent interaction = (InteractingChangedTelemetryEvent) event;
        if (!state.canActivateFromBossInteraction(interaction))
        {
            return null;
        }

        state.noteActivationInteraction(interaction);
        return new ActivityCandidate(
            ActivityId.of("araxxor-" + context.getSessionId() + "-" + interaction.getTime().getGameTick()),
            ActivityType.ARAXXOR,
            0.93D,
            state.activationEvidence(interaction),
            state.activationStartTime(interaction),
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

        if (event instanceof RegionInstanceTelemetryEvent)
        {
            state.updateRegion(((RegionInstanceTelemetryEvent) event).getLocalPlayerLocation());
        }
        else if (event instanceof NpcStateTelemetryEvent)
        {
            state.noteNpc((NpcStateTelemetryEvent) event);
        }
        else if (event instanceof PlayerActionTelemetryEvent)
        {
            state.noteAction((PlayerActionTelemetryEvent) event);
        }
        else if (event instanceof InteractingChangedTelemetryEvent)
        {
            state.noteInteraction((InteractingChangedTelemetryEvent) event);
        }
        else if (event instanceof DamageTelemetryEvent)
        {
            state.noteDamage((DamageTelemetryEvent) event);
        }

        state.expireTimedOut(event.getTime());
    }

    @Override
    public Optional<FinishReason> evaluateTermination(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        if (event instanceof DamageTelemetryEvent)
        {
            final DamageTelemetryEvent damage = (DamageTelemetryEvent) event;
            if (state.isVerifiedPlayerDeath(damage))
            {
                final FinishReason reason = new FinishReason(
                    FinishReasonType.PLAYER_DEAD,
                    damage.getTime(),
                    0.95D,
                    "Araxxor ended because verified local-player damage evidence reached zero health.",
                    Collections.singletonList("Local player took " + damage.getAmount() + " damage at tick " + damage.getTime().getGameTick() + "."));
                state.cancelOpenOpportunities(damage.getTime(), reason.getExplanation());
                return Optional.of(reason);
            }
        }
        else if (event instanceof NpcStateTelemetryEvent)
        {
            final NpcStateTelemetryEvent npc = (NpcStateTelemetryEvent) event;
            if (state.isBossDefeat(npc))
            {
                final FinishReason reason = new FinishReason(
                    FinishReasonType.BOSS_DEAD,
                    npc.getTime(),
                    0.94D,
                    "Araxxor ended because verified boss despawn or zero-health evidence completed the fight.",
                    Collections.singletonList("Verified Araxxor NPC " + npc.getNpcRef().getId() + " ended at tick " + npc.getTime().getGameTick() + "."));
                state.cancelOpenOpportunities(npc.getTime(), reason.getExplanation());
                return Optional.of(reason);
            }
        }
        else if (event instanceof RegionInstanceTelemetryEvent)
        {
            final RegionInstanceTelemetryEvent region = (RegionInstanceTelemetryEvent) event;
            state.updateRegion(region.getLocalPlayerLocation());

            if (!"LOGGED_IN".equals(region.getGameState()))
            {
                final FinishReasonType type = "HOPPING".equals(region.getGameState()) ? FinishReasonType.HOPPED_WORLD : FinishReasonType.LOGGED_OUT;
                final FinishReason reason = new FinishReason(
                    type,
                    region.getTime(),
                    0.95D,
                    "Araxxor ended because the client left the logged-in state.",
                    Collections.singletonList("Region/game-state evidence changed to " + region.getGameState() + "."));
                state.cancelOpenOpportunities(region.getTime(), reason.getExplanation());
                return Optional.of(reason);
            }

            if (state.leftVerifiedRegion())
            {
                final FinishReason reason = new FinishReason(
                    FinishReasonType.TELEPORTED,
                    region.getTime(),
                    0.93D,
                    "Araxxor ended because verified instance evidence showed the player left the Araxxor arena mid-fight.",
                    Collections.singletonList("Region changed from verified Araxxor context to " + region.getRegionId() + "."));
                state.cancelOpenOpportunities(region.getTime(), reason.getExplanation());
                return Optional.of(reason);
            }
        }

        if (state.shouldIdleTerminate(event.getTime()))
        {
            final FinishReason reason = new FinishReason(
                FinishReasonType.IDLE_TIMEOUT,
                event.getTime(),
                0.86D,
                "Araxxor ended because verified boss evidence disappeared and no re-engagement arrived within five ticks.",
                Collections.singletonList("Araxxor NPC absent for 5 ticks."));
            state.cancelOpenOpportunities(event.getTime(), reason.getExplanation());
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
    }
}
