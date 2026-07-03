package com.ticksense.activities.execution;

import com.ticksense.activities.ActivityContext;
import com.ticksense.activities.OpportunityDefinition;
import com.ticksense.activities.OpportunityInstance;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.EntityRef;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.InteractingChangedTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;

public final class TargetReengagementTracker extends AbstractExecutionTracker
{
    public static final String ID = "target-reengagement";
    public static final String OPPORTUNITY_TARGET_REENGAGEMENT = "TARGET_REENGAGEMENT";

    private static final long DEFAULT_TIMEOUT_MILLIS = 2_400L;

    private OpportunityInstance reengagement;

    public TargetReengagementTracker()
    {
        super(ID);
    }

    @Override
    public void onEvent(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        if (event instanceof InteractingChangedTelemetryEvent)
        {
            onInteraction(session, (InteractingChangedTelemetryEvent) event);
            return;
        }
        if (event instanceof PlayerActionTelemetryEvent)
        {
            onAction((PlayerActionTelemetryEvent) event);
        }
    }

    @Override
    public void reset()
    {
        super.reset();
        reengagement = null;
    }

    private void onInteraction(ActivitySession session, InteractingChangedTelemetryEvent event)
    {
        if (event.getActorRef().getType() != EntityRef.Type.LOCAL_PLAYER)
        {
            return;
        }
        if (event.getInteractingRef().getType() == EntityRef.Type.UNKNOWN && !isOpen(reengagement))
        {
            reengagement = startOpportunity(
                definition(
                    OPPORTUNITY_TARGET_REENGAGEMENT,
                    "Target re-engagement",
                    session.getActivityType(),
                    DEFAULT_TIMEOUT_MILLIS,
                    "Re-click or otherwise restore the intended target"),
                event.getTime(),
                context("stateChange", event.getStateChange()));
            return;
        }
        if (event.getInteractingRef().getType() == EntityRef.Type.NPC)
        {
            complete(
                reengagement,
                event.getTime(),
                InteractingChangedTelemetryEvent.TYPE,
                "Local player restored an NPC target.");
            reengagement = null;
        }
    }

    private void onAction(PlayerActionTelemetryEvent event)
    {
        if (!isOpen(reengagement) || event.getTargetRef().getType() != EntityRef.Type.NPC)
        {
            return;
        }
        complete(
            reengagement,
            event.getTime(),
            PlayerActionTelemetryEvent.TYPE,
            "Local player clicked an NPC target after target loss.");
        reengagement = null;
    }
}
