package com.ticksense.activities.inferno;

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

public class InfernoIdsTest
{
    @Test
    public void registryImportsNoRuneliteClasses() throws IOException
    {
        final String source = new String(
            Files.readAllBytes(Paths.get("src/main/java/com/ticksense/activities/inferno/InfernoIds.java")),
            StandardCharsets.UTF_8);

        assertFalse(source.contains("net.runelite."));
    }

    @Test
    public void prayerTimingRequiresVerifiedPrayerEvidence() throws IOException
    {
        final InfernoVerificationDecision decision = InfernoIds.verificationDecision();

        assertEquals(InfernoVerificationDecision.Status.PARTIALLY_VERIFIED, decision.getStatus());
        assertEquals("2026-07-03", decision.getVerifiedOnDate());
        assertEquals(InfernoVerificationDecision.EvidenceStatus.PARTIALLY_VERIFIED, decision.getWaveEvidenceStatus());
        assertEquals(InfernoVerificationDecision.EvidenceStatus.PARTIALLY_VERIFIED, decision.getNibblerEvidenceStatus());
        assertEquals(InfernoVerificationDecision.EvidenceStatus.BLOCKED, decision.getPrayerEvidenceStatus());
        assertEquals(InfernoVerificationDecision.EvidenceStatus.PARTIALLY_VERIFIED, decision.getSupplyEvidenceStatus());
        assertEquals(InfernoVerificationDecision.EvidenceStatus.BLOCKED, decision.getDeathEvidenceStatus());
        assertFalse(decision.allowsPrayerTimingReports());
        assertFalse(decision.getBlockers().isEmpty());

        assertArrayEquals(new int[] {7674, 7675}, InfernoIds.nibblerNpcIds());
        assertArrayEquals(new int[] {7691, 7692, 7693, 7694, 7695, 7696, 7697, 7698, 7699, 7700, 7704, 7706}, InfernoIds.waveNpcIds());
        assertArrayEquals(new int[] {2434, 3024, 6685, 3144, 811, 12926}, InfernoIds.supplyItemIds());
        assertArrayEquals(new int[0], InfernoIds.verifiedRegionIds());
        assertArrayEquals(new int[0], InfernoIds.prayerStateIds());
        assertArrayEquals(new int[0], InfernoIds.deathTimelineIds());

        final List<String> readmeLines = Files.readAllLines(
            Paths.get("src/test/resources/replays/README.md"),
            StandardCharsets.UTF_8);
        final String readme = String.join("\n", readmeLines);

        assertTrue(readme.contains("inferno-wave-basic.jsonl"));
        assertTrue(readme.contains("Inferno"));
        assertTrue(readme.contains("Remove or replace player names"));
    }
}
