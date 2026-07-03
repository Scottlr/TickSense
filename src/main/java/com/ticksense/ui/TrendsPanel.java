package com.ticksense.ui;

import com.ticksense.analytics.TrendAnalyzer;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import net.runelite.client.ui.ColorScheme;

public final class TrendsPanel extends JPanel
{
    private final JTextArea textArea;

    public TrendsPanel()
    {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        textArea = PanelTextAreas.readOnlyWrapped();
        add(PanelScrollPanes.borderless(textArea), BorderLayout.CENTER);
    }

    public void setTrendSummary(TrendAnalyzer.TrendSummary trendSummary)
    {
        if (trendSummary == null || !trendSummary.hasData())
        {
            textArea.setText("No local trend data yet.");
            return;
        }

        final StringBuilder builder = new StringBuilder();
        for (TrendAnalyzer.ActivityTrend trend : trendSummary.getActivityTrends())
        {
            builder.append(trend.getActivityType().name())
                .append(" (")
                .append(trend.getSampleSize())
                .append(" reports");
            if (trend.isSmallSampleSize())
            {
                builder.append(", small sample");
            }
            builder.append(")\n");
            for (TrendAnalyzer.MetricTrend metricTrend : trend.getMetricTrends().values())
            {
                builder.append("  ")
                    .append(metricTrend.getMetricKey())
                    .append(": median ")
                    .append(String.format("%.2f", metricTrend.getMedianValue()))
                    .append(", delta ")
                    .append(String.format("%.2f", metricTrend.getDeltaFromFirst()))
                    .append('\n');
            }
            if (!trend.getRepeatedTickLossCategories().isEmpty())
            {
                builder.append("  repeated tick-loss: ")
                    .append(String.join(", ", trend.getRepeatedTickLossCategories()))
                    .append('\n');
            }
            builder.append('\n');
        }
        textArea.setText(builder.toString().trim());
    }
}
