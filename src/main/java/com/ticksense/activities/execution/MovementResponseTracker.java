package com.ticksense.activities.execution;

import com.ticksense.activities.ActivityContext;
import com.ticksense.activities.OpportunityDefinition;
import com.ticksense.activities.OpportunityInstance;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.MovementTelemetryEvent;
import java.util.Map;

public final class MovementResponseTracker extends AbstractExecutionTracker
{
    public static final String ID = "movement-response";
    public static final String OPPORTUNITY_MOVEMENT_RESPONSE = "MOVEMENT_RESPONSE";

    private static final long DEFAULT_TIMEOUT_MILLIS = 2_400L;

    private OpportunityInstance movementResponse;

    public MovementResponseTracker()
    {
        super(ID);
    }

    public void openResponse(ActivitySession session, EventTime time, Map<String, String> context)
    {
        if (isOpen(movementResponse))
        {
            return;
        }
        movementResponse = startOpportunity(
            definition(
                OPPORTUNITY_MOVEMENT_RESPONSE,
                "Movement response",
                session.getActivityType(),
                DEFAULT_TIMEOUT_MILLIS,
                "Move away from or toward the activity cue"),
            time,
            context);
    }

    @Override
    public void onEvent(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        if (!isOpen(movementResponse) || !(event instanceof MovementTelemetryEvent))
        {
            return;
        }
        final MovementTelemetryEvent movement = (MovementTelemetryEvent) event;
        if (movement.getEntityRef().getType() != EntityRef.Type.LOCAL_PLAYER)
        {
            return;
        }
        complete(
            movementResponse,
            movement.getTime(),
            MovementTelemetryEvent.TYPE,
            "Local-player movement responded to the active movement window.");
        movementResponse = null;
    }

    @Override
    public void reset()
    {
        super.reset();
        movementResponse = null;
    }
}
