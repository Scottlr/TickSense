package com.ticksense.storage;

import com.ticksense.analytics.ReportSummary;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ReportIndexMaintenanceService
{
    private final TickSenseDataPaths dataPaths;
    private final JsonReportRepository reportRepository;
    private final Clock clock;

    public ReportIndexMaintenanceService(TickSenseDataPaths dataPaths, JsonReportRepository reportRepository, Clock clock)
    {
        this.dataPaths = Objects.requireNonNull(dataPaths, "dataPaths");
        this.reportRepository = Objects.requireNonNull(reportRepository, "reportRepository");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public List<ReportSummary> rebuildIndex() throws IOException
    {
        return reportRepository.rebuildIndex();
    }

    public RetentionResult applyRetention(RetentionPolicy retentionPolicy) throws IOException
    {
        final RetentionPolicy normalizedPolicy = Objects.requireNonNull(retentionPolicy, "retentionPolicy");
        final int deletedTimelineCount = deleteOldTimelines(normalizedPolicy);
        final int deletedReportCount = normalizedPolicy.isKeepReportsForever() ? 0 : deleteOldReports(normalizedPolicy);
        final List<ReportSummary> rebuilt = rebuildIndex();
        return new RetentionResult(deletedTimelineCount, deletedReportCount, rebuilt.size());
    }

    private int deleteOldTimelines(RetentionPolicy retentionPolicy) throws IOException
    {
        if (retentionPolicy.getMaxRawTimelineDays() <= 0 || Files.notExists(dataPaths.getTimelinesDirectory()))
        {
            return 0;
        }
        final Instant cutoff = clock.instant().minus(retentionPolicy.getMaxRawTimelineDays(), ChronoUnit.DAYS);
        int deleted = 0;
        try (Stream<Path> fileStream = Files.list(dataPaths.getTimelinesDirectory()))
        {
            for (Path timeline : fileStream.filter(path -> path.getFileName().toString().endsWith(".jsonl")).collect(Collectors.toList()))
            {
                if (Files.getLastModifiedTime(timeline).toInstant().isBefore(cutoff))
                {
                    Files.deleteIfExists(timeline);
                    deleted++;
                }
            }
        }
        return deleted;
    }

    private int deleteOldReports(RetentionPolicy retentionPolicy) throws IOException
    {
        final List<ReportSummary> summaries = reportRepository.rebuildIndex();
        if (summaries.size() <= retentionPolicy.getMaxReportCount())
        {
            return 0;
        }
        final int keepCount = retentionPolicy.getMaxReportCount();
        final List<ReportSummary> reportsToDelete = new ArrayList<>(summaries.subList(keepCount, summaries.size()));
        int deleted = 0;
        for (ReportSummary summary : reportsToDelete)
        {
            final Path reportPath = dataPaths.getReportsDirectory().resolve(summary.getReportId() + ".json");
            if (reportPath.normalize().startsWith(dataPaths.getTickSenseRoot().normalize()))
            {
                Files.deleteIfExists(reportPath);
                deleted++;
            }
        }
        return deleted;
    }

    public static final class RetentionResult
    {
        private final int deletedTimelineCount;
        private final int deletedReportCount;
        private final int remainingReportCount;

        public RetentionResult(int deletedTimelineCount, int deletedReportCount, int remainingReportCount)
        {
            this.deletedTimelineCount = deletedTimelineCount;
            this.deletedReportCount = deletedReportCount;
            this.remainingReportCount = remainingReportCount;
        }

        public int getDeletedTimelineCount()
        {
            return deletedTimelineCount;
        }

        public int getDeletedReportCount()
        {
            return deletedReportCount;
        }

        public int getRemainingReportCount()
        {
            return remainingReportCount;
        }
    }
}
