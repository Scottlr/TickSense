package com.ticksense.telemetry.events;

import com.ticksense.core.EventTime;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class InventoryDeltaTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "inventory.delta";

    private final int containerId;
    private final List<ItemDelta> deltas;

    public InventoryDeltaTelemetryEvent(EventTime time, Map<String, String> tags, int containerId, List<ItemDelta> deltas)
    {
        super(TYPE, TelemetryCategory.INVENTORY_EQUIPMENT, time, tags);
        this.containerId = containerId;
        this.deltas = immutableList(deltas);
    }

    public int getContainerId()
    {
        return containerId;
    }

    public List<ItemDelta> getDeltas()
    {
        return deltas;
    }

    public static final class ItemDelta
    {
        private final int slot;
        private final int beforeItemId;
        private final int beforeQuantity;
        private final int afterItemId;
        private final int afterQuantity;
        private final List<String> beforeInventoryActions;

        public ItemDelta(int slot, int beforeItemId, int beforeQuantity, int afterItemId, int afterQuantity)
        {
            this(slot, beforeItemId, beforeQuantity, afterItemId, afterQuantity, Collections.emptyList());
        }

        public ItemDelta(
            int slot,
            int beforeItemId,
            int beforeQuantity,
            int afterItemId,
            int afterQuantity,
            List<String> beforeInventoryActions)
        {
            this.slot = slot;
            this.beforeItemId = beforeItemId;
            this.beforeQuantity = beforeQuantity;
            this.afterItemId = afterItemId;
            this.afterQuantity = afterQuantity;
            this.beforeInventoryActions = immutableActions(beforeInventoryActions);
        }

        public int getSlot()
        {
            return slot;
        }

        public int getBeforeItemId()
        {
            return beforeItemId;
        }

        public int getBeforeQuantity()
        {
            return beforeQuantity;
        }

        public int getAfterItemId()
        {
            return afterItemId;
        }

        public int getAfterQuantity()
        {
            return afterQuantity;
        }

        public List<String> getBeforeInventoryActions()
        {
            return beforeInventoryActions;
        }

        public boolean hasBeforeInventoryAction(String action)
        {
            final String normalizedAction = normalizeAction(action);
            if (normalizedAction.isEmpty())
            {
                return false;
            }
            for (String beforeAction : beforeInventoryActions)
            {
                if (normalizedAction.equalsIgnoreCase(normalizeAction(beforeAction)))
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object other)
        {
            if (this == other)
            {
                return true;
            }
            if (!(other instanceof ItemDelta))
            {
                return false;
            }
            final ItemDelta that = (ItemDelta) other;
            return slot == that.slot
                && beforeItemId == that.beforeItemId
                && beforeQuantity == that.beforeQuantity
                && afterItemId == that.afterItemId
                && afterQuantity == that.afterQuantity
                && beforeInventoryActions.equals(that.beforeInventoryActions);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(slot, beforeItemId, beforeQuantity, afterItemId, afterQuantity, beforeInventoryActions);
        }

        private static List<String> immutableActions(List<String> source)
        {
            if (source == null || source.isEmpty())
            {
                return Collections.emptyList();
            }

            final List<String> actions = new ArrayList<>(source.size());
            for (String action : source)
            {
                final String normalizedAction = normalizeAction(action);
                if (!normalizedAction.isEmpty())
                {
                    actions.add(normalizedAction);
                }
            }
            return Collections.unmodifiableList(actions);
        }

        private static String normalizeAction(String action)
        {
            return action == null ? "" : action.trim();
        }
    }
}
