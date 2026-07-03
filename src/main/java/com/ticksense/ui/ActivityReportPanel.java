package com.ticksense.ui;

import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.MetricValue;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;

public class ActivityReportPanel extends JPanel
{
    private final JLabel titleLabel = sectionValue("");
    private final JLabel durationValue = sectionValue("Unknown");
    private final JLabel gradeValue = sectionValue("Unknown");
    private final JLabel totalTickLossValue = sectionValue("Unknown");
    private final JLabel bestExecutionValue = sectionValue("Unknown");
    private final JLabel worstExecutionValue = sectionValue("Unknown");
    private final JLabel finishReasonValue = sectionValue("Unknown");
    private final JLabel confidenceValue = sectionValue("Unknown");
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
        content.add(PanelScrollPanes.borderless(evidenceValue));
        content.add(sectionLabel("Metrics"));
        content.add(metricPanel);
        content.add(sectionLabel("Opportunity timeline"));
        content.add(opportunityTimelinePanel);
        content.add(sectionLabel("Tick-loss breakdown"));
        content.add(tickLossBreakdownPanel);

        add(PanelScrollPanes.borderless(content), BorderLayout.CENTER);

        showReport(null);
    }

    public void showReport(ActivityReport report)
    {
        if (report == null)
        {
            titleLabel.setText("Select a report");
            durationValue.setText("Unknown");
            gradeValue.setText("Unknown");
            totalTickLossValue.setText("Unknown");
            bestExecutionValue.setText("Unknown");
            worstExecutionValue.setText("Unknown");
            finishReasonValue.setText("Unknown");
            confidenceValue.setText("Unknown");
            evidenceValue.setText("Completed reports will appear here.");
            metricPanel.setMetrics(null);
            opportunityTimelinePanel.setEntries(null);
            tickLossBreakdownPanel.setBreakdown(null);
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
            add(PanelScrollPanes.borderless(metricsText), BorderLayout.CENTER);
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
