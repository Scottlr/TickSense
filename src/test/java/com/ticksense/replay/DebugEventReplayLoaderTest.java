package com.ticksense.replay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ticksense.activities.ActivityDiagnostic;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import com.ticksense.storage.debug.DebugEventKind;
import com.ticksense.storage.debug.DebugEventRecord;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryJson;
import com.ticksense.telemetry.events.GameTickTelemetryEvent;
import com.ticksense.telemetry.events.NpcStateTelemetryEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.Test;

public class DebugEventReplayLoaderTest
{
    @Test
    public void loadsOnlyNormalizedTelemetryRecords() throws Exception
    {
        final Gson gson = new Gson();
        final Path debugFile = Files.createTempFile("ticksense-debug-replay", ".jsonl");
        final TelemetryEnvelope telemetry = TelemetryEnvelope.create(
            "event-debug",
            "session-debug",
            new GameTickTelemetryEvent(
                new EventTime(100L, 200L, 300, 400L, 500),
                Collections.singletonMap("source", "GameTick"),
                300));

        Files.write(
            debugFile,
            Arrays.asList(
                gson.toJson(DebugEventRecord.of(
                    DebugEventKind.ADAPTER_OBSERVATION,
                    "session-debug",
                    "ProjectileMoved",
                    new EventTime(90L, 190L, 299, 399L, 499),
                    "{\"projectileId\":1234}")),
                gson.toJson(DebugEventRecord.normalizedTelemetry(
                    "session-debug",
                    "GameTick",
                    telemetry.getEvent().getTime(),
                    TelemetryJson.toJsonLine(telemetry))),
                "{not-json}"),
            StandardCharsets.UTF_8);

        final List<TelemetryEnvelope> events = DebugEventReplayLoader.loadEvents(debugFile);

        assertEquals(1, events.size());
        assertEquals("event-debug", events.get(0).getEventId());
        assertEquals(300, ((GameTickTelemetryEvent) events.get(0).getEvent()).getTick());
    }

    @Test
    public void loadsRawRecordsFromDebugFixture() throws Exception
    {
        final Path fixture = resourcePath("replays/debug/scurrius-observe-only-debug.jsonl");

        final List<DebugEventRecord> records = DebugEventReplayLoader.loadRecords(fixture);

        assertEquals(2, records.size());
        assertEquals(DebugEventKind.NORMALIZED_TELEMETRY, records.get(0).getKind());
        assertEquals(DebugEventKind.ACTIVITY_DIAGNOSTIC, records.get(1).getKind());
        assertEquals("scurrius-observe-only", records.get(0).getSessionId());
        assertEquals("SCURRIUS", records.get(1).getSourceEventType());
    }

    @Test
    public void loadsTelemetryAndDiagnosticsFromObserveOnlyBossFixture() throws Exception
    {
        final Path fixture = resourcePath("replays/debug/scurrius-observe-only-debug.jsonl");

        final List<TelemetryEnvelope> events = DebugEventReplayLoader.loadEvents(fixture);
        final List<ActivityDiagnostic> diagnostics = DebugEventReplayLoader.loadActivityDiagnostics(fixture);

        assertEquals(1, events.size());
        assertTrue(events.get(0).getEvent() instanceof NpcStateTelemetryEvent);
        final NpcStateTelemetryEvent npcEvent = (NpcStateTelemetryEvent) events.get(0).getEvent();
        assertEquals(7221, npcEvent.getNpcRef().getId());
        assertEquals(9999, npcEvent.getAnimationId());
        assertEquals(8888, npcEvent.getGraphicId());

        assertEquals(1, diagnostics.size());
        final ActivityDiagnostic diagnostic = diagnostics.get(0);
        assertEquals(ActivityType.SCURRIUS, diagnostic.getActivityType());
        assertEquals("SUPPRESSED", diagnostic.getDecision());
        assertEquals(0.74D, diagnostic.getConfidence(), 0.0D);
        assertTrue(diagnostic.getEvidence().contains("Known boss NPC observed: npc:7221"));
        assertTrue(diagnostic.getEvidence().contains("Unverified event ID observed: animation:9999"));
        assertTrue(diagnostic.getEvidence().contains("Unverified event ID observed: graphic:8888"));
    }

    private static Path resourcePath(String resourcePath) throws Exception
    {
        return Paths.get(Objects.requireNonNull(
            DebugEventReplayLoaderTest.class.getClassLoader().getResource(resourcePath),
            "resource " + resourcePath).toURI());
    }
}
