package com.ticksense.activities.gemmining;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ticksense.telemetry.StateChanges;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryJson;
import com.ticksense.telemetry.events.AnimationTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.ObjectStateTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import com.ticksense.telemetry.events.StatChangedTelemetryEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class GemMiningIdsTest
{
    @Test
    public void enabledStrategyRequiresVerifiedIds()
    {
        final GemMiningVerificationDecision decision = GemMiningIds.verificationDecision();

        assertEquals(decision.getStatus() == GemMiningVerificationDecision.Status.VERIFIED, decision.allowsStrategyEnablement());
        assertEquals(GemMiningVerificationDecision.Status.VERIFIED, decision.getStatus());
        assertTrue(decision.getBlockers().isEmpty());
        assertTrue(GemMiningIds.isGemMiningRegionId(11410));
        assertTrue(GemMiningIds.isGemRockObjectId(11380));
        assertTrue(GemMiningIds.isMiningAnimationId(625));
        assertTrue(GemMiningIds.isUncutGemItemId(1619));
    }

    @Test
    public void fixtureVerifiesUndergroundGemMiningEvidence() throws IOException
    {
        final List<TelemetryEnvelope> events = Files.readAllLines(
            Paths.get("src/test/resources/replays/gem-mining-basic.jsonl"),
            StandardCharsets.UTF_8)
            .stream()
            .filter(line -> !line.trim().isEmpty())
            .map(TelemetryJson::fromJsonLine)
            .collect(Collectors.toList());

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof RegionInstanceTelemetryEvent
                && ((RegionInstanceTelemetryEvent) envelope.getEvent()).getRegionId() == 11410));

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof ObjectStateTelemetryEvent
                && GemMiningIds.isGemRockObjectId(((ObjectStateTelemetryEvent) envelope.getEvent()).getObjectId())
                && StateChanges.AVAILABLE.equals(((ObjectStateTelemetryEvent) envelope.getEvent()).getStateChange())));

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof PlayerActionTelemetryEvent
                && "Mine".equals(((PlayerActionTelemetryEvent) envelope.getEvent()).getOption())
                && "Gem rock".equals(((PlayerActionTelemetryEvent) envelope.getEvent()).getTarget())));

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof AnimationTelemetryEvent
                && GemMiningIds.isMiningAnimationId(((AnimationTelemetryEvent) envelope.getEvent()).getAnimationId())));

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof StatChangedTelemetryEvent
                && "MINING".equals(((StatChangedTelemetryEvent) envelope.getEvent()).getSkill())
                && ((StatChangedTelemetryEvent) envelope.getEvent()).getXpDelta() > 0));

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof InventoryDeltaTelemetryEvent
                && ((InventoryDeltaTelemetryEvent) envelope.getEvent()).getDeltas().stream()
                .anyMatch(delta -> GemMiningIds.isUncutGemItemId(delta.getAfterItemId()))));

        assertTrue(events.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof ObjectStateTelemetryEvent
                && StateChanges.DEPLETED.equals(((ObjectStateTelemetryEvent) envelope.getEvent()).getStateChange())));

        final long availableCount = events.stream().filter(envelope ->
            envelope.getEvent() instanceof ObjectStateTelemetryEvent
                && StateChanges.AVAILABLE.equals(((ObjectStateTelemetryEvent) envelope.getEvent()).getStateChange()))
            .count();
        assertTrue(availableCount >= 2L);
    }
}
