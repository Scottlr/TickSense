package com.ticksense.activities.execution.recovery;

import com.ticksense.activities.ActivityContext;
import com.ticksense.activities.OpportunityDefinition;
import com.ticksense.activities.OpportunityInstance;
import com.ticksense.activities.execution.AbstractExecutionTracker;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.EntityRef;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.DamageTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import java.util.Set;

public final class FoodRecoveryTracker extends AbstractExecutionTracker
{
    public static final String ID = "food-recovery";
    public static final String OPPORTUNITY_FOOD_RECOVERY = "FOOD_RECOVERY";

    private static final long DEFAULT_TIMEOUT_MILLIS = 3_600L;
    private static final String FOOD_ACTION = "Eat";

    private final Set<Integer> fallbackFoodItemIds;
    private OpportunityInstance recoveryWindow;

    public FoodRecoveryTracker()
    {
        this(RecoveryItemFallbackIds.foodItemIds());
    }

    public FoodRecoveryTracker(Set<Integer> fallbackFoodItemIds)
    {
        super(ID);
        this.fallbackFoodItemIds = Set.copyOf(fallbackFoodItemIds);
    }

    @Override
    public void onEvent(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        if (!supports(context, session))
        {
            return;
        }
        if (event instanceof DamageTelemetryEvent)
        {
            openAfterDamage(session, (DamageTelemetryEvent) event);
            return;
        }
        if (event instanceof InventoryDeltaTelemetryEvent)
        {
            completeOnFoodConsumption(session, (InventoryDeltaTelemetryEvent) event);
        }
    }

    @Override
    public void reset()
    {
        super.reset();
        recoveryWindow = null;
    }

    private void openAfterDamage(ActivitySession session, DamageTelemetryEvent event)
    {
        if (event.getTargetRef().getType() != EntityRef.Type.LOCAL_PLAYER
            || event.getHealthRatio() <= 0
            || isOpen(recoveryWindow))
        {
            return;
        }
        // TODO: Open only when health context indicates food was realistically needed.
        recoveryWindow = startOpportunity(
            definition(
                OPPORTUNITY_FOOD_RECOVERY,
                "Food recovery",
                session.getActivityType(),
                DEFAULT_TIMEOUT_MILLIS,
                "Eat food or otherwise recover after damage"),
            event.getTime(),
            context(
                "damageAmount", String.valueOf(event.getAmount()),
                "healthRatio", String.valueOf(event.getHealthRatio())));
    }

    private void completeOnFoodConsumption(ActivitySession session, InventoryDeltaTelemetryEvent event)
    {
        final Integer consumedItemId = RecoveryItemConsumption.consumedItemId(event, FOOD_ACTION, fallbackFoodItemIds, false);
        if (consumedItemId == null)
        {
            return;
        }
        if (!isOpen(recoveryWindow))
        {
            // TODO: Decide whether proactive eating should open a separate opportunity type.
            recoveryWindow = startOpportunity(
                definition(
                    OPPORTUNITY_FOOD_RECOVERY,
                    "Food recovery",
                    session.getActivityType(),
                    DEFAULT_TIMEOUT_MILLIS,
                    "Eat food or otherwise recover after damage"),
                event.getTime(),
                context("recoveryMode", "proactive-or-unattributed"));
        }
        complete(
            recoveryWindow,
            event.getTime(),
            InventoryDeltaTelemetryEvent.TYPE,
            "Food item " + consumedItemId + " was consumed during the recovery window.");
        recoveryWindow = null;
    }
}
