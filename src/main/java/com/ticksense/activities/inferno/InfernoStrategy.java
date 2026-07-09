package com.ticksense.activities.inferno;

import com.ticksense.activities.AbstractActivityStrategy;
import com.ticksense.activities.ActivityCandidate;
import com.ticksense.activities.ActivityContext;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.ActivityType;
import com.ticksense.core.FinishReason;
import com.ticksense.core.FinishReasonType;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.DamageTelemetryEvent;
import com.ticksense.telemetry.events.InteractingChangedTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.NpcStateTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import java.util.Collections;
import java.util.Optional;

public final class InfernoStrategy extends AbstractActivityStrategy<InfernoState>
{
    public InfernoStrategy()
    {
        this(
            InfernoIds.verificationDecision(),
            InfernoIds.nibblerNpcIds(),
            InfernoIds.waveNpcIds(),
            InfernoIds.supplyItemIds(),
            InfernoIds.verifiedRegionIds());
    }

    public InfernoStrategy(
        InfernoVerificationDecision verificationDecision,
        int[] nibblerNpcIds,
        int[] waveNpcIds,
        int[] supplyItemIds,
        int[] verifiedRegionIds)
    {
        super(
            InfernoModule.DEFINITION,
            "inferno",
            new InfernoState(verificationDecision, nibblerNpcIds, waveNpcIds, supplyItemIds, verifiedRegionIds));
    }

    @Override
    public ActivityCandidate evaluateActivation(ActivityContext context, TelemetryEvent event)
    {
        updatePassiveState(event);

        if (!state.allowsStrategyEnablement() || !(event instanceof NpcStateTelemetryEvent))
        {
            return null;
        }

        final NpcStateTelemetryEvent npc = (NpcStateTelemetryEvent) event;
        if (!state.canActivateFromWaveNpc(npc))
        {
            return null;
        }

        state.noteActivationWave(npc);
        return new ActivityCandidate(
            activityId(context, npc),
            ActivityType.INFERNO,
            0.93D,
            state.activationEvidence(npc),
            state.activationStartTime(npc),
            false,
            "");
    }

    @Override
    protected void onActivityEvent(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        state.noteReusableExecutionEvent(context, session, event);
        state.flushActivationDerivedSpans();

        if (event instanceof RegionInstanceTelemetryEvent)
        {
            state.updateRegion(((RegionInstanceTelemetryEvent) event).getLocalPlayerLocation());
            state.expireTimedOut(event.getTime());
            return;
        }
        if (event instanceof NpcStateTelemetryEvent)
        {
            final NpcStateTelemetryEvent npc = (NpcStateTelemetryEvent) event;
            state.noteWaveNpc(npc);
            state.noteNibblerNpc(npc);
            state.expireTimedOut(event.getTime());
            return;
        }
        if (event instanceof InteractingChangedTelemetryEvent)
        {
            state.noteNibblerInteraction((InteractingChangedTelemetryEvent) event);
            state.expireTimedOut(event.getTime());
            return;
        }
        if (event instanceof InventoryDeltaTelemetryEvent)
        {
            state.noteSupplyUsage((InventoryDeltaTelemetryEvent) event);
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
        if (event instanceof DamageTelemetryEvent)
        {
            final DamageTelemetryEvent damage = (DamageTelemetryEvent) event;
            if (state.isVerifiedPlayerDeath(damage))
            {
                final FinishReason reason = new FinishReason(
                    FinishReasonType.PLAYER_DEAD,
                    damage.getTime(),
                    0.95D,
                    "Inferno ended because verified death evidence placed the local player at zero health.",
                    Collections.singletonList("Local player took " + damage.getAmount() + " damage at tick " + damage.getTime().getGameTick() + "."));
                state.completeWaveSpan(damage.getTime(), reason.getExplanation());
                return Optional.of(reason);
            }
            return Optional.empty();
        }

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
                "Inferno ended because the client left the logged-in state.",
                Collections.singletonList("Region/game-state evidence changed to " + region.getGameState() + "."));
            state.completeWaveSpan(region.getTime(), reason.getExplanation());
            return Optional.of(reason);
        }

        if (!state.isVerifiedRegion(region.getRegionId()))
        {
            final FinishReason reason = new FinishReason(
                FinishReasonType.LEFT_REGION,
                region.getTime(),
                0.92D,
                "Inferno ended because the player left the verified Inferno region.",
                Collections.singletonList("Region changed from verified Inferno context to " + region.getRegionId() + "."));
            state.completeWaveSpan(region.getTime(), reason.getExplanation());
            return Optional.of(reason);
        }

        return Optional.empty();
    }

    private void updatePassiveState(TelemetryEvent event)
    {
        if (event instanceof RegionInstanceTelemetryEvent)
        {
            state.updateRegion(((RegionInstanceTelemetryEvent) event).getLocalPlayerLocation());
        }
    }
}
