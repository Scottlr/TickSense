package com.ticksense.activities.construction;

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

public class ConstructionIdsTest
{
    @Test
    public void registryImportsNoRuneliteClasses() throws IOException
    {
        final String source = new String(
            Files.readAllBytes(Paths.get("src/main/java/com/ticksense/activities/construction/ConstructionIds.java")),
            StandardCharsets.UTF_8);

        assertFalse(source.contains("net.runelite."));
    }

    @Test
    public void enabledStrategyRequiresVerifiedMethodIds() throws IOException
    {
        final ConstructionVerificationDecision decision = ConstructionIds.verificationDecision();

        assertEquals("oak-larder", ConstructionIds.approvedMethodName());
        assertEquals("oak-larder", decision.getMethodName());
        assertEquals(
            decision.getStatus() == ConstructionVerificationDecision.Status.VERIFIED,
            decision.allowsStrategyEnablement());
        assertEquals(ConstructionVerificationDecision.Status.PARTIALLY_VERIFIED, decision.getStatus());
        assertFalse(decision.getBlockers().isEmpty());
        assertTrue(decision.getVerifiedEvidence().size() >= 3);
        assertArrayEquals(new int[] {15403}, ConstructionIds.buildSpotObjectIds());
        assertArrayEquals(new int[] {13565, 13566, 13567}, ConstructionIds.builtObjectIds());
        assertArrayEquals(new int[] {8778, 2347, 8794, 9625, 29774}, ConstructionIds.methodItemIds());
        assertArrayEquals(new int[] {3676, 8912}, ConstructionIds.buildAnimationIds());
        assertArrayEquals(new int[] {12, 15}, ConstructionIds.bankWidgetGroupIds());

        final List<String> readmeLines = Files.readAllLines(
            Paths.get("src/test/resources/replays/README.md"),
            StandardCharsets.UTF_8);
        final String readme = String.join("\n", readmeLines);

        assertTrue(readme.contains("construction-basic.jsonl"));
        assertTrue(readme.contains("oak-larder"));
        assertTrue(readme.contains("Remove or replace player names"));
    }
}
