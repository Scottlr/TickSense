package com.ticksense.ui;

import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.ReportSummary;
import com.ticksense.analytics.TrendAnalyzer;
import com.ticksense.runelite.ObservedId;
import com.ticksense.runelite.TickSenseServices;
import com.ticksense.runelite.TickSenseConfig;
import com.ticksense.storage.DeleteAllDataService;
import com.ticksense.storage.ExportBundleWriter;
import com.ticksense.storage.ReportIndexMaintenanceService;
import com.ticksense.storage.ReportRepository;
import com.ticksense.storage.RetentionPolicy;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class TickSensePanel extends PluginPanel
{
    private static final String CONFIG_GROUP = "ticksense";
    private static final int RECENT_REPORT_LIMIT = 20;
    private static final double NORMAL_CONFIDENCE_THRESHOLD = 0.75D;
    private static final RetentionPolicy DEFAULT_RETENTION_POLICY = new RetentionPolicy(30, 100, false);

    private final TickSenseServices services;
    private final ReportRepository reportRepository;
    private final DeleteAllDataService deleteAllDataService;
    private final ExportBundleWriter exportBundleWriter;
    private final ReportIndexMaintenanceService reportIndexMaintenanceService;
    private final TrendAnalyzer trendAnalyzer;
    private final ConfigManager configManager;
    private final TickSenseConfig config;
    private final ReportListPanel reportListPanel;
    private final ActivityReportPanel activityReportPanel;
    private final CurrentActivityPanel currentActivityPanel;
    private final DeveloperDiagnosticsPanel developerDiagnosticsPanel;
    private final TrendsPanel trendsPanel;
    private final JTabbedPane tabs;
    private final Component developerDiagnosticsTab;
    private final Timer currentActivityRefreshTimer;
    private ReportSummary selectedSummary;
    private List<ReportSummary> lastLowConfidenceReports = new ArrayList<>();

    public TickSensePanel(
        ReportRepository reportRepository,
        DeleteAllDataService deleteAllDataService,
        ConfigManager configManager,
        TickSenseConfig config)
    {
        this(null, reportRepository, deleteAllDataService, null, null, new TrendAnalyzer(), configManager, config);
    }

    public TickSensePanel(
        TickSenseServices services,
        ReportRepository reportRepository,
        DeleteAllDataService deleteAllDataService,
        ExportBundleWriter exportBundleWriter,
        ReportIndexMaintenanceService reportIndexMaintenanceService,
        TrendAnalyzer trendAnalyzer,
        ConfigManager configManager,
        TickSenseConfig config)
    {
        super(false);
        this.services = services;
        this.reportRepository = reportRepository;
        this.deleteAllDataService = deleteAllDataService;
        this.exportBundleWriter = exportBundleWriter;
        this.reportIndexMaintenanceService = reportIndexMaintenanceService;
        this.trendAnalyzer = trendAnalyzer;
        this.configManager = configManager;
        this.config = config;

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        final JLabel title = new JLabel("TickSense");
        title.setForeground(ColorScheme.BRAND_ORANGE);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        reportListPanel = new ReportListPanel();
        reportListPanel.onSelectionChanged(this::loadReport);
        activityReportPanel = new ActivityReportPanel();
        currentActivityPanel = new CurrentActivityPanel(this::activeSession, this::activityDiagnostics);
        developerDiagnosticsPanel = new DeveloperDiagnosticsPanel(this::buildDeveloperDiagnosticsText, this::exportSelectedBundle);
        developerDiagnosticsPanel.setExportEnabled(false);
        trendsPanel = new TrendsPanel();

        tabs = new JTabbedPane();
        tabs.addTab("Recent Reports", createRecentReportsTab());
        tabs.addTab("Trends", trendsPanel);
        tabs.addTab("Settings", createSettingsTab());
        developerDiagnosticsTab = developerDiagnosticsPanel;
        setDiagnosticsTabEnabled(config.debugActivityDiagnostics());
        add(tabs, BorderLayout.CENTER);

        currentActivityRefreshTimer = new Timer(1000, event -> currentActivityPanel.refresh());
        currentActivityRefreshTimer.setRepeats(true);
    }

    public void initialize()
    {
        refreshReports();
        currentActivityPanel.refresh();
        currentActivityRefreshTimer.start();
        if (reportRepository instanceof NotifyingReportRepository)
        {
            ((NotifyingReportRepository) reportRepository).addSaveListener(() -> SwingUtilities.invokeLater(this::refreshReports));
        }
    }

    public void shutdown()
    {
        currentActivityRefreshTimer.stop();
    }

    public void refreshReports()
    {
        try
        {
            final List<ReportSummary> reports = reportRepository.listRecent(RECENT_REPORT_LIMIT);
            final List<ReportSummary> normalReports = new ArrayList<>();
            final List<ReportSummary> lowConfidenceReports = new ArrayList<>();
            for (ReportSummary report : reports)
            {
                if (report.getConfidence() >= NORMAL_CONFIDENCE_THRESHOLD)
                {
                    normalReports.add(report);
                }
                else
                {
                    lowConfidenceReports.add(report);
                }
            }

            reportListPanel.setEmptyState("No completed reports yet.");
            reportListPanel.setReports(normalReports);
            lastLowConfidenceReports = lowConfidenceReports;
            trendsPanel.setTrendSummary(trendAnalyzer.summarize(reports));
            if (normalReports.isEmpty())
            {
                selectedSummary = null;
                activityReportPanel.showReport(null);
            }
            developerDiagnosticsPanel.setExportEnabled(selectedSummary != null);
            developerDiagnosticsPanel.refresh();
            setDiagnosticsTabEnabled(config.debugActivityDiagnostics());
        }
        catch (IOException ex)
        {
            reportListPanel.setEmptyState("Report storage is unavailable.");
            reportListPanel.setReports(null);
            selectedSummary = null;
            activityReportPanel.showReport(null);
            lastLowConfidenceReports = new ArrayList<>();
            trendsPanel.setTrendSummary(null);
            developerDiagnosticsPanel.refresh();
        }
    }

    public ReportListPanel getReportListPanel()
    {
        return reportListPanel;
    }

    private JPanel createRecentReportsTab()
    {
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, reportListPanel, activityReportPanel);
        splitPane.setResizeWeight(0.3D);
        splitPane.setBorder(null);

        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        panel.add(currentActivityPanel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSettingsTab()
    {
        final JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        settingsPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        final JCheckBox debugRecorder = createCheckBox(
            "Debug event recorder",
            config.debugEventRecorder(),
            selected -> configManager.setConfiguration(CONFIG_GROUP, "debugEventRecorder", selected));
        settingsPanel.add(debugRecorder);

        final JCheckBox debugDiagnostics = createCheckBox(
            "Activity diagnostics",
            config.debugActivityDiagnostics(),
            selected -> {
                configManager.setConfiguration(CONFIG_GROUP, "debugActivityDiagnostics", selected);
                setDiagnosticsTabEnabled(selected);
            });
        settingsPanel.add(debugDiagnostics);

        final JButton deleteAllDataButton = new JButton("Delete local data");
        deleteAllDataButton.addActionListener(event -> deleteAllData());
        settingsPanel.add(deleteAllDataButton);

        final JButton refreshButton = new JButton("Refresh reports");
        refreshButton.addActionListener(event -> refreshReports());
        settingsPanel.add(refreshButton);

        final JButton rebuildIndexButton = new JButton("Rebuild report index");
        rebuildIndexButton.addActionListener(event -> rebuildReportIndex());
        settingsPanel.add(rebuildIndexButton);

        final JButton applyRetentionButton = new JButton("Apply retention");
        applyRetentionButton.addActionListener(event -> applyRetention());
        settingsPanel.add(applyRetentionButton);

        final JLabel note = new JLabel("Settings update local TickSense behavior only.");
        note.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        note.setBorder(new EmptyBorder(8, 0, 0, 0));
        settingsPanel.add(note);
        return settingsPanel;
    }

    private JCheckBox createCheckBox(String label, boolean selected, java.util.function.Consumer<Boolean> onChange)
    {
        final JCheckBox checkBox = new JCheckBox(label, selected);
        checkBox.setOpaque(false);
        checkBox.setForeground(ColorScheme.TEXT_COLOR);
        checkBox.setBorder(new EmptyBorder(0, 0, 8, 0));
        checkBox.addActionListener(event -> onChange.accept(checkBox.isSelected()));
        return checkBox;
    }

    private void loadReport(ReportSummary summary)
    {
        selectedSummary = summary;
        developerDiagnosticsPanel.setExportEnabled(summary != null);
        if (summary == null)
        {
            activityReportPanel.showReport(null);
            return;
        }

        try
        {
            final Optional<ActivityReport> report = reportRepository.findById(summary.getReportId());
            activityReportPanel.showReport(report.orElse(null));
        }
        catch (IOException ex)
        {
            activityReportPanel.showReport(null);
        }
    }

    private String buildDeveloperDiagnosticsText()
    {
        final StringBuilder builder = new StringBuilder();
        if (services == null)
        {
            builder.append("Developer diagnostics are unavailable without live TickSense services.");
            return builder.toString();
        }
        appendActiveStrategySection(builder);
        appendCandidateStrategiesSection(builder);
        appendRegionSection(builder);
        appendOpenOpportunitiesSection(builder);
        appendLastFinishReasonSection(builder);
        appendUnknownIdsSection(builder);
        appendRecentEventsSection(builder);
        appendLowConfidenceReportsSection(builder);
        return builder.toString().trim();
    }

    private void appendActiveStrategySection(StringBuilder builder)
    {
        builder.append("Active strategy\n");
        builder.append("----------------\n");
        final Optional<com.ticksense.core.ActivitySession> activeSession = services.getStrategyEngine().getActiveSession();
        if (!activeSession.isPresent())
        {
            builder.append("No active strategy.\n\n");
            return;
        }

        final com.ticksense.core.ActivitySession session = activeSession.get();
        builder.append(session.getActivityType().name())
            .append(" / ")
            .append(session.getActivityId().getValue())
            .append("\n\n");
    }

    private void appendCandidateStrategiesSection(StringBuilder builder)
    {
        builder.append("Candidate strategies / confidence\n");
        builder.append("-------------------------------\n");
        final List<com.ticksense.activities.ActivityDiagnostic> diagnostics = services.getStrategyEngine().getDiagnostics();
        if (diagnostics.isEmpty())
        {
            builder.append("No activity diagnostics captured.\n\n");
            return;
        }

        for (com.ticksense.activities.ActivityDiagnostic diagnostic : diagnostics)
        {
            builder.append(diagnostic.getActivityType().name())
                .append(" ")
                .append(String.format("%.2f", diagnostic.getConfidence()))
                .append(" ")
                .append(diagnostic.getDecision());
            if (!diagnostic.getReason().isEmpty())
            {
                builder.append(" - ").append(diagnostic.getReason());
            }
            builder.append('\n');
        }
        builder.append('\n');
    }

    private void appendRegionSection(StringBuilder builder)
    {
        builder.append("Current region / world view\n");
        builder.append("---------------------------\n");
        final Optional<RegionInstanceTelemetryEvent> regionEvent = services.getLastRegionEvent();
        if (!regionEvent.isPresent())
        {
            builder.append("No region metadata captured yet.\n\n");
            return;
        }

        final RegionInstanceTelemetryEvent event = regionEvent.get();
        builder.append("World ").append(event.getWorld())
            .append(", region ").append(event.getRegionId())
            .append(", view ").append(event.getWorldViewId())
            .append(", state ").append(event.getGameState())
            .append('\n')
            .append('\n');
    }

    private void appendOpenOpportunitiesSection(StringBuilder builder)
    {
        builder.append("Open opportunities\n");
        builder.append("------------------\n");
        final List<com.ticksense.activities.OpportunityMarker> openOpportunities = services.getOpenOpportunityMarkers();
        if (openOpportunities.isEmpty())
        {
            builder.append("No open opportunities.\n\n");
            return;
        }

        for (com.ticksense.activities.OpportunityMarker marker : openOpportunities)
        {
            builder.append(marker.getOpportunityType())
                .append(" / ")
                .append(marker.getStatus().name())
                .append('\n');
        }
        builder.append('\n');
    }

    private void appendLastFinishReasonSection(StringBuilder builder)
    {
        builder.append("Last finish reason\n");
        builder.append("------------------\n");
        final Optional<com.ticksense.core.FinishReason> finishReason = services.getLastFinishReason();
        if (!finishReason.isPresent())
        {
            builder.append("No finished activity yet.\n\n");
            return;
        }

        final com.ticksense.core.FinishReason reason = finishReason.get();
        builder.append(reason.getType().name())
            .append(" - ")
            .append(reason.getExplanation())
            .append('\n')
            .append('\n');
    }

    private void appendUnknownIdsSection(StringBuilder builder)
    {
        builder.append("Observed IDs\n");
        builder.append("------------\n");
        final List<ObservedId> observedIds = services.getObservedIds();
        if (observedIds.isEmpty())
        {
            builder.append("No IDs captured yet.\n\n");
            return;
        }

        for (ObservedId observedId : observedIds)
        {
            builder.append(observedId.getKind())
                .append(' ')
                .append(observedId.getId())
                .append(" x")
                .append(observedId.getCount())
                .append(" @ tick ")
                .append(observedId.getLastSeenTick())
                .append(" via ")
                .append(observedId.getSourceEventType())
                .append('\n');
        }
        builder.append('\n');
    }

    private void appendRecentEventsSection(StringBuilder builder)
    {
        builder.append("Last 50 normalized events\n");
        builder.append("-------------------------\n");
        final List<TelemetryEnvelope> recentTelemetry = services.getRecentTelemetry();
        if (recentTelemetry.isEmpty())
        {
            builder.append("No normalized events captured yet.\n\n");
            return;
        }

        for (TelemetryEnvelope envelope : recentTelemetry)
        {
            builder.append(envelope.getEvent().getType())
                .append(" @ tick ")
                .append(envelope.getEvent().getTime().getGameTick())
                .append('\n');
        }
        builder.append('\n');
    }

    private void appendLowConfidenceReportsSection(StringBuilder builder)
    {
        builder.append("Hidden low-confidence reports\n");
        builder.append("---------------------------\n");
        if (lastLowConfidenceReports.isEmpty())
        {
            builder.append("No low-confidence reports are currently hidden from the normal recent reports list.");
            return;
        }

        for (ReportSummary report : lastLowConfidenceReports)
        {
            builder.append(ReportTextFormatter.formatSummaryLine(report))
                .append('\n')
                .append("Evidence: ")
                .append(report.getEvidenceSummaryText().isEmpty() ? "Unknown" : report.getEvidenceSummaryText())
                .append("\n\n");
        }
    }

    private void setDiagnosticsTabEnabled(boolean enabled)
    {
        final int index = tabs.indexOfComponent(developerDiagnosticsTab);
        if (enabled && index < 0)
        {
            tabs.addTab("Developer Diagnostics", developerDiagnosticsTab);
        }
        else if (!enabled && index >= 0)
        {
            tabs.remove(index);
        }
    }

    private void exportSelectedBundle()
    {
        if (exportBundleWriter == null)
        {
            showInfoMessage("Export debug bundle", "Debug bundle export is unavailable in this context.");
            return;
        }
        if (selectedSummary == null)
        {
            showInfoMessage("Export debug bundle", "Select a completed report before exporting a debug bundle.");
            return;
        }

        try
        {
            final java.nio.file.Path bundlePath = exportBundleWriter.writeBundle(
                selectedSummary.getReportId(),
                exportBundleWriter.defaultExportDirectory());
            showInfoMessage("Export debug bundle", "Exported debug bundle to " + bundlePath);
        }
        catch (IOException ex)
        {
            showErrorMessage("Export debug bundle", "TickSense could not export a debug bundle for the selected report.");
        }
    }

    private void rebuildReportIndex()
    {
        if (reportIndexMaintenanceService == null)
        {
            showInfoMessage("Rebuild report index", "Report index maintenance is unavailable in this context.");
            return;
        }
        try
        {
            reportIndexMaintenanceService.rebuildIndex();
            refreshReports();
        }
        catch (IOException ex)
        {
            showErrorMessage("Rebuild report index", "TickSense could not rebuild the report index right now.");
        }
    }

    private void applyRetention()
    {
        if (reportIndexMaintenanceService == null)
        {
            showInfoMessage("Apply retention", "Retention maintenance is unavailable in this context.");
            return;
        }
        try
        {
            reportIndexMaintenanceService.applyRetention(DEFAULT_RETENTION_POLICY);
            refreshReports();
        }
        catch (IOException ex)
        {
            showErrorMessage("Apply retention", "TickSense could not apply retention right now.");
        }
    }

    private void deleteAllData()
    {
        final int choice = JOptionPane.showConfirmDialog(
            this,
            "Delete all local TickSense reports and timelines?",
            "Delete local data",
            JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION)
        {
            return;
        }

        try
        {
            deleteAllDataService.deleteAll();
            refreshReports();
        }
        catch (IOException ex)
        {
            showErrorMessage("Delete local data", "TickSense could not delete local data right now.");
        }
    }

    private void showInfoMessage(String title, String message)
    {
        showMessage(title, message, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String title, String message)
    {
        showMessage(title, message, JOptionPane.ERROR_MESSAGE);
    }

    private Optional<com.ticksense.core.ActivitySession> activeSession()
    {
        if (services == null)
        {
            return Optional.empty();
        }
        return services.getStrategyEngine().getActiveSession();
    }

    private List<com.ticksense.activities.ActivityDiagnostic> activityDiagnostics()
    {
        if (services == null)
        {
            return java.util.Collections.emptyList();
        }
        return services.getStrategyEngine().getDiagnostics();
    }

    private void showMessage(String title, String message, int messageType)
    {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
}
