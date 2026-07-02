package com.ticksense.storage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class TickSenseDataPaths
{
    private final Path tickSenseRoot;
    private final Path timelinesDirectory;
    private final Path reportsDirectory;
    private final Path indexesDirectory;
    private final Path schemaVersionFile;
    private final Path reportIndexFile;

    public TickSenseDataPaths(Path tickSenseRoot)
    {
        this.tickSenseRoot = Objects.requireNonNull(tickSenseRoot, "tickSenseRoot");
        this.timelinesDirectory = tickSenseRoot.resolve("timelines");
        this.reportsDirectory = tickSenseRoot.resolve("reports");
        this.indexesDirectory = tickSenseRoot.resolve("indexes");
        this.schemaVersionFile = tickSenseRoot.resolve("schema-version.json");
        this.reportIndexFile = indexesDirectory.resolve("report-index.json");
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

    public Path getReportsDirectory()
    {
        return reportsDirectory;
    }

    public Path getIndexesDirectory()
    {
        return indexesDirectory;
    }

    public Path getSchemaVersionFile()
    {
        return schemaVersionFile;
    }

    public Path getReportIndexFile()
    {
        return reportIndexFile;
    }
}
