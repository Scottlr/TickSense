package com.ticksense.activities;

import com.ticksense.analytics.ReportBuilder;
import java.util.Objects;

public class SimpleActivityModule implements ActivityModule
{
    private final ActivityDescriptor descriptor;

    public SimpleActivityModule(ActivityDescriptor descriptor)
    {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    @Override
    public ActivityDefinition definition()
    {
        return descriptor.definition();
    }

    @Override
    public boolean isEnabled()
    {
        return descriptor.isEnabled();
    }

    @Override
    public ActivityStrategy createStrategy()
    {
        return descriptor.createStrategy();
    }

    @Override
    public ReportBuilder reportBuilder()
    {
        return descriptor.reportBuilder();
    }

    @Override
    public ActivityDescriptor descriptor()
    {
        return descriptor;
    }
}
