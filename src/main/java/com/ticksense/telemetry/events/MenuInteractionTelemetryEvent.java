package com.ticksense.telemetry.events;

import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MenuInteractionTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "menu.interaction";

    private final String interactionType;
    private final List<String> entries;
    private final String selectedOption;
    private final String target;
    private final int identifier;
    private final int param0;
    private final int param1;
    private final EntityRef widgetRef;

    public MenuInteractionTelemetryEvent(
        EventTime time,
        Map<String, String> tags,
        String interactionType,
        List<String> entries,
        String selectedOption,
        String target,
        int identifier,
        int param0,
        int param1,
        EntityRef widgetRef)
    {
        super(TYPE, TelemetryCategory.MENU_INTERACTION, time, tags);
        this.interactionType = safeText(interactionType);
        this.entries = immutableList(entries);
        this.selectedOption = safeText(selectedOption);
        this.target = safeText(target);
        this.identifier = identifier;
        this.param0 = param0;
        this.param1 = param1;
        this.widgetRef = Objects.requireNonNull(widgetRef, "widgetRef");
    }

    public String getInteractionType()
    {
        return interactionType;
    }

    public List<String> getEntries()
    {
        return entries;
    }

    public String getSelectedOption()
    {
        return selectedOption;
    }

    public String getTarget()
    {
        return target;
    }

    public int getIdentifier()
    {
        return identifier;
    }

    public int getParam0()
    {
        return param0;
    }

    public int getParam1()
    {
        return param1;
    }

    public EntityRef getWidgetRef()
    {
        return widgetRef;
    }
}
