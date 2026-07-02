package com.ticksense.activities;

import java.util.Collection;

public interface ActivityStrategyFactory
{
    Collection<? extends ActivityStrategy> createStrategies();
}
