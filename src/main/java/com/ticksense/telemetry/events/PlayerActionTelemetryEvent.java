package com.ticksense.telemetry.events;

import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.Map;
import java.util.Objects;

public final class PlayerActionTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "player.action";

    private final String option;
    private final String target;
    private final EntityRef targetRef;
    private final String actionKind;
    private final WorldLocation location;
    private final int menuActionId;

    public PlayerActionTelemetryEvent(
        EventTime time,
        Map<String, String> tags,
        String option,
        String target,
        EntityRef targetRef,
        String actionKind,
        WorldLocation location,
        int menuActionId)
    {
        super(TYPE, TelemetryCategory.PLAYER_ACTION, time, tags);
        this.option = safeText(option);
        this.target = safeText(target);
        this.targetRef = Objects.requireNonNull(targetRef, "targetRef");
        this.actionKind = safeText(actionKind);
        this.location = Objects.requireNonNull(location, "location");
        this.menuActionId = menuActionId;
    }

    public String getOption()
    {
        return option;
    }

    public String getTarget()
    {
        return target;
    }

    public EntityRef getTargetRef()
    {
        return targetRef;
    }

    public String getActionKind()
    {
        return actionKind;
    }

    public WorldLocation getLocation()
    {
        return location;
    }

    public int getMenuActionId()
    {
        return menuActionId;
    }
}
