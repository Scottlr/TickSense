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
import net.runelite.api.ItemID;
import net.runelite.api.NpcID;
import org.junit.Test;

public class InfernoIdsTest
{
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

        assertArrayEquals(new int[] {NpcID.JALNIBREK, NpcID.JALNIBREK_7675}, InfernoIds.nibblerNpcIds());
        assertArrayEquals(
            new int[] {
                NpcID.JALNIB,
                NpcID.JALMEJRAH,
                NpcID.JALAK,
                NpcID.JALAKREKMEJ,
                NpcID.JALAKREKXIL,
                NpcID.JALAKREKKET,
                NpcID.JALIMKOT,
                NpcID.JALXIL,
                NpcID.JALZEK,
                NpcID.JALTOKJAD,
                NpcID.JALTOKJAD_7704,
                NpcID.TZKALZUK
            },
            InfernoIds.waveNpcIds());
        assertArrayEquals(
            new int[] {
                ItemID.PRAYER_POTION4,
                ItemID.SUPER_RESTORE4,
                ItemID.SARADOMIN_BREW4,
                ItemID.COOKED_KARAMBWAN,
                ItemID.RUNE_DART,
                ItemID.TOXIC_BLOWPIPE
            },
            InfernoIds.supplyItemIds());
        assertArrayEquals(new int[0], InfernoIds.verifiedRegionIds());
        assertArrayEquals(new int[0], InfernoIds.prayerStateIds());
        assertArrayEquals(new int[0], InfernoIds.deathTimelineIds());
        assertTrue(InfernoIds.isNibblerNpcId(NpcID.JALNIBREK));
        assertTrue(InfernoIds.isWaveNpcId(NpcID.JALNIB));
        assertTrue(InfernoIds.isSupplyItemId(ItemID.PRAYER_POTION4));
        assertFalse(InfernoIds.hasVerifiedRegionIds());
        assertFalse(InfernoIds.isPrayerStateId(12345));

        final List<String> readmeLines = Files.readAllLines(
            Paths.get("src/test/resources/replays/README.md"),
            StandardCharsets.UTF_8);
        final String readme = String.join("\n", readmeLines);

        assertTrue(readme.contains("inferno-wave-basic.jsonl"));
        assertTrue(readme.contains("Inferno"));
        assertTrue(readme.contains("Remove or replace player names"));
    }
}
