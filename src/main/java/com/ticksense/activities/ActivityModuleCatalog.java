package com.ticksense.activities;

import com.ticksense.activities.araxxor.AraxxorModule;
import com.ticksense.activities.construction.ConstructionModule;
import com.ticksense.activities.gemmining.GemMiningModule;
import com.ticksense.activities.inferno.InfernoModule;
import com.ticksense.activities.vardorvis.VardorvisModule;
import com.ticksense.analytics.ReportBuilder;
import com.ticksense.common.ImmutableCollections;
import com.ticksense.core.ActivityType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ActivityModuleCatalog
{
    private ActivityModuleCatalog()
    {
    }

    public static List<ActivityModule> productionModules()
    {
        return ImmutableCollections.immutableList(java.util.Arrays.asList(
            new GemMiningModule(),
            new ConstructionModule(),
            new AraxxorModule(),
            new VardorvisModule(),
            new InfernoModule()));
    }

    public static List<ActivityModule> enabledModules(List<ActivityModule> modules)
    {
        final List<ActivityModule> enabled = new ArrayList<>();
        for (ActivityModule module : modules)
        {
            if (module.isEnabled())
            {
                enabled.add(module);
            }
        }
        return ImmutableCollections.immutableList(enabled);
    }

    public static ActivityStrategyFactory strategyFactory(List<ActivityModule> modules)
    {
        final List<ActivityModule> enabledModules = enabledModules(modules);
        return () ->
        {
            final List<ActivityStrategy> strategies = new ArrayList<>();
            for (ActivityModule module : enabledModules)
            {
                strategies.add(module.createStrategy());
            }
            return ImmutableCollections.immutableList(strategies);
        };
    }

    public static Map<ActivityType, ReportBuilder> reportBuilders(List<ActivityModule> modules)
    {
        final Map<ActivityType, ReportBuilder> builders = new LinkedHashMap<>();
        for (ActivityModule module : modules)
        {
            builders.put(module.definition().getActivityType(), module.reportBuilder());
        }
        return ImmutableCollections.immutableMap(builders);
    }
}
