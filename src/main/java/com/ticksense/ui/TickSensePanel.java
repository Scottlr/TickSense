package com.ticksense.ui;

import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.ReportSummary;
import com.ticksense.runelite.TickSenseServices;
import com.ticksense.runelite.TickSenseConfig;
import com.ticksense.storage.DeleteAllDataService;
import com.ticksense.storage.ExportBundleWriter;
import com.ticksense.storage.ReportRepository;
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
import javax.swing.border.EmptyBorder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class TickSensePanel extends PluginPanel
{
    private static final String CONFIG_GROUP = "ticksense";
    private static final int RECENT_REPORT_LIMIT = 20;
    private static final double NORMAL_CONFIDENCE_THRESHOLD = 0.75D;

    private final TickSenseServices services;
    private final ReportRepository reportRepository;
    private final DeleteAllDataService deleteAllDataService;
    private final ExportBundleWriter exportBundleWriter;
    private final ConfigManager configManager;
    private final TickSenseConfig config;
    private final ReportListPanel reportListPanel;
    private final ActivityReportPanel activityReportPanel;
    private final DeveloperDiagnosticsPanel developerDiagnosticsPanel;
    private final JTabbedPane tabs;
    private final Component developerDiagnosticsTab;
    private ReportSummary selectedSummary;
    private List<ReportSummary> lastLowConfidenceReports = new ArrayList<>();
    private boolean diagnosticsTabVisible;

    public TickSensePanel(
        ReportRepository reportRepository,
        DeleteAllDataService deleteAllDataService,
        ConfigManager configManager,
        TickSenseConfig config)
    {
        this(null, reportRepository, deleteAllDataService, null, configManager, config);
    }

    public TickSensePanel(
        TickSenseServices services,
        ReportRepository reportRepository,
        DeleteAllDataService deleteAllDataService,
        ExportBundleWriter exportBundleWriter,
        ConfigManager configManager,
        TickSenseConfig config)
    {
        super(false);
        this.services = services;
        this.reportRepository = reportRepository;
        this.deleteAllDataService = deleteAllDataService;
        this.exportBundleWriter = exportBundleWriter;
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
        developerDiagnosticsPanel = new DeveloperDiagnosticsPanel(this::buildDeveloperDiagnosticsText, this::exportSelectedBundle);
        developerDiagnosticsPanel.setExportEnabled(false);

        tabs = new JTabbedPane();
        tabs.addTab("Recent Reports", createRecentReportsTab());
        tabs.addTab("Trends", createDisabledPlaceholder("Trends will stay hidden until local trend summaries land."));
        tabs.setEnabledAt(1, false);
        tabs.addTab("Settings", createSettingsTab());
        developerDiagnosticsTab = developerDiagnosticsPanel;
        diagnosticsTabVisible = false;
        setDiagnosticsTabEnabled(config.debugActivityDiagnostics());
        add(tabs, BorderLayout.CENTER);
    }

    public void initialize()
    {
        refreshReports();
        if (reportRepository instanceof NotifyingReportRepository)
        {
            ((NotifyingReportRepository) reportRepository).addSaveListener(() -> SwingUtilities.invokeLater(this::refreshReports));
        }
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

        final JLabel note = new JLabel("Settings update local TickSense behavior only.");
        note.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        note.setBorder(new EmptyBorder(8, 0, 0, 0));
        settingsPanel.add(note);
        return settingsPanel;
    }

    private JPanel createDisabledPlaceholder(String message)
    {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        final JLabel label = new JLabel(message);
        label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        label.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.add(label, BorderLayout.NORTH);
        return panel;
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
        builder.append("Active strategy\n");
        builder.append("----------------\n");
        if (services.getStrategyEngine().getActiveSession().isPresent())
        {
            builder.append(services.getStrategyEngine().getActiveSession().get().getActivityType().name())
                .append(" / ")
                .append(services.getStrategyEngine().getActiveSession().get().getActivityId().getValue())
                .append("\n\n");
        }
        else
        {
            builder.append("No active strategy.\n\n");
        }

        builder.append("Candidate strategies / confidence\n");
        builder.append("-------------------------------\n");
        final List<com.ticksense.activities.ActivityDiagnostic> diagnostics = services.getStrategyEngine().getDiagnostics();
        if (diagnostics.isEmpty())
        {
            builder.append("No activity diagnostics captured.\n\n");
        }
        else
        {
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

        builder.append("Current region / world view\n");
        builder.append("---------------------------\n");
        final java.util.Optional<RegionInstanceTelemetryEvent> regionEvent = services.getLastRegionEvent();
        if (regionEvent.isPresent())
        {
            builder.append("World ").append(regionEvent.get().getWorld())
                .append(", region ").append(regionEvent.get().getRegionId())
                .append(", view ").append(regionEvent.get().getWorldViewId())
                .append(", state ").append(regionEvent.get().getGameState())
                .append('\n')
                .append('\n');
        }
        else
        {
            builder.append("No region metadata captured yet.\n\n");
        }

        builder.append("Open opportunities\n");
        builder.append("------------------\n");
        if (services.getOpenOpportunityMarkers().isEmpty())
        {
            builder.append("No open opportunities.\n\n");
        }
        else
        {
            for (com.ticksense.activities.OpportunityMarker marker : services.getOpenOpportunityMarkers())
            {
                builder.append(marker.getOpportunityType())
                    .append(" / ")
                    .append(marker.getStatus().name())
                    .append('\n');
            }
            builder.append('\n');
        }

        builder.append("Last finish reason\n");
        builder.append("------------------\n");
        if (services.getLastFinishReason().isPresent())
        {
            builder.append(services.getLastFinishReason().get().getType().name())
                .append(" - ")
                .append(services.getLastFinishReason().get().getExplanation())
                .append('\n')
                .append('\n');
        }
        else
        {
            builder.append("No finished activity yet.\n\n");
        }

        builder.append("Unknown IDs\n");
        builder.append("-----------\n");
        builder.append("None captured.\n\n");

        builder.append("Last 50 normalized events\n");
        builder.append("-------------------------\n");
        final List<TelemetryEnvelope> recentTelemetry = services.getRecentTelemetry();
        if (recentTelemetry.isEmpty())
        {
            builder.append("No normalized events captured yet.\n\n");
        }
        else
        {
            for (TelemetryEnvelope envelope : recentTelemetry)
            {
                builder.append(envelope.getEvent().getType())
                    .append(" @ tick ")
                    .append(envelope.getEvent().getTime().getGameTick())
                    .append('\n');
            }
            builder.append('\n');
        }

        builder.append("Hidden low-confidence reports\n");
        builder.append("---------------------------\n");
        if (lastLowConfidenceReports.isEmpty())
        {
            builder.append("No low-confidence reports are currently hidden from the normal recent reports list.");
        }
        else
        {
            for (ReportSummary report : lastLowConfidenceReports)
            {
                builder.append(ReportTextFormatter.formatSummaryLine(report))
                    .append('\n')
                    .append("Evidence: ")
                    .append(report.getEvidenceSummaryText().isEmpty() ? "Unknown" : report.getEvidenceSummaryText())
                    .append("\n\n");
            }
        }
        return builder.toString().trim();
    }

    private void setDiagnosticsTabEnabled(boolean enabled)
    {
        final int index = tabs.indexOfComponent(developerDiagnosticsTab);
        if (enabled && index < 0)
        {
            tabs.addTab("Developer Diagnostics", developerDiagnosticsTab);
            diagnosticsTabVisible = true;
        }
        else if (!enabled && index >= 0)
        {
            tabs.remove(index);
            diagnosticsTabVisible = false;
        }
    }

    private void exportSelectedBundle()
    {
        if (exportBundleWriter == null)
        {
            JOptionPane.showMessageDialog(
                this,
                "Debug bundle export is unavailable in this context.",
                "Export debug bundle",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (selectedSummary == null)
        {
            JOptionPane.showMessageDialog(
                this,
                "Select a completed report before exporting a debug bundle.",
                "Export debug bundle",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try
        {
            final java.nio.file.Path bundlePath = exportBundleWriter.writeBundle(
                selectedSummary.getReportId(),
                exportBundleWriter.defaultExportDirectory());
            JOptionPane.showMessageDialog(
                this,
                "Exported debug bundle to " + bundlePath,
                "Export debug bundle",
                JOptionPane.INFORMATION_MESSAGE);
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(
                this,
                "TickSense could not export a debug bundle for the selected report.",
                "Export debug bundle",
                JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(
                this,
                "TickSense could not delete local data right now.",
                "Delete local data",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
