package com.ticksense.activities;

import com.ticksense.analytics.ReportBuilder;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class ActivityDescriptor
{
    private final ActivityDefinition definition;
    private final BooleanSupplier enabled;
    private final Supplier<? extends ActivityStrategy> strategyFactory;
    private final ReportBuilder reportBuilder;
    private final ActivityReportMode reportMode;

    public ActivityDescriptor(
        ActivityDefinition definition,
        BooleanSupplier enabled,
        Supplier<? extends ActivityStrategy> strategyFactory,
        ReportBuilder reportBuilder,
        ActivityReportMode reportMode)
    {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.enabled = Objects.requireNonNull(enabled, "enabled");
        this.strategyFactory = Objects.requireNonNull(strategyFactory, "strategyFactory");
        this.reportBuilder = Objects.requireNonNull(reportBuilder, "reportBuilder");
        this.reportMode = Objects.requireNonNull(reportMode, "reportMode");
    }

    public static ActivityDescriptor reportable(
        ActivityDefinition definition,
        BooleanSupplier enabled,
        Supplier<? extends ActivityStrategy> strategyFactory,
        ReportBuilder reportBuilder)
    {
        return new ActivityDescriptor(definition, enabled, strategyFactory, reportBuilder, ActivityReportMode.ENABLED);
    }

    public static ActivityDescriptor reportsDisabled(
        ActivityDefinition definition,
        BooleanSupplier enabled,
        Supplier<? extends ActivityStrategy> strategyFactory,
        String disabledMessage)
    {
        final String message = com.ticksense.common.TextValues.requireText(disabledMessage, "disabledMessage");
        return new ActivityDescriptor(
            definition,
            enabled,
            strategyFactory,
            (session, activityData, opportunityMarkers) ->
            {
                throw new IllegalArgumentException(message);
            },
            ActivityReportMode.DISABLED);
    }

    public ActivityDefinition definition()
    {
        return definition;
    }

    public boolean isEnabled()
    {
        return enabled.getAsBoolean();
    }

    public ActivityStrategy createStrategy()
    {
        return Objects.requireNonNull(strategyFactory.get(), "strategyFactory result");
    }

    public ReportBuilder reportBuilder()
    {
        return reportBuilder;
    }

    public ActivityReportMode getReportMode()
    {
        return reportMode;
    }

    public boolean reportsEnabled()
    {
        return reportMode == ActivityReportMode.ENABLED;
    }
}
