package com.ticksense.ui;

import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.MetricValue;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;

public class ActivityReportPanel extends JPanel
{
    private static final String EMPTY_REPORT_TITLE = "Select a report";
    private static final String UNKNOWN_VALUE = "Unknown";
    private static final String EMPTY_REPORT_MESSAGE = "Completed reports will appear here.";

    private final JLabel titleLabel = sectionValue("");
    private final JLabel durationValue = sectionValue(UNKNOWN_VALUE);
    private final JLabel gradeValue = sectionValue(UNKNOWN_VALUE);
    private final JLabel totalTickLossValue = sectionValue(UNKNOWN_VALUE);
    private final JLabel bestExecutionValue = sectionValue(UNKNOWN_VALUE);
    private final JLabel worstExecutionValue = sectionValue(UNKNOWN_VALUE);
    private final JLabel finishReasonValue = sectionValue(UNKNOWN_VALUE);
    private final JLabel confidenceValue = sectionValue(UNKNOWN_VALUE);
    private final JTextArea evidenceValue = sectionText();
    private final DefaultMetricPanel metricPanel = new DefaultMetricPanel();
    private final OpportunityTimelinePanel opportunityTimelinePanel = new OpportunityTimelinePanel();
    private final TickLossBreakdownPanel tickLossBreakdownPanel = new TickLossBreakdownPanel();

    public ActivityReportPanel()
    {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        final JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ColorScheme.DARK_GRAY_COLOR);
        content.setBorder(new EmptyBorder(8, 8, 8, 8));

        titleLabel.setForeground(ColorScheme.BRAND_ORANGE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        content.add(titleLabel);
        content.add(createOverviewPanel());
        content.add(sectionLabel("Evidence"));
        final JScrollPane evidenceScrollPane = new JScrollPane(evidenceValue);
        evidenceScrollPane.setBorder(null);
        content.add(evidenceScrollPane);
        content.add(sectionLabel("Metrics"));
        content.add(metricPanel);
        content.add(sectionLabel("Opportunity timeline"));
        content.add(opportunityTimelinePanel);
        content.add(sectionLabel("Tick-loss breakdown"));
        content.add(tickLossBreakdownPanel);

        final JScrollPane contentScrollPane = new JScrollPane(content);
        contentScrollPane.setBorder(null);
        add(contentScrollPane, BorderLayout.CENTER);

        showReport(null);
    }

    public void showReport(ActivityReport report)
    {
        if (report == null)
        {
            resetReportDisplay();
            return;
        }

        titleLabel.setText(report.getDetectedActivityName());
        durationValue.setText(ReportTextFormatter.formatDuration(report.getDurationMillis(), report.getDurationTicks()));
        gradeValue.setText(ReportTextFormatter.gradeForMetrics(report.getMetrics()));
        totalTickLossValue.setText(report.getTickLossBreakdown().getTotalTickLoss() + " ticks");
        bestExecutionValue.setText(ReportTextFormatter.bestExecution(report));
        worstExecutionValue.setText(ReportTextFormatter.worstExecution(report));
        finishReasonValue.setText(ReportTextFormatter.formatFinishReason(report.getFinishReason().getType()));
        confidenceValue.setText(ReportTextFormatter.formatConfidence(report.getConfidence()));
        evidenceValue.setText(ReportTextFormatter.evidenceText(report.getEvidenceSummary()));
        metricPanel.setMetrics(report.getMetrics());
        opportunityTimelinePanel.setEntries(report.getOpportunities());
        tickLossBreakdownPanel.setBreakdown(report.getTickLossBreakdown());
    }

    private void resetReportDisplay()
    {
        titleLabel.setText(EMPTY_REPORT_TITLE);
        durationValue.setText(UNKNOWN_VALUE);
        gradeValue.setText(UNKNOWN_VALUE);
        totalTickLossValue.setText(UNKNOWN_VALUE);
        bestExecutionValue.setText(UNKNOWN_VALUE);
        worstExecutionValue.setText(UNKNOWN_VALUE);
        finishReasonValue.setText(UNKNOWN_VALUE);
        confidenceValue.setText(UNKNOWN_VALUE);
        evidenceValue.setText(EMPTY_REPORT_MESSAGE);
        metricPanel.setMetrics(null);
        opportunityTimelinePanel.setEntries(null);
        tickLossBreakdownPanel.setBreakdown(null);
    }

    private JPanel createOverviewPanel()
    {
        final JPanel overview = new JPanel(new GridLayout(0, 2, 8, 6));
        overview.setBackground(ColorScheme.DARK_GRAY_COLOR);
        overview.setBorder(new EmptyBorder(0, 0, 8, 0));

        overview.add(sectionLabel("Duration"));
        overview.add(durationValue);
        overview.add(sectionLabel("Grade"));
        overview.add(gradeValue);
        overview.add(sectionLabel("Total tick loss"));
        overview.add(totalTickLossValue);
        overview.add(sectionLabel("Best execution"));
        overview.add(bestExecutionValue);
        overview.add(sectionLabel("Worst execution"));
        overview.add(worstExecutionValue);
        overview.add(sectionLabel("Finish reason"));
        overview.add(finishReasonValue);
        overview.add(sectionLabel("Confidence"));
        overview.add(confidenceValue);
        return overview;
    }

    private static JLabel sectionLabel(String text)
    {
        final JLabel label = new JLabel(text);
        label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        label.setBorder(new EmptyBorder(0, 0, 4, 0));
        return label;
    }

    private static JLabel sectionValue(String text)
    {
        final JLabel label = new JLabel(text);
        label.setForeground(ColorScheme.TEXT_COLOR);
        return label;
    }

    private static JTextArea sectionText()
    {
        return PanelTextAreas.readOnlyWrapped();
    }

    private static final class DefaultMetricPanel extends JPanel
    {
        private final JTextArea metricsText = sectionText();

        private DefaultMetricPanel()
        {
            super(new BorderLayout());
            setBackground(ColorScheme.DARK_GRAY_COLOR);
            setBorder(new EmptyBorder(0, 0, 8, 0));
            final JScrollPane metricsScrollPane = new JScrollPane(metricsText);
            metricsScrollPane.setBorder(null);
            add(metricsScrollPane, BorderLayout.CENTER);
        }

        private void setMetrics(Map<String, MetricValue> metrics)
        {
            if (metrics == null || metrics.isEmpty())
            {
                metricsText.setText("No metrics yet.");
                return;
            }

            final StringBuilder builder = new StringBuilder();
            for (MetricValue metric : metrics.values())
            {
                if (builder.length() > 0)
                {
                    builder.append('\n');
                }
                builder.append(metric.getDefinition().getDisplayName())
                    .append(": ")
                    .append(ReportTextFormatter.formatMetricValue(metric));
            }
            metricsText.setText(builder.toString());
        }
    }
}
