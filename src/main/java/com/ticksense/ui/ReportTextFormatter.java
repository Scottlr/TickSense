package com.ticksense.ui;

import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.MetricUnit;
import com.ticksense.analytics.OpportunityTimelineEntry;
import com.ticksense.analytics.ReportSummary;
import com.ticksense.core.FinishReasonType;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class ReportTextFormatter
{
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.US));
    private static final String UNKNOWN = "Unknown";

    private ReportTextFormatter()
    {
    }

    static String formatSummaryLine(ReportSummary summary)
    {
        return summary.getDetectedActivityName()
            + " - "
            + formatDuration(summary.getDurationMillis(), summary.getDurationTicks())
            + " - "
            + formatConfidence(summary.getConfidence());
    }

    static String formatDuration(long durationMillis, int durationTicks)
    {
        if (durationMillis <= 0L && durationTicks <= 0)
        {
            return UNKNOWN;
        }

        final long totalTenths = Math.max(0L, durationMillis / 100L);
        final long minutes = totalTenths / 600L;
        final long seconds = (totalTenths / 10L) % 60L;
        final long tenths = totalTenths % 10L;
        return String.format(Locale.US, "%d:%02d.%d / %d ticks", minutes, seconds, tenths, Math.max(0, durationTicks));
    }

    static String formatConfidence(double confidence)
    {
        return Math.round(confidence * 100.0D) + "%";
    }

    static String formatFinishReason(FinishReasonType finishReasonType)
    {
        if (finishReasonType == null)
        {
            return UNKNOWN;
        }
        return finishReasonType.displayName();
    }

    static String gradeForMetrics(Map<String, MetricValue> metrics)
    {
        if (metrics == null || metrics.isEmpty())
        {
            return UNKNOWN;
        }

        for (MetricValue metric : metrics.values())
        {
            if (metric.getUnit() == MetricUnit.SCORE)
            {
                final double score = metric.getValue();
                if (score >= 97.0D)
                {
                    return "S";
                }
                if (score >= 93.0D)
                {
                    return "A+";
                }
                if (score >= 87.0D)
                {
                    return "A";
                }
                if (score >= 80.0D)
                {
                    return "B+";
                }
                if (score >= 73.0D)
                {
                    return "B";
                }
                if (score >= 67.0D)
                {
                    return "C+";
                }
                if (score >= 60.0D)
                {
                    return "C";
                }
                if (score >= 50.0D)
                {
                    return "D";
                }
                return "F";
            }
        }

        return UNKNOWN;
    }

    static String formatMetricValue(MetricValue metricValue)
    {
        if (metricValue == null)
        {
            return UNKNOWN;
        }

        final double value = metricValue.getValue();
        switch (metricValue.getUnit())
        {
            case TICKS:
                return DECIMAL_FORMAT.format(value) + " ticks";
            case MILLISECONDS:
                return DECIMAL_FORMAT.format(value) + " ms";
            case SECONDS:
                return DECIMAL_FORMAT.format(value) + " s";
            case COUNT:
                return DECIMAL_FORMAT.format(value);
            case PER_HOUR:
                return DECIMAL_FORMAT.format(value) + "/hr";
            case PERCENT:
                return DECIMAL_FORMAT.format(value) + "%";
            case SCORE:
                return DECIMAL_FORMAT.format(value);
            default:
                return DECIMAL_FORMAT.format(value);
        }
    }

    static String bestExecution(ActivityReport report)
    {
        return summaryLine(report, 0);
    }

    static String worstExecution(ActivityReport report)
    {
        return summaryLine(report, 1);
    }

    static String timelineEntry(OpportunityTimelineEntry entry)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("Tick ").append(entry.getGameTick()).append("  ").append(entry.getLabel());
        if (entry.getLatencyTicks() != null)
        {
            builder.append(" (+").append(entry.getLatencyTicks()).append(" ticks)");
        }
        builder.append("  ").append(entry.getStatus());
        return builder.toString();
    }

    static String evidenceText(List<String> evidence)
    {
        if (evidence == null || evidence.isEmpty())
        {
            return UNKNOWN;
        }
        return String.join(" | ", evidence);
    }

    private static String summaryLine(ActivityReport report, int index)
    {
        if (report == null || report.getSummaryLines().size() <= index)
        {
            return UNKNOWN;
        }
        return report.getSummaryLines().get(index);
    }
}
