package com.ticksense.replay;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.ticksense.core.EventTime;
import com.ticksense.storage.debug.DebugEventKind;
import com.ticksense.storage.debug.DebugEventRecord;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryJson;
import com.ticksense.telemetry.events.GameTickTelemetryEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
}
