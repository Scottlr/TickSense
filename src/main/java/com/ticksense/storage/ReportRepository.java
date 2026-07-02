package com.ticksense.storage;

import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.ReportSummary;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ReportRepository
{
    void save(ActivityReport report) throws IOException;

    Optional<ActivityReport> findById(String reportId) throws IOException;

    List<ReportSummary> listRecent(int limit) throws IOException;
}
