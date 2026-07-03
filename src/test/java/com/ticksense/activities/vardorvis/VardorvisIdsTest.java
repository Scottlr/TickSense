package com.ticksense.activities.vardorvis;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Test;

public class VardorvisIdsTest
{
    @Test
    public void registryImportsNoRuneliteClasses() throws IOException
    {
        final String source = new String(
            Files.readAllBytes(Paths.get("src/main/java/com/ticksense/activities/vardorvis/VardorvisIds.java")),
            StandardCharsets.UTF_8);

        assertFalse(source.contains("net.runelite."));
    }

    @Test
    public void normalReportsRequireVerifiedMechanicIds() throws IOException
    {
        final VardorvisVerificationDecision decision = VardorvisIds.verificationDecision();

        assertEquals(
            decision.getStatus() == VardorvisVerificationDecision.Status.VERIFIED,
            decision.allowsNormalReports());
        assertEquals(VardorvisVerificationDecision.Status.PARTIALLY_VERIFIED, decision.getStatus());
        assertEquals("2026-07-03", decision.getVerifiedOnDate());
        assertTrue(decision.getVerifiedMechanics().contains("boss-presence"));
        assertTrue(decision.getVerifiedMechanics().contains("head-presence"));
        assertFalse(decision.getUnresolvedMechanics().isEmpty());

        assertArrayEquals(new int[] {12223, 12224, 12228, 12425, 12426, 13656}, VardorvisIds.bossNpcIds());
        assertArrayEquals(new int[] {12226}, VardorvisIds.headNpcIds());
        assertArrayEquals(new int[0], VardorvisIds.rangedHeadProjectileIds());
        assertArrayEquals(new int[0], VardorvisIds.bloodSplatGraphicIds());
        assertArrayEquals(new int[0], VardorvisIds.axeMechanicIds());
        assertArrayEquals(new int[0], VardorvisIds.verifiedRegionIds());

        final List<String> readmeLines = Files.readAllLines(
            Paths.get("src/test/resources/replays/README.md"),
            StandardCharsets.UTF_8);
        final String readme = String.join("\n", readmeLines);

        assertTrue(readme.contains("vardorvis-basic.jsonl"));
        assertTrue(readme.contains("Vardorvis"));
        assertTrue(readme.contains("Remove or replace player names"));
    }
}
