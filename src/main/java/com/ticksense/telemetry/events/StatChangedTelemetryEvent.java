package com.ticksense.telemetry.events;

import com.ticksense.core.EventTime;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.Map;

public final class StatChangedTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "stat.changed";

    private final String skill;
    private final int xp;
    private final int xpDelta;
    private final int level;
    private final int boostedLevel;

    public StatChangedTelemetryEvent(
        EventTime time,
        Map<String, String> tags,
        String skill,
        int xp,
        int xpDelta,
        int level,
        int boostedLevel)
    {
        super(TYPE, TelemetryCategory.XP_STAT_CHANGE, time, tags);
        this.skill = safeText(skill);
        this.xp = xp;
        this.xpDelta = xpDelta;
        this.level = level;
        this.boostedLevel = boostedLevel;
    }

    public String getSkill()
    {
        return skill;
    }

    public int getXp()
    {
        return xp;
    }

    public int getXpDelta()
    {
        return xpDelta;
    }

    public int getLevel()
    {
        return level;
    }

    public int getBoostedLevel()
    {
        return boostedLevel;
    }
}
