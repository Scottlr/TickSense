package com.ticksense.activities;

import com.ticksense.common.ImmutableCollections;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public final class ActivityDiagnosticBuffer
{
    private final int capacity;
    private final Deque<ActivityDiagnostic> diagnostics = new ArrayDeque<>();

    public ActivityDiagnosticBuffer(int capacity)
    {
        if (capacity <= 0)
        {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    public void add(ActivityDiagnostic diagnostic)
    {
        if (diagnostics.size() == capacity)
        {
            diagnostics.removeFirst();
        }
        diagnostics.addLast(diagnostic);
    }

    public List<ActivityDiagnostic> snapshot()
    {
        if (diagnostics.isEmpty())
        {
            return Collections.emptyList();
        }
        return ImmutableCollections.immutableList(diagnostics);
    }
}
