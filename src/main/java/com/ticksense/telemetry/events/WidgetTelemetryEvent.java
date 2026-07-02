package com.ticksense.telemetry.events;

import com.ticksense.core.EventTime;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.List;
import java.util.Map;

public final class WidgetTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "widget";

    private final int groupId;
    private final int childId;
    private final int itemId;
    private final String text;
    private final String eventKind;
    private final boolean visible;
    private final List<String> actions;

    public WidgetTelemetryEvent(
        EventTime time,
        Map<String, String> tags,
        int groupId,
        int childId,
        int itemId,
        String text,
        String eventKind,
        boolean visible,
        List<String> actions)
    {
        super(TYPE, TelemetryCategory.WIDGET, time, tags);
        this.groupId = groupId;
        this.childId = childId;
        this.itemId = itemId;
        this.text = safeText(text);
        this.eventKind = safeText(eventKind);
        this.visible = visible;
        this.actions = immutableList(actions);
    }

    public int getGroupId()
    {
        return groupId;
    }

    public int getChildId()
    {
        return childId;
    }

    public int getItemId()
    {
        return itemId;
    }

    public String getText()
    {
        return text;
    }

    public String getEventKind()
    {
        return eventKind;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public List<String> getActions()
    {
        return actions;
    }
}
