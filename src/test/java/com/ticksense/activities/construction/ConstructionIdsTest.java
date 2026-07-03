package com.ticksense.activities.construction;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryJson;
import com.ticksense.telemetry.events.AnimationTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.MenuInteractionTelemetryEvent;
import com.ticksense.telemetry.events.ObjectStateTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import com.ticksense.telemetry.events.StatChangedTelemetryEvent;
import com.ticksense.telemetry.events.WidgetTelemetryEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
        assertEquals(ConstructionVerificationDecision.Status.VERIFIED, decision.getStatus());
        assertTrue(decision.getBlockers().isEmpty());
        assertTrue(decision.getVerifiedEvidence().size() >= 5);
        assertArrayEquals(new int[] {15403}, ConstructionIds.buildSpotObjectIds());
        assertArrayEquals(new int[] {13565, 13566, 13567}, ConstructionIds.builtObjectIds());
        assertArrayEquals(new int[] {8778, 2347, 8794, 9625, 29774}, ConstructionIds.methodItemIds());
        assertArrayEquals(new int[] {3676, 8912}, ConstructionIds.buildAnimationIds());
        assertArrayEquals(new int[] {12, 15}, ConstructionIds.bankWidgetGroupIds());
        assertArrayEquals(new int[] {458}, ConstructionIds.constructionWidgetGroupIds());
        assertArrayEquals(new int[] {12}, ConstructionIds.constructionWidgetChildIds());

        final List<String> readmeLines = Files.readAllLines(
            Paths.get("src/test/resources/replays/README.md"),
            StandardCharsets.UTF_8);
        final String readme = String.join("\n", readmeLines);

        assertTrue(readme.contains("construction-basic.jsonl"));
        assertTrue(readme.contains("oak-larder"));
        assertTrue(readme.contains("Remove or replace player names"));
    }

    @Test
    public void fixtureVerifiesOakLarderConstructionEvidence() throws IOException
    {
        final List<TelemetryEnvelope> events = Files.readAllLines(
            Paths.get("src/test/resources/replays/construction-basic.jsonl"),
            StandardCharsets.UTF_8)
            .stream()
            .filter(line -> !line.trim().isEmpty())
            .map(TelemetryJson::fromJsonLine)
            .collect(Collectors.toList());

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof ObjectStateTelemetryEvent
                && Arrays.stream(ConstructionIds.buildSpotObjectIds()).anyMatch(id -> id == ((ObjectStateTelemetryEvent) envelope.getEvent()).getObjectId())
                && "AVAILABLE".equals(((ObjectStateTelemetryEvent) envelope.getEvent()).getStateChange())));

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof MenuInteractionTelemetryEvent
                && "MenuOpened".equals(((MenuInteractionTelemetryEvent) envelope.getEvent()).getInteractionType())
                && "Build".equals(((MenuInteractionTelemetryEvent) envelope.getEvent()).getSelectedOption())
                && "Larder space".equals(((MenuInteractionTelemetryEvent) envelope.getEvent()).getTarget())));

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof PlayerActionTelemetryEvent
                && "Build".equals(((PlayerActionTelemetryEvent) envelope.getEvent()).getOption())
                && "Larder space".equals(((PlayerActionTelemetryEvent) envelope.getEvent()).getTarget())));

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof WidgetTelemetryEvent
                && Arrays.stream(ConstructionIds.constructionWidgetGroupIds()).anyMatch(id -> id == ((WidgetTelemetryEvent) envelope.getEvent()).getGroupId())
                && Arrays.stream(ConstructionIds.constructionWidgetChildIds()).anyMatch(id -> id == ((WidgetTelemetryEvent) envelope.getEvent()).getChildId())
                && "WidgetLoaded".equals(((WidgetTelemetryEvent) envelope.getEvent()).getEventKind())));

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof AnimationTelemetryEvent
                && Arrays.stream(ConstructionIds.buildAnimationIds()).anyMatch(id -> id == ((AnimationTelemetryEvent) envelope.getEvent()).getAnimationId())));

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof InventoryDeltaTelemetryEvent
                && ((InventoryDeltaTelemetryEvent) envelope.getEvent()).getDeltas().stream()
                .anyMatch(delta -> delta.getBeforeItemId() == 8778 && delta.getAfterItemId() == -1)));

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof StatChangedTelemetryEvent
                && "CONSTRUCTION".equals(((StatChangedTelemetryEvent) envelope.getEvent()).getSkill())
                && ((StatChangedTelemetryEvent) envelope.getEvent()).getXpDelta() > 0));

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof PlayerActionTelemetryEvent
                && "Remove".equals(((PlayerActionTelemetryEvent) envelope.getEvent()).getOption())
                && "Oak larder".equals(((PlayerActionTelemetryEvent) envelope.getEvent()).getTarget())));

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof ObjectStateTelemetryEvent
                && Arrays.stream(ConstructionIds.builtObjectIds()).anyMatch(id -> id == ((ObjectStateTelemetryEvent) envelope.getEvent()).getObjectId())
                && "BUILT".equals(((ObjectStateTelemetryEvent) envelope.getEvent()).getStateChange())));

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof WidgetTelemetryEvent
                && Arrays.stream(ConstructionIds.bankWidgetGroupIds()).anyMatch(id -> id == ((WidgetTelemetryEvent) envelope.getEvent()).getGroupId())
                && "WidgetLoaded".equals(((WidgetTelemetryEvent) envelope.getEvent()).getEventKind())));
    }
}
