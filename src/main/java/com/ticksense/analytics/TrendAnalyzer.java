package com.ticksense.analytics;

import com.ticksense.core.ActivityType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TrendAnalyzer
{
    public TrendSummary summarize(List<ReportSummary> reports)
    {
        final List<ReportSummary> normalizedReports = reports == null ? Collections.emptyList() : reports;
        final Map<String, List<ReportSummary>> grouped = new LinkedHashMap<>();
        for (ReportSummary report : normalizedReports)
        {
            final String key = report.getActivityType().name() + ":" + report.getSchemaVersion();
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(report);
        }

        final List<ActivityTrend> trends = new ArrayList<>();
        for (List<ReportSummary> group : grouped.values())
        {
            if (!group.isEmpty())
            {
                trends.add(summarizeGroup(group));
            }
        }
        trends.sort(Comparator.comparingInt(ActivityTrend::getSampleSize).reversed().thenComparing(trend -> trend.getActivityType().name()));
        return new TrendSummary(trends);
    }

    private ActivityTrend summarizeGroup(List<ReportSummary> reports)
    {
        final List<ReportSummary> sorted = new ArrayList<>(reports);
        sorted.sort(Comparator.comparingLong(ReportSummary::getCreatedAtMillis));
        final ReportSummary newest = sorted.get(sorted.size() - 1);

        final Map<String, MetricTrend> metricTrends = new LinkedHashMap<>();
        final Map<String, List<Double>> metricSamples = new LinkedHashMap<>();
        final Map<String, Integer> tickLossCounts = new LinkedHashMap<>();
        for (ReportSummary report : sorted)
        {
            if (report.getActivityType() != newest.getActivityType() || report.getSchemaVersion() != newest.getSchemaVersion())
            {
                continue;
            }
            for (Map.Entry<String, Double> entry : report.getMetricValues().entrySet())
            {
                metricSamples.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>()).add(entry.getValue());
            }
            for (String category : report.getTickLossCategories().keySet())
            {
                tickLossCounts.put(category, tickLossCounts.getOrDefault(category, 0) + 1);
            }
        }

        for (Map.Entry<String, List<Double>> entry : metricSamples.entrySet())
        {
            if (entry.getValue().size() < 2)
            {
                continue;
            }
            final List<Double> values = new ArrayList<>(entry.getValue());
            values.sort(Double::compareTo);
            final double median = values.get(values.size() / 2);
            final double first = sortedMetricValue(sorted.get(0), entry.getKey());
            final double latest = sortedMetricValue(newest, entry.getKey());
            metricTrends.put(entry.getKey(), new MetricTrend(entry.getKey(), median, latest - first));
        }

        final List<String> repeatedTickLossCategories = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : tickLossCounts.entrySet())
        {
            if (entry.getValue() >= 2)
            {
                repeatedTickLossCategories.add(entry.getKey());
            }
        }
        Collections.sort(repeatedTickLossCategories);

        return new ActivityTrend(
            newest.getActivityType(),
            newest.getSchemaVersion(),
            sorted.size(),
            sorted.size() < 3,
            metricTrends,
            repeatedTickLossCategories);
    }

    private static double sortedMetricValue(ReportSummary report, String key)
    {
        final Double value = report.getMetricValues().get(key);
        return value == null ? 0.0D : value;
    }

    public static final class TrendSummary
    {
        private final List<ActivityTrend> activityTrends;

        private TrendSummary(List<ActivityTrend> activityTrends)
        {
            this.activityTrends = Collections.unmodifiableList(new ArrayList<>(activityTrends));
        }

        public List<ActivityTrend> getActivityTrends()
        {
            return activityTrends;
        }

        public boolean hasData()
        {
            return !activityTrends.isEmpty();
        }
    }

    public static final class ActivityTrend
    {
        private final ActivityType activityType;
        private final int schemaVersion;
        private final int sampleSize;
        private final boolean smallSampleSize;
        private final Map<String, MetricTrend> metricTrends;
        private final List<String> repeatedTickLossCategories;

        private ActivityTrend(
            ActivityType activityType,
            int schemaVersion,
            int sampleSize,
            boolean smallSampleSize,
            Map<String, MetricTrend> metricTrends,
            List<String> repeatedTickLossCategories)
        {
            this.activityType = Objects.requireNonNull(activityType, "activityType");
            this.schemaVersion = schemaVersion;
            this.sampleSize = sampleSize;
            this.smallSampleSize = smallSampleSize;
            this.metricTrends = Collections.unmodifiableMap(new LinkedHashMap<>(metricTrends));
            this.repeatedTickLossCategories = Collections.unmodifiableList(new ArrayList<>(repeatedTickLossCategories));
        }

        public ActivityType getActivityType()
        {
            return activityType;
        }

        public int getSchemaVersion()
        {
            return schemaVersion;
        }

        public int getSampleSize()
        {
            return sampleSize;
        }

        public boolean isSmallSampleSize()
        {
            return smallSampleSize;
        }

        public Map<String, MetricTrend> getMetricTrends()
        {
            return metricTrends;
        }

        public List<String> getRepeatedTickLossCategories()
        {
            return repeatedTickLossCategories;
        }
    }

    public static final class MetricTrend
    {
        private final String metricKey;
        private final double medianValue;
        private final double deltaFromFirst;

        private MetricTrend(String metricKey, double medianValue, double deltaFromFirst)
        {
            this.metricKey = Objects.requireNonNull(metricKey, "metricKey");
            this.medianValue = medianValue;
            this.deltaFromFirst = deltaFromFirst;
        }

        public String getMetricKey()
        {
            return metricKey;
        }

        public double getMedianValue()
        {
            return medianValue;
        }

        public double getDeltaFromFirst()
        {
            return deltaFromFirst;
        }
    }
}
