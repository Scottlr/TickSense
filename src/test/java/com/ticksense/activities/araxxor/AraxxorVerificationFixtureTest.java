package com.ticksense.activities.araxxor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ticksense.core.EntityRef;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryJson;
import com.ticksense.telemetry.events.DamageTelemetryEvent;
import com.ticksense.telemetry.events.InteractingChangedTelemetryEvent;
import com.ticksense.telemetry.events.NpcStateTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class AraxxorVerificationFixtureTest
{
    @Test
    public void fixtureContainsSpiderSpawnEvidenceWhenVerified() throws IOException
    {
        final AraxxorVerificationDecision decision = AraxxorVerificationDecision.current();
        if (decision.getStatus() != AraxxorVerificationDecision.Status.VERIFIED)
        {
            assertFalse(decision.allowsNormalStrategyEnablement());
            return;
        }

        final List<TelemetryEnvelope> spiderFixture = loadFixture("src/test/resources/replays/araxxor-spider-basic.jsonl");
        assertTrue(spiderFixture.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof NpcStateTelemetryEvent
                && "SPAWNED".equals(((NpcStateTelemetryEvent) envelope.getEvent()).getStateChange())
                && isVerifiedSpider(((NpcStateTelemetryEvent) envelope.getEvent()).getNpcRef())));
    }

    @Test
    public void fixtureContainsEngagementEvidenceWhenVerified() throws IOException
    {
        final AraxxorVerificationDecision decision = AraxxorVerificationDecision.current();
        if (decision.getStatus() != AraxxorVerificationDecision.Status.VERIFIED)
        {
            assertFalse(decision.allowsNormalStrategyEnablement());
            return;
        }

        final List<TelemetryEnvelope> spiderFixture = loadFixture("src/test/resources/replays/araxxor-spider-basic.jsonl");
        assertTrue(spiderFixture.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof PlayerActionTelemetryEvent
                && "Attack".equals(((PlayerActionTelemetryEvent) envelope.getEvent()).getOption())
                && isVerifiedSpider(((PlayerActionTelemetryEvent) envelope.getEvent()).getTargetRef())));
        assertTrue(spiderFixture.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof InteractingChangedTelemetryEvent
                && ((InteractingChangedTelemetryEvent) envelope.getEvent()).getActorRef().getType() == EntityRef.Type.LOCAL_PLAYER
                && isVerifiedSpider(((InteractingChangedTelemetryEvent) envelope.getEvent()).getInteractingRef())));
        assertTrue(spiderFixture.stream().anyMatch(envelope ->
            envelope.getEvent() instanceof DamageTelemetryEvent
                && isVerifiedSpider(((DamageTelemetryEvent) envelope.getEvent()).getTargetRef())));
    }

    @Test
    public void blockedDecisionExplainsMissingEvidence() throws IOException
    {
        final AraxxorVerificationDecision decision = AraxxorVerificationDecision.current();

        assertEquals(AraxxorVerificationDecision.Status.BLOCKED, decision.getStatus());
        assertFalse(decision.allowsNormalStrategyEnablement());
        assertEquals("2026-07-03", decision.getVerifiedOnDate());
        assertTrue(decision.getEvidence().stream().anyMatch(line -> line.contains("NpcID")));
        assertTrue(decision.getUnresolvedQuestions().stream().anyMatch(line -> line.contains("spider spawn")));
        assertTrue(decision.getUnresolvedQuestions().stream().anyMatch(line -> line.contains("attack click")));
        assertTrue(decision.getUnresolvedQuestions().stream().anyMatch(line -> line.contains("Teleport-mid-kill")));
        assertTrue(loadFixture("src/test/resources/replays/araxxor-spider-basic.jsonl").isEmpty());
        assertTrue(loadFixture("src/test/resources/replays/araxxor-teleport-midkill.jsonl").isEmpty());
    }

    private static List<TelemetryEnvelope> loadFixture(String path) throws IOException
    {
        final List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8)
            .stream()
            .filter(line -> !line.trim().isEmpty())
            .collect(Collectors.toList());
        if (lines.isEmpty())
        {
            return Collections.emptyList();
        }
        return lines.stream().map(TelemetryJson::fromJsonLine).collect(Collectors.toList());
    }

    private static boolean isVerifiedSpider(EntityRef ref)
    {
        if (ref == null || ref.getType() != EntityRef.Type.NPC)
        {
            return false;
        }
        for (int spiderId : AraxxorIds.spiderNpcIds())
        {
            if (spiderId == ref.getId())
            {
                return true;
            }
        }
        return false;
    }
}
