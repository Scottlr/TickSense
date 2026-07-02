package com.ticksense.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ActivityIdTest
{
    @Test
    public void parseTrimsAndComparesByValue()
    {
        assertEquals(ActivityId.of("session-1"), ActivityId.parse(" session-1 "));
        assertEquals("session-1", ActivityId.parse(" session-1 ").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankValue()
    {
        ActivityId.of("   ");
    }
}
