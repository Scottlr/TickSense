package com.ticksense.activities.execution.prayer;

import com.ticksense.activities.ActivityContext;
import com.ticksense.activities.execution.AbstractExecutionTracker;
import com.ticksense.core.ActivitySession;
import com.ticksense.telemetry.TelemetryEvent;

public final class PrayerSwitchTracker extends AbstractExecutionTracker
{
    public static final String ID = "prayer-switch";
    public static final String OPPORTUNITY_PRAYER_SWITCH = "PRAYER_SWITCH";

    public PrayerSwitchTracker()
    {
        super(ID);
    }

    @Override
    public void onEvent(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        // TODO: Wire this once normalized telemetry exposes prayer widget/varbit state changes.
        // TODO: Model the cue separately from the actual prayer toggle so latency is measurable.
    }
}
