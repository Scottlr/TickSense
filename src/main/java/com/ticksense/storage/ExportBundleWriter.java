package com.ticksense.storage;

import com.google.gson.Gson;
import com.ticksense.activities.ActivityDiagnostic;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.common.TextValues;
import com.ticksense.storage.debug.DebugEventLogRepository;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ExportBundleWriter
{
    private static final int BUNDLE_SCHEMA_VERSION = 1;

    private final TickSenseDataPaths dataPaths;
    private final ReportRepository reportRepository;
    private final Gson gson;
    private final ExportConfigSnapshotProvider configSnapshotProvider;
    private final Supplier<List<ActivityDiagnostic>> diagnosticsSupplier;
    private final Clock clock;

    @Inject
    public ExportBundleWriter(
        ReportRepository reportRepository,
        ExportConfigSnapshotProvider configSnapshotProvider,
        Supplier<List<ActivityDiagnostic>> diagnosticsSupplier)
    {
        this(TickSenseDataPaths.defaultPaths(), reportRepository, new Gson(), configSnapshotProvider, diagnosticsSupplier, Clock.systemUTC());
    }

    public ExportBundleWriter(
        TickSenseDataPaths dataPaths,
        ReportRepository reportRepository,
        Gson gson,
        ExportConfigSnapshotProvider configSnapshotProvider,
        Supplier<List<ActivityDiagnostic>> diagnosticsSupplier,
        Clock clock)
    {
        this.dataPaths = Objects.requireNonNull(dataPaths, "dataPaths");
        this.reportRepository = Objects.requireNonNull(reportRepository, "reportRepository");
        this.gson = Objects.requireNonNull(gson, "gson");
        this.configSnapshotProvider = Objects.requireNonNull(configSnapshotProvider, "configSnapshotProvider");
        this.diagnosticsSupplier = Objects.requireNonNull(diagnosticsSupplier, "diagnosticsSupplier");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public Path writeBundle(String reportId, Path destinationDirectory) throws IOException
    {
        final String normalizedReportId = TextValues.requireText(reportId, "reportId");
        final Path normalizedDestinationDirectory = Objects.requireNonNull(destinationDirectory, "destinationDirectory");
        final ActivityReport report = findReport(normalizedReportId);
        final List<String> timelineLines = JsonlTimelineRepository.readActivityRecordLines(dataPaths, report.getActivityId(), gson);
        if (timelineLines.isEmpty())
        {
            throw new IOException("No timeline data found for activity " + report.getActivityId().getValue());
        }
        final ActivityTimelineWindow activityWindow = JsonlTimelineRepository.readActivityWindow(dataPaths, report.getActivityId(), gson);
        final List<String> debugLines = new DebugEventLogRepository(dataPaths, gson).readLines(activityWindow);

        Files.createDirectories(normalizedDestinationDirectory);
        final Path zipPath = normalizedDestinationDirectory.resolve("ticksense-bundle-" + normalizedReportId + ".zip");
        Files.deleteIfExists(zipPath);

        try (OutputStream outputStream = Files.newOutputStream(zipPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8))
        {
            addJsonEntry(zipOutputStream, "bundle.json", bundleMetadata(report));
            addJsonEntry(zipOutputStream, "plugin-config.json", pluginConfigSnapshot());
            addJsonEntry(zipOutputStream, "report.json", report);
            addJsonEntry(zipOutputStream, "activity.json", activitySnapshot(report));
            addLinesEntry(zipOutputStream, "timeline.jsonl", timelineLines);
            addLinesEntry(zipOutputStream, "diagnostics.jsonl", diagnosticsLines());
            addLinesEntry(zipOutputStream, "debug-events.jsonl", debugLines);
        }
        catch (IOException ex)
        {
            Files.deleteIfExists(zipPath);
            throw ex;
        }

        return zipPath;
    }

    public Path defaultExportDirectory()
    {
        return dataPaths.getExportsDirectory();
    }

    private ActivityReport findReport(String reportId) throws IOException
    {
        final Optional<ActivityReport> report = reportRepository.findById(reportId);
        if (!report.isPresent())
        {
            throw new IOException("No report found for " + reportId);
        }
        return report.get();
    }

    private Map<String, Object> bundleMetadata(ActivityReport report)
    {
        final Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("bundleSchemaVersion", BUNDLE_SCHEMA_VERSION);
        metadata.put("createdAtMillis", clock.millis());
        metadata.put("reportId", report.getReportId());
        metadata.put("activityId", report.getActivityId().getValue());
        metadata.put("activityType", report.getActivityType().name());
        return metadata;
    }

    private Map<String, Object> pluginConfigSnapshot()
    {
        return new LinkedHashMap<>(Objects.requireNonNull(configSnapshotProvider.snapshot(), "configSnapshot"));
    }

    private Map<String, Object> activitySnapshot(ActivityReport report)
    {
        final Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("activityId", report.getActivityId().getValue());
        snapshot.put("activityType", report.getActivityType().name());
        snapshot.put("detectedActivityName", report.getDetectedActivityName());
        snapshot.put("finishReason", report.getFinishReason().getType().name());
        snapshot.put("confidence", report.getConfidence());
        snapshot.put("evidenceSummary", report.getEvidenceSummary());
        snapshot.put("summaryLines", report.getSummaryLines());
        return snapshot;
    }

    private List<String> diagnosticsLines()
    {
        final List<ActivityDiagnostic> diagnostics = diagnosticsSupplier.get();
        if (diagnostics == null || diagnostics.isEmpty())
        {
            return Collections.emptyList();
        }

        final java.util.ArrayList<String> lines = new java.util.ArrayList<>(diagnostics.size());
        for (ActivityDiagnostic diagnostic : diagnostics)
        {
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", "activity.diagnostic");
            payload.put("activityType", diagnostic.getActivityType().name());
            payload.put("confidence", diagnostic.getConfidence());
            payload.put("decision", diagnostic.getDecision());
            payload.put("reason", diagnostic.getReason());
            payload.put("time", diagnostic.getTime());
            payload.put("evidence", diagnostic.getEvidence());
            lines.add(gson.toJson(payload));
        }
        return lines;
    }

    private void addJsonEntry(ZipOutputStream zipOutputStream, String name, Object value) throws IOException
    {
        addLinesEntry(zipOutputStream, name, Collections.singletonList(gson.toJson(value)));
    }

    private void addLinesEntry(ZipOutputStream zipOutputStream, String name, List<String> lines) throws IOException
    {
        zipOutputStream.putNextEntry(new ZipEntry(name));
        for (String line : lines)
        {
            zipOutputStream.write(line.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.write('\n');
        }
        zipOutputStream.closeEntry();
    }
}
