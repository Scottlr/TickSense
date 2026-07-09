package com.ticksense.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.ticksense.core.ActivityType;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ActivityModuleCatalogContractTest
{
    @Test
    public void productionModulesExposeCompleteDescriptors()
    {
        final List<ActivityModule> modules = ActivityModuleCatalog.productionModules();
        final Set<ActivityType> types = EnumSet.noneOf(ActivityType.class);

        for (ActivityModule module : modules)
        {
            final ActivityDescriptor descriptor = module.descriptor();

            assertNotNull(descriptor);
            assertSame(module.definition(), descriptor.definition());
            assertNotNull(module.reportBuilder());
            assertNotNull(module.createStrategy());
            assertEquals(module.definition(), module.createStrategy().getDefinition());
            assertTrue("Duplicate activity module for " + module.definition().getActivityType(), types.add(module.definition().getActivityType()));
            assertFalse(module.definition().getDisplayName().isEmpty());
        }
    }

    @Test
    public void observeOnlyBossModulesDeclareReportsDisabled()
    {
        for (ActivityModule module : ActivityModuleCatalog.productionModules())
        {
            final ActivityType type = module.definition().getActivityType();
            if (type == ActivityType.SCURRIUS
                || type == ActivityType.PHANTOM_MUSPAH
                || type == ActivityType.HUNLLEF
                || type == ActivityType.CORRUPTED_GAUNTLET
                || type == ActivityType.ARAXXOR)
            {
                assertEquals(ActivityReportMode.DISABLED, module.descriptor().getReportMode());
            }
        }
    }

    @Test
    public void productionModulesRespectActivitySupportConfig()
    {
        final ActivitySupportConfig supportConfig = ActivitySupportConfig.builder()
            .enable(ActivityType.GEM_MINING)
            .enable(ActivityType.VARDORVIS)
            .build();
        final List<ActivityModule> modules = ActivityModuleCatalog.productionModules(supportConfig);
        final Set<ActivityType> types = EnumSet.noneOf(ActivityType.class);

        for (ActivityModule module : modules)
        {
            types.add(module.definition().getActivityType());
        }

        assertEquals(EnumSet.of(ActivityType.GEM_MINING, ActivityType.VARDORVIS), types);
    }

    @Test
    public void currentActivitySupportConfigCoversProductionModules()
    {
        final ActivitySupportConfig supportConfig = ActivitySupportConfig.current();

        for (ActivityModule module : ActivityModuleCatalog.productionModules())
        {
            assertTrue(supportConfig.isActivitySupported(module.definition().getActivityType()));
        }
    }

    @Test
    public void availableModulesCoverEveryKnownActivityType()
    {
        final Set<ActivityType> availableTypes = moduleTypes(ActivityModuleCatalog.availableModules());
        final Set<ActivityType> expectedTypes = EnumSet.allOf(ActivityType.class);
        expectedTypes.remove(ActivityType.UNKNOWN);

        assertEquals(expectedTypes, availableTypes);
    }

    @Test
    public void plannedStubsStayOutOfProductionByDefault()
    {
        final Set<ActivityType> availableTypes = moduleTypes(ActivityModuleCatalog.availableModules());
        final Set<ActivityType> productionTypes = moduleTypes(ActivityModuleCatalog.productionModules());

        assertTrue(availableTypes.contains(ActivityType.TEMPOROSS));
        assertTrue(availableTypes.contains(ActivityType.CHAMBERS_OF_XERIC));
        assertTrue(availableTypes.contains(ActivityType.SAILING_PORT_TASKS));
        assertFalse(productionTypes.contains(ActivityType.TEMPOROSS));
        assertFalse(productionTypes.contains(ActivityType.CHAMBERS_OF_XERIC));
        assertFalse(productionTypes.contains(ActivityType.SAILING_PORT_TASKS));
    }

    @Test
    public void supportConfigCanExposePlannedStubWithoutReports()
    {
        final ActivitySupportConfig supportConfig = ActivitySupportConfig.builder()
            .enable(ActivityType.TEMPOROSS)
            .build();
        final List<ActivityModule> modules = ActivityModuleCatalog.productionModules(supportConfig);

        assertEquals(1, modules.size());
        assertEquals(ActivityType.TEMPOROSS, modules.get(0).definition().getActivityType());
        assertTrue(modules.get(0).isEnabled());
        assertEquals(ActivityReportMode.DISABLED, modules.get(0).descriptor().getReportMode());
    }

    private static Set<ActivityType> moduleTypes(List<ActivityModule> modules)
    {
        final Set<ActivityType> types = EnumSet.noneOf(ActivityType.class);
        for (ActivityModule module : modules)
        {
            assertTrue("Duplicate activity module for " + module.definition().getActivityType(), types.add(module.definition().getActivityType()));
        }
        return types;
    }
}
