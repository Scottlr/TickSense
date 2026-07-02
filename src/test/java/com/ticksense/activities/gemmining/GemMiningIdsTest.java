package com.ticksense.activities.gemmining;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;

public class GemMiningIdsTest
{
    @Test
    public void registryImportsNoRuneliteClasses() throws IOException
    {
        final String source = new String(
            Files.readAllBytes(Paths.get("src/main/java/com/ticksense/activities/gemmining/GemMiningIds.java")),
            StandardCharsets.UTF_8);

        assertFalse(source.contains("net.runelite."));
    }

    @Test
    public void enabledStrategyRequiresVerifiedIds()
    {
        final GemMiningVerificationDecision decision = GemMiningIds.verificationDecision();

        assertEquals(decision.getStatus() == GemMiningVerificationDecision.Status.VERIFIED, decision.allowsStrategyEnablement());
        assertEquals(GemMiningVerificationDecision.Status.PARTIALLY_VERIFIED, decision.getStatus());
        assertFalse(decision.getBlockers().isEmpty());
    }
}
