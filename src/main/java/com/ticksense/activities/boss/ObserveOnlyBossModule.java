package com.ticksense.activities.boss;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import java.util.Objects;
import java.util.function.IntPredicate;

public final class ObserveOnlyBossModule extends SimpleActivityModule
{
    public ObserveOnlyBossModule(
        ActivityDefinition definition,
        IntPredicate bossNpcMatcher,
        String reportDisabledMessage)
    {
        super(ActivityDescriptor.reportsDisabled(
            Objects.requireNonNull(definition, "definition"),
            () -> true,
            () -> new ObserveOnlyBossStrategy(definition, Objects.requireNonNull(bossNpcMatcher, "bossNpcMatcher")),
            Objects.requireNonNull(reportDisabledMessage, "reportDisabledMessage")));
    }
}
