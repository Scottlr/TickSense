package com.ticksense.activities;

public final class ActivityMarkerTypes
{
    public static final String STARTED = "STARTED";
    public static final String FINISHED = "FINISHED";

    private ActivityMarkerTypes()
    {
    }

    public static boolean isFinished(ActivityMarker marker)
    {
        return marker != null && FINISHED.equals(marker.getMarkerType());
    }
}
