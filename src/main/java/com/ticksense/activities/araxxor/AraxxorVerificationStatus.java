package com.ticksense.activities.araxxor;

public enum AraxxorVerificationStatus
{
    UNVERIFIED(false),
    PARTIALLY_VERIFIED(false),
    VERIFIED(true);

    private final boolean allowsNormalStrategyEnablement;

    AraxxorVerificationStatus(boolean allowsNormalStrategyEnablement)
    {
        this.allowsNormalStrategyEnablement = allowsNormalStrategyEnablement;
    }

    public boolean allowsNormalStrategyEnablement()
    {
        return allowsNormalStrategyEnablement;
    }
}
