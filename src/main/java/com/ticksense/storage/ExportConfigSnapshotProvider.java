package com.ticksense.storage;

import java.util.Map;

public interface ExportConfigSnapshotProvider
{
    Map<String, Object> snapshot();
}
