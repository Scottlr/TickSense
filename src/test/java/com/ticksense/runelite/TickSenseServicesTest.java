package com.ticksense.runelite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ticksense.activities.scurrius.ScurriusModule;
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.storage.JsonReportRepository;
import com.ticksense.storage.JsonlTimelineRepository;
import com.ticksense.storage.TickSenseDataPaths;
import com.ticksense.storage.debug.DebugEventKind;
import com.ticksense.storage.debug.DebugEventRecord;
import com.ticksense.storage.debug.DebugEventRecorder;
import com.ticksense.telemetry.TelemetryBus;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.events.NpcStateTelemetryEvent;
import com.ticksense.telemetry.events.ProjectileTelemetryEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import net.runelite.api.NpcID;
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

    @Test
    public void streamsObserveOnlyBossDiagnosticsToDebugRecorder() throws Exception
    {
        final TickSenseDataPaths paths = new TickSenseDataPaths(Files.createTempDirectory("ticksense-observe-only-debug"));
        final TelemetryBus telemetryBus = new TelemetryBus();
        final DebugEventRecorder debugRecorder = new DebugEventRecorder(paths.getTickSenseRoot().resolve("debug"), () -> "session-scurrius");
        final TickSenseServices services = TickSenseServices.create(
            telemetryBus,
            new JsonlTimelineRepository(paths, "observe-only", new Gson(), Clock.fixed(Instant.parse("2026-07-03T02:00:00Z"), ZoneOffset.UTC)),
            new JsonReportRepository(paths, new Gson()),
            Collections.singletonList(new ScurriusModule()),
            true,
            debugRecorder);

        try
        {
            debugRecorder.startSession(true, 25, 5);
            services.start();
            telemetryBus.accept(TelemetryEnvelope.create(
                "event-scurrius",
                "session-scurrius",
                new NpcStateTelemetryEvent(
                    new EventTime(100L, 200L, 321, 400L, 500),
                    Collections.singletonMap("source", "NpcSpawned"),
                    EntityRef.npc(1, NpcID.SCURRIUS, "Scurrius"),
                    "SPAWNED",
                    new WorldLocation(301, 0, 3200, 3201, 12_550, false),
                    9999,
                    8888,
                    EntityRef.localPlayer(),
                    10,
                    20)));

            assertFalse(services.getStrategyEngine().getActiveSession().isPresent());
            assertEquals(1, services.getStrategyEngine().getDiagnostics().size());
            assertEquals("SUPPRESSED", services.getStrategyEngine().getDiagnostics().get(0).getDecision());

            final DebugEventRecord diagnosticRecord = firstDebugRecord(paths.getTickSenseRoot().resolve("debug"), DebugEventKind.ACTIVITY_DIAGNOSTIC);
            assertEquals("session-scurrius", diagnosticRecord.getSessionId());
            assertEquals("SCURRIUS", diagnosticRecord.getSourceEventType());
            assertTrue(diagnosticRecord.getPayloadJson().contains("Known boss NPC observed: npc:" + NpcID.SCURRIUS));
            assertTrue(diagnosticRecord.getPayloadJson().contains("Unverified event ID observed: animation:9999"));
        }
        finally
        {
            services.close();
            debugRecorder.close();
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

    private static DebugEventRecord firstDebugRecord(Path debugDirectory, DebugEventKind kind) throws Exception
    {
        final Gson gson = new Gson();
        try (Stream<Path> files = Files.list(debugDirectory))
        {
            final Path debugFile = files
                .filter(path -> path.getFileName().toString().endsWith(".jsonl"))
                .findFirst()
                .get();
            for (String line : Files.readAllLines(debugFile, StandardCharsets.UTF_8))
            {
                final DebugEventRecord record = gson.fromJson(line, DebugEventRecord.class);
                if (record.getKind() == kind)
                {
                    return record;
                }
            }
        }
        throw new AssertionError("Missing debug record kind " + kind);
    }
}
