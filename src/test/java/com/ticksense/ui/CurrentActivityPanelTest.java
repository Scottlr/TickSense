package com.ticksense.ui;

import static org.junit.Assert.assertEquals;

import com.ticksense.activities.ActivityDiagnostic;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class CurrentActivityPanelTest
{
    @Test
    public void showsNoActivityWhenSessionIsAbsent()
    {
        final CurrentActivityPanel panel = new CurrentActivityPanel(
            Optional::empty,
            Collections::emptyList);

        panel.refresh();

        assertEquals("None", panel.getActivityText());
        assertEquals("No candidate", panel.getCandidateText());
    }

    @Test
    public void showsActiveActivityDisplayName()
    {
        final CurrentActivityPanel panel = new CurrentActivityPanel(
            () -> Optional.of(session()),
            Collections::emptyList);

        panel.refresh();

        assertEquals("Gem Mining", panel.getActivityText());
    }

    @Test
    public void showsLatestCandidateDiagnostic()
    {
        final CurrentActivityPanel panel = new CurrentActivityPanel(
            Optional::empty,
            () -> List.of(
                diagnostic(ActivityType.CONSTRUCTION, 0.60D, "NO_CONFIDENCE"),
                diagnostic(ActivityType.VARDORVIS, 0.85D, "STARTED")));

        panel.refresh();

        assertEquals("VARDORVIS 0.85 STARTED", panel.getCandidateText());
    }

    private static ActivitySession session()
    {
        return new ActivitySession(
            ActivityId.of("activity-1"),
            ActivityType.GEM_MINING,
            time(12),
            null,
            null,
            Collections.emptyList(),
            Collections.singletonMap("displayName", "Gem Mining"));
    }

    private static ActivityDiagnostic diagnostic(ActivityType activityType, double confidence, String decision)
    {
        return new ActivityDiagnostic(activityType, confidence, decision, "", time(15), Collections.emptyList());
    }

    private static EventTime time(int gameTick)
    {
        return new EventTime(1000L, 2000L, gameTick, 3000L, 4);
    }
}
