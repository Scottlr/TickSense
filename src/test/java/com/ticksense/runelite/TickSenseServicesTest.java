package com.ticksense.runelite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.storage.JsonReportRepository;
import com.ticksense.storage.JsonlTimelineRepository;
import com.ticksense.storage.TickSenseDataPaths;
import com.ticksense.telemetry.TelemetryBus;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.events.ProjectileTelemetryEvent;
import java.nio.file.Files;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class TickSenseServicesTest
{
    @Test
    public void tracksObservedIdsFromTelemetryIntake() throws Exception
    {
        final TickSenseDataPaths paths = new TickSenseDataPaths(Files.createTempDirectory("ticksense-observed-ids"));
        final TelemetryBus telemetryBus = new TelemetryBus();
        final TickSenseServices services = TickSenseServices.create(
            telemetryBus,
            new JsonlTimelineRepository(paths, "observed-ids", new Gson(), Clock.fixed(Instant.parse("2026-07-03T01:00:00Z"), ZoneOffset.UTC)),
            new JsonReportRepository(paths, new Gson()),
            Collections.emptyList(),
            true);

        try
        {
            services.start();
            telemetryBus.accept(TelemetryEnvelope.create(
                "event-projectile",
                "session-observed",
                new ProjectileTelemetryEvent(
                    new EventTime(100L, 200L, 321, 400L, 500),
                    Collections.singletonMap("source", "ProjectileMoved"),
                    1234,
                    EntityRef.npc(7, 5678, "Boss"),
                    EntityRef.localPlayer(),
                    new WorldLocation(301, 0, 3200, 3201, 12_550, false),
                    10,
                    20)));

            final List<ObservedId> observedIds = services.getObservedIds();
            assertTrue(contains(observedIds, "projectile", 1234));
            assertTrue(contains(observedIds, "source-npc", 5678));
            assertTrue(contains(observedIds, "region", 12_550));
            final ObservedId projectile = find(observedIds, "projectile", 1234);
            assertEquals("ProjectileMoved", projectile.getSourceEventType());
            assertEquals(321, projectile.getLastSeenTick());
            assertEquals(1, projectile.getCount());
        }
        finally
        {
            services.close();
        }
    }

    private static boolean contains(List<ObservedId> observedIds, String kind, int id)
    {
        return find(observedIds, kind, id) != null;
    }

    private static ObservedId find(List<ObservedId> observedIds, String kind, int id)
    {
        for (ObservedId observedId : observedIds)
        {
            if (observedId.getKind().equals(kind) && observedId.getId() == id)
            {
                return observedId;
            }
        }
        return null;
    }
}
