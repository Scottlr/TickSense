package com.ticksense.telemetry;

import static org.junit.Assert.assertTrue;

import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.events.NpcStateTelemetryEvent;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class TelemetryIdExtractorTest
{
    @Test
    public void extractsReusableIdsFromNpcTelemetry()
    {
        final List<ObservedTelemetryId> ids = TelemetryIdExtractor.extract(new NpcStateTelemetryEvent(
            new EventTime(100L, 200L, 300, 400L, 500),
            Collections.singletonMap("source", "NpcSpawned"),
            EntityRef.npc(1, 7221, "Scurrius"),
            "SPAWNED",
            new WorldLocation(301, 0, 3200, 3201, 12345, false),
            999,
            888,
            EntityRef.npc(2, 12077, "Phantom Muspah"),
            10,
            20));

        assertTrue(ids.contains(new ObservedTelemetryId("npc", 7221)));
        assertTrue(ids.contains(new ObservedTelemetryId("animation", 999)));
        assertTrue(ids.contains(new ObservedTelemetryId("graphic", 888)));
        assertTrue(ids.contains(new ObservedTelemetryId("interacting-npc", 12077)));
        assertTrue(ids.contains(new ObservedTelemetryId("region", 12345)));
    }
}
