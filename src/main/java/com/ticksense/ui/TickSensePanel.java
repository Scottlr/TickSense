package com.ticksense.ui;

import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.ReportSummary;
import com.ticksense.runelite.TickSenseConfig;
import com.ticksense.storage.DeleteAllDataService;
import com.ticksense.storage.ReportRepository;
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

    private final ReportRepository reportRepository;
    private final DeleteAllDataService deleteAllDataService;
    private final ConfigManager configManager;
    private final TickSenseConfig config;
    private final ReportListPanel reportListPanel;
    private final ActivityReportPanel activityReportPanel;
    private final JTextArea developerDiagnosticsArea;
    private final JTabbedPane tabs;
    private final Component developerDiagnosticsTab;

    public TickSensePanel(
        ReportRepository reportRepository,
        DeleteAllDataService deleteAllDataService,
        ConfigManager configManager,
        TickSenseConfig config)
    {
        super(false);
        this.reportRepository = reportRepository;
        this.deleteAllDataService = deleteAllDataService;
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
        developerDiagnosticsArea = new JTextArea();
        developerDiagnosticsArea.setEditable(false);
        developerDiagnosticsArea.setLineWrap(true);
        developerDiagnosticsArea.setWrapStyleWord(true);
        developerDiagnosticsArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        developerDiagnosticsArea.setForeground(ColorScheme.TEXT_COLOR);
        developerDiagnosticsArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        tabs = new JTabbedPane();
        tabs.addTab("Recent Reports", createRecentReportsTab());
        tabs.addTab("Trends", createDisabledPlaceholder("Trends will stay hidden until local trend summaries land."));
        tabs.setEnabledAt(1, false);
        tabs.addTab("Settings", createSettingsTab());
        developerDiagnosticsTab = createDeveloperDiagnosticsTab();
        tabs.addTab("Developer Diagnostics", developerDiagnosticsTab);
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
            if (normalReports.isEmpty())
            {
                activityReportPanel.showReport(null);
            }
            updateDeveloperDiagnostics(lowConfidenceReports);
            setDiagnosticsTabEnabled(config.debugActivityDiagnostics());
        }
        catch (IOException ex)
        {
            reportListPanel.setEmptyState("Report storage is unavailable.");
            reportListPanel.setReports(null);
            activityReportPanel.showReport(null);
            developerDiagnosticsArea.setText("Developer diagnostics are unavailable while reports fail to load.");
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

    private JScrollPane createDeveloperDiagnosticsTab()
    {
        return new JScrollPane(developerDiagnosticsArea);
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

    private void updateDeveloperDiagnostics(List<ReportSummary> lowConfidenceReports)
    {
        if (lowConfidenceReports == null || lowConfidenceReports.isEmpty())
        {
            developerDiagnosticsArea.setText("No low-confidence reports are currently hidden from the normal recent reports list.");
            return;
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("Low-confidence reports stay out of the normal panel.\n\n");
        for (ReportSummary report : lowConfidenceReports)
        {
            builder.append(ReportTextFormatter.formatSummaryLine(report))
                .append('\n')
                .append("Evidence: ")
                .append(report.getEvidenceSummaryText().isEmpty() ? "Unknown" : report.getEvidenceSummaryText())
                .append("\n\n");
        }
        developerDiagnosticsArea.setText(builder.toString().trim());
    }

    private void setDiagnosticsTabEnabled(boolean enabled)
    {
        final int index = tabs.indexOfComponent(developerDiagnosticsTab);
        if (index >= 0)
        {
            tabs.setEnabledAt(index, enabled);
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
