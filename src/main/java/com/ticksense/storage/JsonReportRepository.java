package com.ticksense.storage;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.MetricDefinition;
import com.ticksense.analytics.MetricUnit;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.OpportunityTimelineEntry;
import com.ticksense.analytics.ReportSummary;
import com.ticksense.analytics.TickLossBreakdown;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import com.ticksense.core.FinishReason;
import com.ticksense.core.FinishReasonType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JsonReportRepository implements ReportRepository
{
    private static final String REPORT_EXTENSION = ".json";

    private final TickSenseDataPaths dataPaths;
    private final Gson gson;

    public JsonReportRepository()
    {
        this(TickSenseDataPaths.defaultPaths(), new Gson());
    }

    public JsonReportRepository(TickSenseDataPaths dataPaths, Gson gson)
    {
        this.dataPaths = Objects.requireNonNull(dataPaths, "dataPaths");
        this.gson = Objects.requireNonNull(gson, "gson");
    }

    @Override
    public synchronized void save(ActivityReport report) throws IOException
    {
        final ActivityReport normalizedReport = Objects.requireNonNull(report, "report");
        ensureDirectories();
        writeJsonAtomically(reportPath(normalizedReport.getReportId()), gson.toJson(PersistedActivityReport.from(normalizedReport)));
        writeIndex(rebuildIndexFromReports());
    }

    @Override
    public synchronized Optional<ActivityReport> findById(String reportId) throws IOException
    {
        final Path path = reportPath(StorageTexts.requireText(reportId, "reportId"));
        if (Files.notExists(path))
        {
            return Optional.empty();
        }
        return Optional.of(readReport(path));
    }

    @Override
    public synchronized List<ReportSummary> listRecent(int limit) throws IOException
    {
        if (limit < 0)
        {
            throw new IllegalArgumentException("limit must not be negative");
        }
        if (limit == 0)
        {
            return Collections.emptyList();
        }

        final List<ReportSummary> reports = loadIndex();
        return StorageCollections.immutableHead(reports, limit);
    }

    public synchronized List<ReportSummary> rebuildIndex() throws IOException
    {
        final List<ReportSummary> rebuilt = rebuildIndexFromReports();
        writeIndex(rebuilt);
        return rebuilt;
    }

    private List<ReportSummary> loadIndex() throws IOException
    {
        ensureDirectories();
        final Path indexPath = dataPaths.getReportIndexFile();
        if (Files.exists(indexPath))
        {
            try
            {
                return readIndex(indexPath);
            }
            catch (IOException | IllegalArgumentException | JsonParseException ex)
            {
                // Fall back to rebuilding from full reports.
            }
        }

        final List<ReportSummary> rebuilt = rebuildIndexFromReports();
        writeIndex(rebuilt);
        return rebuilt;
    }

    private List<ReportSummary> rebuildIndexFromReports() throws IOException
    {
        ensureDirectories();
        try (Stream<Path> fileStream = Files.list(dataPaths.getReportsDirectory()))
        {
            final List<Path> reportFiles = fileStream
                .filter(path -> path.getFileName().toString().endsWith(REPORT_EXTENSION))
                .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                .collect(Collectors.toList());

            final List<ReportSummary> summaries = new ArrayList<>();
            for (Path reportFile : reportFiles)
            {
                try
                {
                    summaries.add(readReport(reportFile).toSummary());
                }
                catch (IOException | IllegalArgumentException | JsonParseException ex)
                {
                    // Ignore corrupt or partial report files while rebuilding the index.
                }
            }

            summaries.sort(Comparator
                .comparingLong(ReportSummary::getCreatedAtMillis).reversed()
                .thenComparing(ReportSummary::getReportId));
            return StorageCollections.immutableList(summaries);
        }
    }

    private List<ReportSummary> readIndex(Path indexPath) throws IOException
    {
        final PersistedReportIndex persisted = gson.fromJson(readUtf8(indexPath), PersistedReportIndex.class);
        if (persisted == null)
        {
            throw new IOException("Report index must not be null");
        }
        if (persisted.schemaVersion != ActivityReport.SCHEMA_VERSION)
        {
            throw new IllegalArgumentException("Unsupported report index schema version: " + persisted.schemaVersion);
        }

        final List<ReportSummary> summaries = new ArrayList<>();
        if (persisted.reports != null)
        {
            for (PersistedReportSummary summary : persisted.reports)
            {
                summaries.add(Objects.requireNonNull(summary, "report summary").toReportSummary());
            }
        }
        return StorageCollections.immutableList(summaries);
    }

    private ActivityReport readReport(Path path) throws IOException
    {
        try
        {
            final PersistedActivityReport persisted = gson.fromJson(readUtf8(path), PersistedActivityReport.class);
            if (persisted == null)
            {
                throw new IOException("Report must not be null: " + path);
            }
            return persisted.toActivityReport();
        }
        catch (JsonParseException | IllegalArgumentException ex)
        {
            throw new IOException("Failed to read report: " + path, ex);
        }
    }

    private void writeIndex(List<ReportSummary> summaries) throws IOException
    {
        final List<PersistedReportSummary> persistedSummaries = new ArrayList<>();
        for (ReportSummary summary : summaries)
        {
            persistedSummaries.add(PersistedReportSummary.from(summary));
        }
        writeJsonAtomically(
            dataPaths.getReportIndexFile(),
            gson.toJson(new PersistedReportIndex(ActivityReport.SCHEMA_VERSION, persistedSummaries)));
    }

    private void writeJsonAtomically(Path path, String json) throws IOException
    {
        ensureDirectories();
        final Path parent = Objects.requireNonNull(path.getParent(), "parent");
        Files.createDirectories(parent);
        final Path tempFile = parent.resolve(path.getFileName().toString() + ".tmp");
        Files.write(tempFile, Collections.singletonList(json), StandardCharsets.UTF_8);
        try
        {
            Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (AtomicMoveNotSupportedException ex)
        {
            Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Path reportPath(String reportId)
    {
        return dataPaths.getReportsDirectory().resolve(reportId + REPORT_EXTENSION);
    }

    private void ensureDirectories() throws IOException
    {
        Files.createDirectories(dataPaths.getTickSenseRoot());
        Files.createDirectories(dataPaths.getReportsDirectory());
        Files.createDirectories(dataPaths.getIndexesDirectory());
    }

    private static String readUtf8(Path path) throws IOException
    {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
    private static final class PersistedReportIndex
    {
        private final int schemaVersion;
        private final List<PersistedReportSummary> reports;

        private PersistedReportIndex(int schemaVersion, List<PersistedReportSummary> reports)
        {
            this.schemaVersion = schemaVersion;
            this.reports = reports;
        }
    }

    private static final class PersistedReportSummary
    {
        private int schemaVersion;
        private String reportId;
        private String activityId;
        private String activityType;
        private String detectedActivityName;
        private long createdAtMillis;
        private int durationTicks;
        private long durationMillis;
        private String finishReason;
        private double confidence;
        private String evidenceSummaryText;
        private List<String> summaryLines;
        private Map<String, Double> metricValues;
        private Map<String, Integer> tickLossCategories;

        private static PersistedReportSummary from(ReportSummary summary)
        {
            final PersistedReportSummary persisted = new PersistedReportSummary();
            persisted.schemaVersion = summary.getSchemaVersion();
            persisted.reportId = summary.getReportId();
            persisted.activityId = summary.getActivityId().getValue();
            persisted.activityType = summary.getActivityType().name();
            persisted.detectedActivityName = summary.getDetectedActivityName();
            persisted.createdAtMillis = summary.getCreatedAtMillis();
            persisted.durationTicks = summary.getDurationTicks();
            persisted.durationMillis = summary.getDurationMillis();
            persisted.finishReason = summary.getFinishReason();
            persisted.confidence = summary.getConfidence();
            persisted.evidenceSummaryText = summary.getEvidenceSummaryText();
            persisted.summaryLines = summary.getSummaryLines();
            persisted.metricValues = summary.getMetricValues();
            persisted.tickLossCategories = summary.getTickLossCategories();
            return persisted;
        }

        private ReportSummary toReportSummary()
        {
            return new ReportSummary(
                schemaVersion,
                reportId,
                ActivityId.of(activityId),
                ActivityType.valueOf(activityType),
                detectedActivityName,
                createdAtMillis,
                durationTicks,
                durationMillis,
                finishReason,
                confidence,
                evidenceSummaryText,
                summaryLines,
                metricValues,
                tickLossCategories);
        }
    }

    private static final class PersistedActivityReport
    {
        private int schemaVersion;
        private String reportId;
        private String activityId;
        private String activityType;
        private String detectedActivityName;
        private long createdAtMillis;
        private int durationTicks;
        private long durationMillis;
        private PersistedFinishReason finishReason;
        private double confidence;
        private List<String> evidenceSummary;
        private Map<String, PersistedMetricValue> metrics;
        private List<PersistedOpportunityTimelineEntry> opportunities;
        private PersistedTickLossBreakdown tickLossBreakdown;
        private List<String> summaryLines;

        private static PersistedActivityReport from(ActivityReport report)
        {
            final PersistedActivityReport persisted = new PersistedActivityReport();
            persisted.schemaVersion = report.getSchemaVersion();
            persisted.reportId = report.getReportId();
            persisted.activityId = report.getActivityId().getValue();
            persisted.activityType = report.getActivityType().name();
            persisted.detectedActivityName = report.getDetectedActivityName();
            persisted.createdAtMillis = report.getCreatedAtMillis();
            persisted.durationTicks = report.getDurationTicks();
            persisted.durationMillis = report.getDurationMillis();
            persisted.finishReason = PersistedFinishReason.from(report.getFinishReason());
            persisted.confidence = report.getConfidence();
            persisted.evidenceSummary = report.getEvidenceSummary();
            persisted.metrics = new LinkedHashMap<>();
            for (Map.Entry<String, MetricValue> entry : report.getMetrics().entrySet())
            {
                persisted.metrics.put(entry.getKey(), PersistedMetricValue.from(entry.getValue()));
            }
            persisted.opportunities = new ArrayList<>();
            for (OpportunityTimelineEntry entry : report.getOpportunities())
            {
                persisted.opportunities.add(PersistedOpportunityTimelineEntry.from(entry));
            }
            persisted.tickLossBreakdown = PersistedTickLossBreakdown.from(report.getTickLossBreakdown());
            persisted.summaryLines = report.getSummaryLines();
            return persisted;
        }

        private ActivityReport toActivityReport()
        {
            if (schemaVersion != ActivityReport.SCHEMA_VERSION)
            {
                throw new IllegalArgumentException("Unsupported report schema version: " + schemaVersion);
            }

            final Map<String, MetricValue> reportMetrics = new LinkedHashMap<>();
            if (metrics != null)
            {
                for (Map.Entry<String, PersistedMetricValue> entry : metrics.entrySet())
                {
                    reportMetrics.put(entry.getKey(), Objects.requireNonNull(entry.getValue(), "metric").toMetricValue());
                }
            }

            final List<OpportunityTimelineEntry> reportOpportunities = new ArrayList<>();
            if (opportunities != null)
            {
                for (PersistedOpportunityTimelineEntry opportunity : opportunities)
                {
                    reportOpportunities.add(Objects.requireNonNull(opportunity, "opportunity").toOpportunityTimelineEntry());
                }
            }

            return new ActivityReport(
                schemaVersion,
                reportId,
                ActivityId.of(activityId),
                ActivityType.valueOf(activityType),
                detectedActivityName,
                createdAtMillis,
                durationTicks,
                durationMillis,
                Objects.requireNonNull(finishReason, "finishReason").toFinishReason(),
                confidence,
                evidenceSummary,
                reportMetrics,
                reportOpportunities,
                Objects.requireNonNull(tickLossBreakdown, "tickLossBreakdown").toTickLossBreakdown(),
                summaryLines);
        }
    }

    private static final class PersistedMetricValue
    {
        private PersistedMetricDefinition definition;
        private double value;

        private static PersistedMetricValue from(MetricValue metricValue)
        {
            final PersistedMetricValue persisted = new PersistedMetricValue();
            persisted.definition = PersistedMetricDefinition.from(metricValue.getDefinition());
            persisted.value = metricValue.getValue();
            return persisted;
        }

        private MetricValue toMetricValue()
        {
            return new MetricValue(Objects.requireNonNull(definition, "definition").toMetricDefinition(), value);
        }
    }

    private static final class PersistedMetricDefinition
    {
        private String key;
        private String displayName;
        private String unit;
        private String description;
        private boolean lowerValueBetter;

        private static PersistedMetricDefinition from(MetricDefinition definition)
        {
            final PersistedMetricDefinition persisted = new PersistedMetricDefinition();
            persisted.key = definition.getKey();
            persisted.displayName = definition.getDisplayName();
            persisted.unit = definition.getUnit().name();
            persisted.description = definition.getDescription();
            persisted.lowerValueBetter = definition.isLowerValueBetter();
            return persisted;
        }

        private MetricDefinition toMetricDefinition()
        {
            return new MetricDefinition(key, displayName, MetricUnit.valueOf(unit), description, lowerValueBetter);
        }
    }

    private static final class PersistedOpportunityTimelineEntry
    {
        private String opportunityType;
        private String label;
        private String status;
        private int gameTick;
        private long wallTimeMillis;
        private Integer latencyTicks;
        private Long latencyMillis;
        private List<String> evidenceSummary;

        private static PersistedOpportunityTimelineEntry from(OpportunityTimelineEntry entry)
        {
            final PersistedOpportunityTimelineEntry persisted = new PersistedOpportunityTimelineEntry();
            persisted.opportunityType = entry.getOpportunityType();
            persisted.label = entry.getLabel();
            persisted.status = entry.getStatus();
            persisted.gameTick = entry.getGameTick();
            persisted.wallTimeMillis = entry.getWallTimeMillis();
            persisted.latencyTicks = entry.getLatencyTicks();
            persisted.latencyMillis = entry.getLatencyMillis();
            persisted.evidenceSummary = entry.getEvidenceSummary();
            return persisted;
        }

        private OpportunityTimelineEntry toOpportunityTimelineEntry()
        {
            return new OpportunityTimelineEntry(
                opportunityType,
                label,
                status,
                gameTick,
                wallTimeMillis,
                latencyTicks,
                latencyMillis,
                evidenceSummary);
        }
    }

    private static final class PersistedTickLossBreakdown
    {
        private int totalTickLoss;
        private Map<String, Integer> categories;

        private static PersistedTickLossBreakdown from(TickLossBreakdown breakdown)
        {
            final PersistedTickLossBreakdown persisted = new PersistedTickLossBreakdown();
            persisted.totalTickLoss = breakdown.getTotalTickLoss();
            persisted.categories = breakdown.getCategories();
            return persisted;
        }

        private TickLossBreakdown toTickLossBreakdown()
        {
            return new TickLossBreakdown(totalTickLoss, categories);
        }
    }

    private static final class PersistedFinishReason
    {
        private String type;
        private EventTime time;
        private double confidence;
        private String explanation;
        private List<String> evidence;

        private static PersistedFinishReason from(FinishReason finishReason)
        {
            final PersistedFinishReason persisted = new PersistedFinishReason();
            persisted.type = finishReason.getType().name();
            persisted.time = finishReason.getTime();
            persisted.confidence = finishReason.getConfidence();
            persisted.explanation = finishReason.getExplanation();
            persisted.evidence = finishReason.getEvidence();
            return persisted;
        }

        private FinishReason toFinishReason()
        {
            return new FinishReason(FinishReasonType.valueOf(type), time, confidence, explanation, evidence);
        }
    }
}
