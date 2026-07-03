package com.ticksense.activities.execution.recovery;

import com.ticksense.activities.ActivityContext;
import com.ticksense.activities.OpportunityDefinition;
import com.ticksense.activities.OpportunityInstance;
import com.ticksense.activities.execution.AbstractExecutionTracker;
import com.ticksense.core.ActivitySession;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import java.util.Set;

public final class PotionRecoveryTracker extends AbstractExecutionTracker
{
    public static final String ID = "potion-recovery";
    public static final String OPPORTUNITY_POTION_RECOVERY = "POTION_RECOVERY";

    private final Set<Integer> potionItemIds;

    public PotionRecoveryTracker()
    {
        this(RecoveryItemIds.potionItemIds());
    }

    public PotionRecoveryTracker(Set<Integer> potionItemIds)
    {
        super(ID);
        this.potionItemIds = Set.copyOf(potionItemIds);
    }

    @Override
    public void onEvent(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        if (!supports(context, session) || !(event instanceof InventoryDeltaTelemetryEvent))
        {
            return;
        }
        completeOnPotionConsumption(session, (InventoryDeltaTelemetryEvent) event);
    }

    private void completeOnPotionConsumption(ActivitySession session, InventoryDeltaTelemetryEvent event)
    {
        final Integer consumedItemId = consumedPotionItemId(event);
        if (consumedItemId == null)
        {
            return;
        }

        // TODO: Split restoration, brew, offensive buff, and defensive buff semantics once item taxonomy is verified.
        // TODO: Expand default IDs to include every dose variant from source-owned RuneLite ItemID fixtures.
        final OpportunityDefinition definition = definition(
            OPPORTUNITY_POTION_RECOVERY,
            "Potion recovery",
            session.getActivityType(),
            0L,
            "Drink a potion to restore resources or refresh a combat buff");
        final OpportunityInstance instance = startOpportunity(
            definition,
            event.getTime(),
            context(
                "potionItemId", String.valueOf(consumedItemId),
                "containerId", String.valueOf(event.getContainerId())));
        complete(
            instance,
            event.getTime(),
            InventoryDeltaTelemetryEvent.TYPE,
            "Potion item " + consumedItemId + " was consumed or dosed down.");
    }

    private Integer consumedPotionItemId(InventoryDeltaTelemetryEvent event)
    {
        for (InventoryDeltaTelemetryEvent.ItemDelta delta : event.getDeltas())
        {
            if (!potionItemIds.contains(delta.getBeforeItemId()))
            {
                continue;
            }
            if (delta.getAfterQuantity() < delta.getBeforeQuantity() || delta.getAfterItemId() != delta.getBeforeItemId())
            {
                return delta.getBeforeItemId();
            }
        }
        return null;
    }
}
