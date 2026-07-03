package com.ticksense.storage.debug;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ticksense.activities.ActivityDiagnostic;
import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.ActivityMarkerTypes;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryJson;
import com.ticksense.telemetry.events.GameTickTelemetryEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class DebugEventRecorderTest
{
    private static final Gson GSON = new Gson();

    @Test
    public void writesVersionedNormalizedJsonlWhenEnabled() throws IOException
    {
        final Path tempDir = Files.createTempDirectory("ticksense-debug-enabled");
        final DebugEventRecorder recorder = new DebugEventRecorder(tempDir, () -> "session-enabled");

        recorder.startSession(true, 25, 5);
        recorder.accept(envelope("event-enabled", "session-enabled"));
        recorder.close();

        final List<Path> files = jsonlFiles(tempDir);
        assertEquals(1, files.size());

        final String jsonLine = Files.readAllLines(files.get(0), StandardCharsets.UTF_8).get(0);
        final DebugEventRecord debugRecord = GSON.fromJson(jsonLine, DebugEventRecord.class);
        assertEquals(DebugEventRecord.SCHEMA_VERSION, debugRecord.getDebugSchemaVersion());
        assertEquals(DebugEventKind.NORMALIZED_TELEMETRY, debugRecord.getKind());
        assertEquals("session-enabled", debugRecord.getSessionId());
        assertEquals("GameTick", debugRecord.getSourceEventType());
        assertEquals(100L, debugRecord.getTime().getWallTimeMillis());
        assertEquals(200L, debugRecord.getTime().getMonotonicNanos());
        assertEquals(300, debugRecord.getTime().getGameTick());
        assertEquals(400L, debugRecord.getTime().getClientCycle());
        assertEquals(500, debugRecord.getTime().getClientTickSequence());

        final TelemetryEnvelope parsed = TelemetryJson.fromJsonLine(debugRecord.getPayloadJson());
        assertEquals("event-enabled", parsed.getEventId());
        assertEquals("session-enabled", parsed.getSessionId());

        final DebugEventRecorder disabledRecorder = new DebugEventRecorder(tempDir, () -> "session-disabled");
        disabledRecorder.startSession(false, 25, 5);
        disabledRecorder.accept(envelope("event-disabled", "session-disabled"));
        disabledRecorder.close();

        assertEquals(1, jsonlFiles(tempDir).size());
    }

    @Test
    public void appliesRetentionLimit() throws Exception
    {
        final Path tempDir = Files.createTempDirectory("ticksense-debug-retention");

        final DebugEventRecorder first = new DebugEventRecorder(tempDir, () -> "session-1");
        first.startSession(true, 25, 2);
        first.accept(envelope("event-1", "session-1"));
        first.close();
        Thread.sleep(5L);

        final DebugEventRecorder second = new DebugEventRecorder(tempDir, () -> "session-2");
        second.startSession(true, 25, 2);
        second.accept(envelope("event-2", "session-2"));
        second.close();
        Thread.sleep(5L);

        final DebugEventRecorder third = new DebugEventRecorder(tempDir, () -> "session-3");
        third.startSession(true, 25, 2);
        third.accept(envelope("event-3", "session-3"));
        third.close();

        final List<Path> files = jsonlFiles(tempDir);
        assertEquals(2, files.size());

        final String allFileNames = files.stream()
            .map(path -> path.getFileName().toString())
            .collect(Collectors.joining(","));
        assertFalse(allFileNames.contains("-session-1.jsonl"));
        assertTrue(allFileNames.contains("-session-2.jsonl"));
        assertTrue(allFileNames.contains("-session-3.jsonl"));
    }

    @Test
    public void writesAdapterObservationRecord() throws IOException
    {
        final Path tempDir = Files.createTempDirectory("ticksense-debug-observation");
        final DebugEventRecorder recorder = new DebugEventRecorder(tempDir, () -> "session-observation");

        recorder.startSession(true, 25, 5);
        recorder.record(
            DebugEventKind.ADAPTER_OBSERVATION,
            "session-observation",
            "ProjectileMoved",
            new EventTime(101L, 202L, 303, 404L, 505),
            "{\"projectileId\":1234}");
        recorder.close();

        final String jsonLine = Files.readAllLines(jsonlFiles(tempDir).get(0), StandardCharsets.UTF_8).get(0);
        final DebugEventRecord debugRecord = GSON.fromJson(jsonLine, DebugEventRecord.class);
        assertEquals(DebugEventKind.ADAPTER_OBSERVATION, debugRecord.getKind());
        assertEquals("session-observation", debugRecord.getSessionId());
        assertEquals("ProjectileMoved", debugRecord.getSourceEventType());
        assertEquals(303, debugRecord.getTime().getGameTick());
        assertEquals("{\"projectileId\":1234}", debugRecord.getPayloadJson());
    }

    @Test
    public void writesActivityOpportunityAndDiagnosticRecords() throws IOException
    {
        final Path tempDir = Files.createTempDirectory("ticksense-debug-activity");
        final DebugEventRecorder recorder = new DebugEventRecorder(tempDir, () -> "session-activity");
        final ActivityId activityId = ActivityId.of("activity-debug");

        recorder.startSession(true, 25, 5);
        recorder.recordActivityMarker(new ActivityMarker(
            "activity-marker-1",
            activityId,
            ActivityType.ARAXXOR,
            ActivityMarkerTypes.STARTED,
            new EventTime(110L, 220L, 330, 440L, 550),
            Collections.singletonMap("confidence", "0.9")));
        recorder.recordOpportunityMarker(new OpportunityMarker(
            "opportunity-marker-1",
            "opportunity-1",
            activityId,
            "TEST_OPPORTUNITY",
            OpportunityStatus.OPEN,
            new EventTime(111L, 222L, 333, 444L, 555),
            Collections.singletonMap("source", "test"),
            Collections.emptyList()));
        recorder.recordActivityDiagnostic(
            "session-activity",
            new ActivityDiagnostic(
                ActivityType.ARAXXOR,
                0.90D,
                "STARTED",
                "",
                new EventTime(112L, 224L, 336, 448L, 560),
                Collections.singletonList("boss present")));
        recorder.close();

        final List<String> jsonLines = Files.readAllLines(jsonlFiles(tempDir).get(0), StandardCharsets.UTF_8);
        assertEquals(DebugEventKind.ACTIVITY_MARKER, GSON.fromJson(jsonLines.get(0), DebugEventRecord.class).getKind());
        assertEquals("activity-debug", GSON.fromJson(jsonLines.get(0), DebugEventRecord.class).getSessionId());
        assertEquals(DebugEventKind.OPPORTUNITY_MARKER, GSON.fromJson(jsonLines.get(1), DebugEventRecord.class).getKind());
        assertEquals("TEST_OPPORTUNITY", GSON.fromJson(jsonLines.get(1), DebugEventRecord.class).getSourceEventType());
        assertEquals(DebugEventKind.ACTIVITY_DIAGNOSTIC, GSON.fromJson(jsonLines.get(2), DebugEventRecord.class).getKind());
        assertEquals("ARAXXOR", GSON.fromJson(jsonLines.get(2), DebugEventRecord.class).getSourceEventType());
    }

    private static List<Path> jsonlFiles(Path directory) throws IOException
    {
        try (Stream<Path> files = Files.list(directory))
        {
            return files
                .filter(path -> path.getFileName().toString().endsWith(".jsonl"))
                .collect(Collectors.toList());
        }
    }

    private static TelemetryEnvelope envelope(String eventId, String sessionId)
    {
        return TelemetryEnvelope.create(
            eventId,
            sessionId,
            new GameTickTelemetryEvent(new EventTime(100L, 200L, 300, 400L, 500), Collections.singletonMap("source", "GameTick"), 300));
    }
}
