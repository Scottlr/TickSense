package com.ticksense.replay;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.ticksense.storage.debug.DebugEventKind;
import com.ticksense.storage.debug.DebugEventRecord;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryJson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DebugEventReplayLoader
{
    private static final Gson GSON = new Gson();

    private DebugEventReplayLoader()
    {
    }

    public static List<TelemetryEnvelope> loadEvents(Path debugJsonlPath) throws IOException
    {
        final Path normalizedPath = Objects.requireNonNull(debugJsonlPath, "debugJsonlPath");
        final List<TelemetryEnvelope> events = new ArrayList<>();
        for (String line : Files.readAllLines(normalizedPath, StandardCharsets.UTF_8))
        {
            final TelemetryEnvelope event = telemetryEvent(line);
            if (event != null)
            {
                events.add(event);
            }
        }
        return Collections.unmodifiableList(events);
    }

    private static TelemetryEnvelope telemetryEvent(String line)
    {
        if (line == null || line.trim().isEmpty())
        {
            return null;
        }
        try
        {
            final DebugEventRecord record = GSON.fromJson(line, DebugEventRecord.class);
            if (record == null || record.getKind() != DebugEventKind.NORMALIZED_TELEMETRY)
            {
                return null;
            }
            return TelemetryJson.fromJsonLine(record.getPayloadJson());
        }
        catch (JsonParseException | IllegalArgumentException ex)
        {
            return null;
        }
    }
}
