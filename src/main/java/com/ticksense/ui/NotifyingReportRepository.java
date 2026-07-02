package com.ticksense.ui;

import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.ReportSummary;
import com.ticksense.storage.ReportRepository;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class NotifyingReportRepository implements ReportRepository
{
    private final ReportRepository delegate;
    private final CopyOnWriteArrayList<Runnable> saveListeners = new CopyOnWriteArrayList<>();

    public NotifyingReportRepository(ReportRepository delegate)
    {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    public void addSaveListener(Runnable listener)
    {
        saveListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    @Override
    public void save(ActivityReport report) throws IOException
    {
        delegate.save(report);
        for (Runnable listener : saveListeners)
        {
            listener.run();
        }
    }

    @Override
    public Optional<ActivityReport> findById(String reportId) throws IOException
    {
        return delegate.findById(reportId);
    }

    @Override
    public List<ReportSummary> listRecent(int limit) throws IOException
    {
        return delegate.listRecent(limit);
    }
}
