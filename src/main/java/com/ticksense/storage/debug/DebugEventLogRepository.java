package com.ticksense.storage.debug;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.ticksense.storage.ActivityTimelineWindow;
import com.ticksense.storage.TickSenseDataPaths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DebugEventLogRepository
{
    private static final String DEBUG_DIRECTORY = "debug";

    private final TickSenseDataPaths dataPaths;
    private final Gson gson;

    public DebugEventLogRepository(TickSenseDataPaths dataPaths, Gson gson)
    {
        this.dataPaths = Objects.requireNonNull(dataPaths, "dataPaths");
        this.gson = Objects.requireNonNull(gson, "gson");
    }

    public List<String> readLines(ActivityTimelineWindow window) throws IOException
    {
        final ActivityTimelineWindow normalizedWindow = Objects.requireNonNull(window, "window");
        final Path debugDirectory = dataPaths.getTickSenseRoot().resolve(DEBUG_DIRECTORY);
        final List<String> lines = new ArrayList<>();
        if (Files.notExists(debugDirectory))
        {
            return lines;
        }

        try (Stream<Path> fileStream = Files.list(debugDirectory))
        {
            final List<Path> files = fileStream
                .filter(path -> path.getFileName().toString().endsWith(".jsonl"))
                .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                .collect(Collectors.toList());

            for (Path file : files)
            {
                for (String line : Files.readAllLines(file, StandardCharsets.UTF_8))
                {
                    if (line.trim().isEmpty())
                    {
                        continue;
                    }
                    if (matchesWindow(line, normalizedWindow))
                    {
                        lines.add(line);
                    }
                }
            }
        }
        return lines;
    }

    private boolean matchesWindow(String line, ActivityTimelineWindow window)
    {
        try
        {
            final DebugEventRecord record = gson.fromJson(line, DebugEventRecord.class);
            return record != null && record.getTime() != null && window.contains(record.getTime());
        }
        catch (JsonParseException | IllegalArgumentException ex)
        {
            return false;
        }
    }
}
