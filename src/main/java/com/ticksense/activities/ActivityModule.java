package com.ticksense.activities;

import com.ticksense.analytics.ReportBuilder;

public interface ActivityModule
{
    ActivityDefinition definition();

    boolean isEnabled();

    ActivityStrategy createStrategy();

    ReportBuilder reportBuilder();
}
