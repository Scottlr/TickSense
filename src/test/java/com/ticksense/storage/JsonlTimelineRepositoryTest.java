package com.ticksense.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.EvidenceStrength;
import com.ticksense.activities.OpportunityEvidence;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class JsonlTimelineRepositoryTest
{
    @Test
    public void appendsAndReadsTelemetryEvents() throws IOException
    {
        final TickSenseDataPaths paths = repositoryPaths();
        final JsonlTimelineRepository repository = repository(paths, "session-a", "2026-07-02T20:30:00Z");
        final TelemetryEnvelope first = telemetry("event-1", "session-a", 1_000L, 100);
        final TelemetryEnvelope second = telemetry("event-2", "session-a", 1_600L, 101);

        repository.append(first);
        repository.append(second);

        assertTrue(Files.exists(repository.getTimelineFile()));
        assertTrue(Files.exists(paths.getSchemaVersionFile()));
        final List<String> lines = Files.readAllLines(repository.getTimelineFile(), StandardCharsets.UTF_8);
        assertEquals(2, lines.size());
        repository.close();
    }

    @Test
    public void readsActivityTimelineBetweenMarkers() throws IOException
    {
        final JsonlTimelineRepository repository = repository(repositoryPaths(), "session-b", "2026-07-02T20:31:00Z");
        final ActivityId activityId = ActivityId.of("activity-1");
        repository.append(telemetry("before", "session-b", 900L, 99));
        repository.appendActivityMarker(activityMarker("marker-start", activityId, "STARTED", 1_000L, 100));
        repository.append(telemetry("inside-1", "session-b", 1_100L, 101));
        repository.append(telemetry("inside-2", "session-b", 1_300L, 102));
        repository.appendActivityMarker(activityMarker("marker-end", activityId, "FINISHED", 1_400L, 103));
        repository.append(telemetry("after", "session-b", 1_500L, 104));

        final CompletedActivityTimeline timeline = repository.readActivityTimeline(activityId);

        assertEquals(activityId, timeline.getActivityId());
        assertEquals(2, timeline.getTelemetryEvents().size());
        assertEquals("inside-1", timeline.getTelemetryEvents().get(0).getEventId());
        assertEquals("inside-2", timeline.getTelemetryEvents().get(1).getEventId());
        assertEquals(2, timeline.getActivityMarkers().size());
    }

    @Test
    public void readsOpportunityMarkersWithActivityTimeline() throws IOException
    {
        final JsonlTimelineRepository repository = repository(repositoryPaths(), "session-c", "2026-07-02T20:32:00Z");
        final ActivityId activityId = ActivityId.of("activity-2");
        repository.appendActivityMarker(activityMarker("marker-start", activityId, "STARTED", 1_000L, 100));
        repository.appendOpportunityMarker(opportunityMarker("opp-marker", "opp-1", activityId, 1_200L, 101));
        repository.appendActivityMarker(activityMarker("marker-end", activityId, "FINISHED", 1_400L, 102));

        final CompletedActivityTimeline timeline = repository.readActivityTimeline(activityId);

        assertEquals(1, timeline.getOpportunityMarkers().size());
        assertEquals("opp-marker", timeline.getOpportunityMarkers().get(0).getMarkerId());
        assertEquals(OpportunityStatus.COMPLETED, timeline.getOpportunityMarkers().get(0).getStatus());
    }

    @Test
    public void skipsCorruptLineWithDiagnostic() throws Exception
    {
        final TickSenseDataPaths paths = repositoryPaths();
        JsonlTimelineRepository repository = repository(paths, "session-d", "2026-07-02T20:33:00Z");
        final ActivityId activityId = ActivityId.of("activity-3");
        repository.appendActivityMarker(activityMarker("marker-start", activityId, "STARTED", 1_000L, 100));
        repository.appendOpportunityMarker(opportunityMarker("opp-marker", "opp-2", activityId, 1_100L, 101));
        repository.close();
        Files.write(
            repository.getTimelineFile(),
            Collections.singletonList("this is not json"),
            StandardCharsets.UTF_8,
            java.nio.file.StandardOpenOption.APPEND);
        repository = repository(paths, "session-d-reader", "2026-07-02T20:33:30Z");
        repository.appendActivityMarker(activityMarker("marker-end", activityId, "FINISHED", 1_400L, 102));

        final CompletedActivityTimeline timeline = repository.readActivityTimeline(activityId);

        assertEquals(1, repository.getCorruptLineCount());
        assertEquals(1, timeline.getOpportunityMarkers().size());
        assertEquals(2, timeline.getActivityMarkers().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnsupportedSchemaVersion() throws Exception
    {
        final TickSenseDataPaths paths = repositoryPaths();
        final JsonlTimelineRepository repository = repository(paths, "session-e", "2026-07-02T20:34:00Z");
        Files.write(
            paths.getSchemaVersionFile(),
            Collections.singletonList(new Gson().toJson(Collections.singletonMap("schemaVersion", 999))),
            StandardCharsets.UTF_8);
        repository.readActivityRecords(ActivityId.of("activity-4"));
    }

    private JsonlTimelineRepository repository(TickSenseDataPaths paths, String sessionId, String instant) throws IOException
    {
        return new JsonlTimelineRepository(
            paths,
            sessionId,
            new Gson(),
            Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
    }

    private TickSenseDataPaths repositoryPaths() throws IOException
    {
        final Path root = Files.createTempDirectory("ticksense-timelines");
        return new TickSenseDataPaths(root);
    }

    private static TelemetryEnvelope telemetry(String eventId, String sessionId, long wallTimeMillis, int gameTick)
    {
        return TelemetryEnvelope.create(
            eventId,
            sessionId,
            new PlayerActionTelemetryEvent(
                time(wallTimeMillis, gameTick),
                Collections.singletonMap("source", "MenuOptionClicked"),
                "Attack",
                "Spider",
                EntityRef.npc(1, 2, "Spider"),
                "NPC_FIRST_OPTION",
                new WorldLocation(301, 0, 3200, 3201, 12_850, false),
                2));
    }

    private static ActivityMarker activityMarker(String markerId, ActivityId activityId, String markerType, long wallTimeMillis, int gameTick)
    {
        return new ActivityMarker(
            markerId,
            activityId,
            ActivityType.ARAXXOR,
            markerType,
            time(wallTimeMillis, gameTick),
            Collections.singletonMap("phase", markerType));
    }

    private static OpportunityMarker opportunityMarker(String markerId, String opportunityId, ActivityId activityId, long wallTimeMillis, int gameTick)
    {
        return new OpportunityMarker(
            markerId,
            opportunityId,
            activityId,
            "SPIDER_ENGAGEMENT",
            OpportunityStatus.COMPLETED,
            time(wallTimeMillis, gameTick),
            Collections.singletonMap("phase", "spider"),
            Arrays.asList(new OpportunityEvidence(
                time(wallTimeMillis, gameTick),
                "InteractingChanged",
                EvidenceStrength.CONFIRMING,
                "Confirmed")));
    }

    private static EventTime time(long wallTimeMillis, int gameTick)
    {
        return new EventTime(wallTimeMillis, wallTimeMillis * 1_000_000L, gameTick, gameTick * 10L, gameTick);
    }
}
