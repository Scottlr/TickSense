package com.ticksense.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EntityRefTest
{
    @Test
    public void npcFactoryCreatesComparableValueReference()
    {
        final EntityRef first = EntityRef.npc(42, 12_345, "Araxxor spider");
        final EntityRef second = EntityRef.npc(42, 12_345, "Araxxor spider");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void widgetFactoryPreservesWidgetCoordinates()
    {
        final EntityRef widget = EntityRef.widget(12, 34);

        assertEquals(EntityRef.Type.WIDGET, widget.getType());
        assertEquals(12, widget.getGroupId());
        assertEquals(34, widget.getChildId());
    }
}
