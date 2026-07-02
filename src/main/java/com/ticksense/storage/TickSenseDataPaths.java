package com.ticksense.storage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class TickSenseDataPaths
{
    private final Path tickSenseRoot;
    private final Path timelinesDirectory;
    private final Path schemaVersionFile;

    public TickSenseDataPaths(Path tickSenseRoot)
    {
        this.tickSenseRoot = Objects.requireNonNull(tickSenseRoot, "tickSenseRoot");
        this.timelinesDirectory = tickSenseRoot.resolve("timelines");
        this.schemaVersionFile = tickSenseRoot.resolve("schema-version.json");
    }

    public static TickSenseDataPaths defaultPaths()
    {
        return new TickSenseDataPaths(Paths.get(System.getProperty("user.home"), ".runelite", "ticksense"));
    }

    public Path getTickSenseRoot()
    {
        return tickSenseRoot;
    }

    public Path getTimelinesDirectory()
    {
        return timelinesDirectory;
    }

    public Path getSchemaVersionFile()
    {
        return schemaVersionFile;
    }
}
