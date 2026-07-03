package com.ticksense.activities.araxxor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;

public class AraxxorIdsTest
{
    @Test
    public void registryImportsNoRuneliteClasses() throws IOException
    {
        final String source = new String(
            Files.readAllBytes(Paths.get("src/main/java/com/ticksense/activities/araxxor/AraxxorIds.java")),
            StandardCharsets.UTF_8);

        assertFalse(source.contains("net.runelite."));
    }

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
        assertTrue(AraxxorIds.isAraxxorNpcId(13668));
        assertTrue(AraxxorIds.isSpiderNpcId(13671));
        assertFalse(AraxxorIds.hasVerifiedRegionIds());
        assertFalse(AraxxorIds.isVerifiedRegionId(12345));
        assertTrue(AraxxorIds.blockers().stream().anyMatch(blocker -> blocker.contains("T029")));
    }
}
