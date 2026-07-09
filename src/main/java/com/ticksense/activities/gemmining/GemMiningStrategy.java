package com.ticksense.activities.gemmining;

import com.ticksense.activities.AbstractActivityStrategy;
import com.ticksense.activities.ActivityCandidate;
import com.ticksense.activities.ActivityContext;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EntityRef;
import com.ticksense.core.FinishReason;
import com.ticksense.core.FinishReasonType;
import com.ticksense.telemetry.StateChanges;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.AnimationTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.MovementTelemetryEvent;
import com.ticksense.telemetry.events.ObjectStateTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import com.ticksense.telemetry.events.StatChangedTelemetryEvent;
import java.util.Collections;
import java.util.Optional;

public final class GemMiningStrategy extends AbstractActivityStrategy<GemMiningState>
{
    public GemMiningStrategy()
    {
        super(GemMiningModule.DEFINITION, "gem-mining", new GemMiningState());
    }

    @Override
    public ActivityCandidate evaluateActivation(ActivityContext context, TelemetryEvent event)
    {
        updatePassiveState(event);

        if (!(event instanceof PlayerActionTelemetryEvent))
        {
            return passiveCandidate(event);
        }

        final PlayerActionTelemetryEvent action = (PlayerActionTelemetryEvent) event;
        if (!GemMiningState.isMineAction(action.getOption(), action.getTarget()))
        {
            return passiveCandidate(event);
        }

        final int objectId = state.matchesAvailableRock(action.getLocation()) && state.isVerifiedRegion(action.getLocation().getRegionId())
            ? state.availableRockObjectIdOrUnknown()
            : -1;
        state.noteMineClick(objectId, action.getLocation(), action.getTime());
        final double confidence = state.activationConfidenceForMineClick(action.getLocation());
        return new ActivityCandidate(
            activityId(context, event),
            ActivityType.GEM_MINING,
            confidence,
            state.activationEvidenceForMineClick(),
            state.activationStartTime(event.getTime()),
            false,
            "");
    }

    @Override
    protected void onActivityEvent(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        state.noteReusableExecutionEvent(context, session, event);
        state.expireTimedOut(event.getTime());
        if (state.availableRock() != null)
        {
            state.emitCycleOpportunitiesForActiveClick();
        }

        if (event instanceof RegionInstanceTelemetryEvent)
        {
            final RegionInstanceTelemetryEvent region = (RegionInstanceTelemetryEvent) event;
            state.updateRegion(region.getLocalPlayerLocation());
            return;
        }
        if (event instanceof ObjectStateTelemetryEvent)
        {
            final ObjectStateTelemetryEvent object = (ObjectStateTelemetryEvent) event;
            if (StateChanges.AVAILABLE.equals(object.getStateChange()))
            {
                state.markRockAvailable(object.getObjectId(), object.getLocation(), object.getTime());
            }
            else if (StateChanges.DEPLETED.equals(object.getStateChange()))
            {
                state.markRockDepleted(object.getObjectId(), object.getLocation());
            }
            return;
        }
        if (event instanceof PlayerActionTelemetryEvent)
        {
            final PlayerActionTelemetryEvent action = (PlayerActionTelemetryEvent) event;
            if (GemMiningState.isMineAction(action.getOption(), action.getTarget()))
            {
                final int objectId = state.matchesAvailableRock(action.getLocation()) ? state.availableRockObjectIdOrUnknown() : -1;
                state.noteMineClick(objectId, action.getLocation(), action.getTime());
                state.emitCycleOpportunitiesForActiveClick();
            }
            return;
        }
        if (event instanceof MovementTelemetryEvent)
        {
            final MovementTelemetryEvent movement = (MovementTelemetryEvent) event;
            if (GemMiningState.isLocalPlayer(movement.getEntityRef()))
            {
                final GemMiningState.MovementTelemetry movementView =
                    new GemMiningState.MovementTelemetry(movement.getTime(), movement.getFromLocation(), movement.getToLocation());
                state.updateRegion(movement.getToLocation());
                if (GemMiningState.isMovementTowardAvailableRock(movementView, state.availableRock()))
                {
                    state.noteMovementTowardRock(new GemMiningState.MovementTowardRock(
                        movement.getTime(),
                        movement.getFromLocation(),
                        movement.getToLocation()));
                }
            }
            return;
        }
        if (isMiningConfirmation(event))
        {
            state.noteProgressConfirmation();
        }
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
                "Gem mining ended because the client left the logged-in state.",
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
                "Gem mining ended because the player left the verified gem-mining region.",
                Collections.singletonList("Region changed from verified gem mine to " + region.getRegionId() + "."));
            state.cancelOpenOpportunities(region.getTime(), reason.getExplanation());
            return Optional.of(reason);
        }

        return Optional.empty();
    }

    private ActivityCandidate passiveCandidate(TelemetryEvent event)
    {
        if (state.hasVerifiedRegion() && state.availableRock() != null)
        {
            return new ActivityCandidate(
                ActivityId.of("gem-mining-candidate-" + event.getTime().getGameTick()),
                ActivityType.GEM_MINING,
                0.60D,
                Collections.singletonList("Verified gem rock became available but no mine click is confirmed yet."),
                state.activationStartTime(event.getTime()),
                false,
                "");
        }
        return null;
    }

    private void updatePassiveState(TelemetryEvent event)
    {
        if (event instanceof RegionInstanceTelemetryEvent)
        {
            final RegionInstanceTelemetryEvent region = (RegionInstanceTelemetryEvent) event;
            state.updateRegion(region.getLocalPlayerLocation());
            return;
        }
        if (event instanceof ObjectStateTelemetryEvent)
        {
            final ObjectStateTelemetryEvent object = (ObjectStateTelemetryEvent) event;
            if (StateChanges.AVAILABLE.equals(object.getStateChange()))
            {
                state.markRockAvailable(object.getObjectId(), object.getLocation(), object.getTime());
            }
            else if (StateChanges.DEPLETED.equals(object.getStateChange()))
            {
                state.markRockDepleted(object.getObjectId(), object.getLocation());
            }
            return;
        }
        if (event instanceof MovementTelemetryEvent)
        {
            final MovementTelemetryEvent movement = (MovementTelemetryEvent) event;
            if (GemMiningState.isLocalPlayer(movement.getEntityRef()))
            {
                state.updateRegion(movement.getToLocation());
                if (GemMiningState.isMovementTowardAvailableRock(
                    new GemMiningState.MovementTelemetry(movement.getTime(), movement.getFromLocation(), movement.getToLocation()),
                    state.availableRock()))
                {
                    state.noteMovementTowardRock(new GemMiningState.MovementTowardRock(
                        movement.getTime(),
                        movement.getFromLocation(),
                        movement.getToLocation()));
                }
            }
        }
    }

    private boolean isMiningConfirmation(TelemetryEvent event)
    {
        if (event instanceof AnimationTelemetryEvent)
        {
            final AnimationTelemetryEvent animation = (AnimationTelemetryEvent) event;
            return animation.getActorRef().getType() == EntityRef.Type.LOCAL_PLAYER
                && state.isVerifiedMiningAnimation(animation.getAnimationId());
        }
        if (event instanceof StatChangedTelemetryEvent)
        {
            final StatChangedTelemetryEvent stat = (StatChangedTelemetryEvent) event;
            return "MINING".equals(stat.getSkill()) && stat.getXpDelta() > 0;
        }
        if (event instanceof InventoryDeltaTelemetryEvent)
        {
            final InventoryDeltaTelemetryEvent inventory = (InventoryDeltaTelemetryEvent) event;
            return inventory.getDeltas().stream().anyMatch(delta -> state.isVerifiedGemItem(delta.getAfterItemId()));
        }
        return false;
    }

}
