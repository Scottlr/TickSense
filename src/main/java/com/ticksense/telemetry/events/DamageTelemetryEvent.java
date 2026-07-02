package com.ticksense.telemetry.events;

import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.Map;
import java.util.Objects;

public final class DamageTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "damage";

    private final EntityRef targetRef;
    private final int hitsplatType;
    private final int amount;
    private final int healthRatio;
    private final int healthScale;

    public DamageTelemetryEvent(
        EventTime time,
        Map<String, String> tags,
        EntityRef targetRef,
        int hitsplatType,
        int amount,
        int healthRatio,
        int healthScale)
    {
        super(TYPE, TelemetryCategory.DAMAGE, time, tags);
        this.targetRef = Objects.requireNonNull(targetRef, "targetRef");
        this.hitsplatType = hitsplatType;
        this.amount = amount;
        this.healthRatio = healthRatio;
        this.healthScale = healthScale;
    }

    public EntityRef getTargetRef()
    {
        return targetRef;
    }

    public int getHitsplatType()
    {
        return hitsplatType;
    }

    public int getAmount()
    {
        return amount;
    }

    public int getHealthRatio()
    {
        return healthRatio;
    }

    public int getHealthScale()
    {
        return healthScale;
    }
}
