package com.ticksense.storage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class TickSenseDataPaths
{
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String RUNE_LITE_DIRECTORY = ".runelite";
    private static final String DATA_DIRECTORY = "ticksense";
    private static final String TIMELINES_DIRECTORY = "timelines";
    private static final String REPORTS_DIRECTORY = "reports";
    private static final String INDEXES_DIRECTORY = "indexes";
    private static final String EXPORTS_DIRECTORY = "exports";
    private static final String SCHEMA_VERSION_FILE = "schema-version.json";
    private static final String REPORT_INDEX_FILE = "report-index.json";

    private final Path tickSenseRoot;
    private final Path timelinesDirectory;
    private final Path reportsDirectory;
    private final Path indexesDirectory;
    private final Path exportsDirectory;
    private final Path schemaVersionFile;
    private final Path reportIndexFile;

    public TickSenseDataPaths(Path tickSenseRoot)
    {
        this.tickSenseRoot = Objects.requireNonNull(tickSenseRoot, "tickSenseRoot");
        this.timelinesDirectory = tickSenseRoot.resolve(TIMELINES_DIRECTORY);
        this.reportsDirectory = tickSenseRoot.resolve(REPORTS_DIRECTORY);
        this.indexesDirectory = tickSenseRoot.resolve(INDEXES_DIRECTORY);
        this.exportsDirectory = tickSenseRoot.resolve(EXPORTS_DIRECTORY);
        this.schemaVersionFile = tickSenseRoot.resolve(SCHEMA_VERSION_FILE);
        this.reportIndexFile = indexesDirectory.resolve(REPORT_INDEX_FILE);
    }

    public static TickSenseDataPaths defaultPaths()
    {
        return new TickSenseDataPaths(Paths.get(USER_HOME, RUNE_LITE_DIRECTORY, DATA_DIRECTORY));
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

    public Path getExportsDirectory()
    {
        return exportsDirectory;
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
