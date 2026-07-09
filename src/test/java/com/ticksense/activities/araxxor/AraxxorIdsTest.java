package com.ticksense.activities.araxxor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.runelite.api.NpcID;
import org.junit.Test;

public class AraxxorIdsTest
{
    @Test
    public void enabledStrategyRequiresVerifiedBossIds()
    {
        if (AraxxorIds.allowsStrategyEnablement())
        {
            assertEquals(AraxxorVerificationStatus.VERIFIED, AraxxorIds.verificationStatus());
            assertTrue(AraxxorIds.araxxorNpcIds().length > 0);
        }
        else
        {
            assertFalse(AraxxorIds.verificationStatus().allowsNormalStrategyEnablement()
                && AraxxorIds.araxxorNpcIds().length == 0);
        }
    }

    @Test
    public void enabledStrategyRequiresVerifiedSpiderIds()
    {
        if (AraxxorIds.allowsStrategyEnablement())
        {
            assertEquals(AraxxorVerificationStatus.VERIFIED, AraxxorIds.verificationStatus());
            assertTrue(AraxxorIds.spiderNpcIds().length > 0);
        }
        else
        {
            assertFalse(AraxxorIds.verificationStatus().allowsNormalStrategyEnablement()
                && AraxxorIds.spiderNpcIds().length == 0);
        }
    }

    @Test
    public void registryStaysBlockedUntilAraxxorFixturesExist()
    {
        assertEquals(AraxxorVerificationStatus.PARTIALLY_VERIFIED, AraxxorIds.verificationStatus());
        assertFalse(AraxxorIds.allowsStrategyEnablement());
        assertTrue(AraxxorIds.araxxorNpcIds().length > 0);
        assertTrue(AraxxorIds.spiderNpcIds().length > 0);
        assertTrue(AraxxorIds.isAraxxorNpcId(NpcID.ARAXXOR));
        assertTrue(AraxxorIds.isSpiderNpcId(NpcID.MIRRORBACK_ARAXYTE));
        assertFalse(AraxxorIds.hasVerifiedRegionIds());
        assertFalse(AraxxorIds.isVerifiedRegionId(12345));
        assertTrue(AraxxorIds.blockers().stream().anyMatch(blocker -> blocker.contains("T029")));
    }
}
