package com.ticksense.replay;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.ticksense.activities.ActivityDiagnostic;
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
        final List<TelemetryEnvelope> events = new ArrayList<>();
        for (DebugEventRecord record : loadRecords(debugJsonlPath))
        {
            final TelemetryEnvelope event = telemetryEvent(record);
            if (event != null)
            {
                events.add(event);
            }
        }
        return Collections.unmodifiableList(events);
    }

    public static List<ActivityDiagnostic> loadActivityDiagnostics(Path debugJsonlPath) throws IOException
    {
        final List<ActivityDiagnostic> diagnostics = new ArrayList<>();
        for (DebugEventRecord record : loadRecords(debugJsonlPath))
        {
            final ActivityDiagnostic diagnostic = activityDiagnostic(record);
            if (diagnostic != null)
            {
                diagnostics.add(diagnostic);
            }
        }
        return Collections.unmodifiableList(diagnostics);
    }

    public static List<DebugEventRecord> loadRecords(Path debugJsonlPath) throws IOException
    {
        final Path normalizedPath = Objects.requireNonNull(debugJsonlPath, "debugJsonlPath");
        final List<DebugEventRecord> records = new ArrayList<>();
        for (String line : Files.readAllLines(normalizedPath, StandardCharsets.UTF_8))
        {
            final DebugEventRecord record = debugRecord(line);
            if (record != null)
            {
                records.add(record);
            }
        }
        return Collections.unmodifiableList(records);
    }

    private static DebugEventRecord debugRecord(String line)
    {
        if (line == null || line.trim().isEmpty())
        {
            return null;
        }
        try
        {
            final DebugEventRecord record = GSON.fromJson(line, DebugEventRecord.class);
            if (record == null || record.getKind() == null)
            {
                return null;
            }
            return record;
        }
        catch (JsonParseException ex)
        {
            return null;
        }
    }

    private static TelemetryEnvelope telemetryEvent(DebugEventRecord record)
    {
        if (record == null || record.getKind() != DebugEventKind.NORMALIZED_TELEMETRY)
        {
            return null;
        }
        try
        {
            return TelemetryJson.fromJsonLine(record.getPayloadJson());
        }
        catch (IllegalArgumentException ex)
        {
            return null;
        }
    }

    private static ActivityDiagnostic activityDiagnostic(DebugEventRecord record)
    {
        if (record == null || record.getKind() != DebugEventKind.ACTIVITY_DIAGNOSTIC)
        {
            return null;
        }
        try
        {
            return GSON.fromJson(record.getPayloadJson(), ActivityDiagnostic.class);
        }
        catch (JsonParseException ex)
        {
            return null;
        }
    }
}
