package com.ticksense.activities.construction;

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
import com.ticksense.core.EntityRef;
import com.ticksense.core.FinishReason;
import com.ticksense.core.FinishReasonType;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.AnimationTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.MenuInteractionTelemetryEvent;
import com.ticksense.telemetry.events.ObjectStateTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import com.ticksense.telemetry.events.StatChangedTelemetryEvent;
import com.ticksense.telemetry.events.WidgetTelemetryEvent;
import java.util.Collections;
import java.util.Optional;

public final class ConstructionStrategy implements ActivityStrategy
{
    private static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.CONSTRUCTION, "Construction", 20, 0.75D, false);

    private final ConstructionState state = new ConstructionState();

    @Override
    public ActivityDefinition getDefinition()
    {
        return DEFINITION;
    }

    @Override
    public ActivityCandidate evaluateActivation(ActivityContext context, TelemetryEvent event)
    {
        updatePassiveState(event);

        if (!(event instanceof PlayerActionTelemetryEvent) || !ConstructionIds.verificationDecision().allowsStrategyEnablement())
        {
            return null;
        }

        final PlayerActionTelemetryEvent action = (PlayerActionTelemetryEvent) event;
        if (!ConstructionState.isBuildAction(action.getOption(), action.getTarget()))
        {
            return null;
        }

        state.noteBuildClick(action.getTime(), action.getLocation());
        final double confidence = state.activationConfidenceForBuildClick(action.getLocation());
        return new ActivityCandidate(
            ActivityId.of("construction-" + context.getSessionId() + "-" + action.getTime().getGameTick()),
            ActivityType.CONSTRUCTION,
            confidence,
            state.activationEvidence(action.getLocation()),
            state.activationStartTime(action.getTime()),
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
        state.flushActivationDerivedOpportunities();

        if (event instanceof RegionInstanceTelemetryEvent)
        {
            final RegionInstanceTelemetryEvent region = (RegionInstanceTelemetryEvent) event;
            state.updateRegion(region.getLocalPlayerLocation());
            return;
        }
        if (event instanceof ObjectStateTelemetryEvent)
        {
            final ObjectStateTelemetryEvent object = (ObjectStateTelemetryEvent) event;
            state.markObjectState(object.getObjectId(), object.getLocation(), object.getObjectName(), object.getStateChange(), object.getTime());
            return;
        }
        if (event instanceof MenuInteractionTelemetryEvent)
        {
            final MenuInteractionTelemetryEvent menu = (MenuInteractionTelemetryEvent) event;
            if ("MenuOpened".equals(menu.getInteractionType()))
            {
                state.noteMenuOpened(menu.getTime(), menu.getSelectedOption(), menu.getTarget());
            }
            return;
        }
        if (event instanceof PlayerActionTelemetryEvent)
        {
            final PlayerActionTelemetryEvent action = (PlayerActionTelemetryEvent) event;
            state.noteMenuClick(action.getTime(), action.getOption(), action.getTarget(), action.getLocation());
            if (ConstructionState.isBuildAction(action.getOption(), action.getTarget()))
            {
                state.noteBuildClick(action.getTime(), action.getLocation());
            }
            else if (ConstructionState.isRemoveAction(action.getOption(), action.getTarget()))
            {
                state.noteRemoveClick(action.getTime(), action.getLocation());
            }
            return;
        }
        if (event instanceof WidgetTelemetryEvent)
        {
            final WidgetTelemetryEvent widget = (WidgetTelemetryEvent) event;
            state.noteConstructionWidget(widget.getTime(), widget.getGroupId(), widget.getChildId());
            state.noteBankWidget(widget.getTime(), widget.getGroupId());
            return;
        }
        if (event instanceof AnimationTelemetryEvent)
        {
            final AnimationTelemetryEvent animation = (AnimationTelemetryEvent) event;
            if (animation.getActorRef().getType() == EntityRef.Type.LOCAL_PLAYER)
            {
                state.noteConstructionAnimation(animation.getTime(), animation.getAnimationId());
            }
            return;
        }
        if (event instanceof InventoryDeltaTelemetryEvent)
        {
            final InventoryDeltaTelemetryEvent inventory = (InventoryDeltaTelemetryEvent) event;
            for (InventoryDeltaTelemetryEvent.ItemDelta delta : inventory.getDeltas())
            {
                state.noteInventoryDelta(inventory.getTime(), delta.getBeforeItemId(), delta.getAfterItemId(), delta.getAfterQuantity());
            }
            return;
        }
        if (event instanceof StatChangedTelemetryEvent)
        {
            final StatChangedTelemetryEvent stat = (StatChangedTelemetryEvent) event;
            if ("CONSTRUCTION".equals(stat.getSkill()))
            {
                state.noteConstructionXp(stat.getTime(), stat.getXpDelta());
            }
        }
    }

    @Override
    public Optional<FinishReason> evaluateTermination(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        if (event instanceof WidgetTelemetryEvent)
        {
            final WidgetTelemetryEvent widget = (WidgetTelemetryEvent) event;
            if (state.isBankWidget(widget.getGroupId()))
            {
                final FinishReason reason = new FinishReason(
                    FinishReasonType.BANK_OPENED,
                    widget.getTime(),
                    0.95D,
                    "Construction ended because verified banking evidence completed the oak-larder cycle.",
                    Collections.singletonList("Bank widget group " + widget.getGroupId() + " loaded after the verified build/remove flow."));
                state.cancelOpenOpportunities(widget.getTime(), reason.getExplanation());
                return Optional.of(reason);
            }
        }
        if (event instanceof RegionInstanceTelemetryEvent)
        {
            final RegionInstanceTelemetryEvent region = (RegionInstanceTelemetryEvent) event;
            if (!"LOGGED_IN".equals(region.getGameState()))
            {
                final FinishReasonType type = "HOPPING".equals(region.getGameState()) ? FinishReasonType.HOPPED_WORLD : FinishReasonType.LOGGED_OUT;
                final FinishReason reason = new FinishReason(
                    type,
                    region.getTime(),
                    0.95D,
                    "Construction ended because the client left the logged-in state.",
                    Collections.singletonList("Construction region evidence changed to " + region.getGameState() + "."));
                state.cancelOpenOpportunities(region.getTime(), reason.getExplanation());
                return Optional.of(reason);
            }
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
            final RegionInstanceTelemetryEvent region = (RegionInstanceTelemetryEvent) event;
            state.updateRegion(region.getLocalPlayerLocation());
            return;
        }
        if (event instanceof ObjectStateTelemetryEvent)
        {
            final ObjectStateTelemetryEvent object = (ObjectStateTelemetryEvent) event;
            state.markObjectState(object.getObjectId(), object.getLocation(), object.getObjectName(), object.getStateChange(), object.getTime());
            return;
        }
        if (event instanceof MenuInteractionTelemetryEvent)
        {
            final MenuInteractionTelemetryEvent menu = (MenuInteractionTelemetryEvent) event;
            if ("MenuOpened".equals(menu.getInteractionType()))
            {
                state.noteMenuOpened(menu.getTime(), menu.getSelectedOption(), menu.getTarget());
            }
        }
    }
}
