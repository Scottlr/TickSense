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
}
