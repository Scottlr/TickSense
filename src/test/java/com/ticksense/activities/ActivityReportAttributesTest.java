package com.ticksense.activities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ActivityReportAttributesTest
{
    @Test
    public void readsTypedValuesFromStringBoundary()
    {
        final ActivityReportAttributes attributes = ActivityReportAttributes.builder()
            .putText("verificationStatus", "VERIFIED")
            .putInt("idleTicks", 4)
            .build();

        assertEquals("VERIFIED", attributes.getText("verificationStatus"));
        assertEquals(4, attributes.getInt("idleTicks"));
        assertEquals("", attributes.getText("missing"));
        assertEquals(0, attributes.getInt("missingInt"));
    }
}
