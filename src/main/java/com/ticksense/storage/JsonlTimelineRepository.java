package com.ticksense.storage;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.OpportunityEvidence;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryJson;
import com.ticksense.telemetry.TelemetrySchema;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JsonlTimelineRepository implements TimelineRepository
{
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);

    private final TickSenseDataPaths dataPaths;
    private final String sessionId;
    private final Gson gson;
    private final Clock clock;
    private final Path timelineFile;
    private final BufferedWriter writer;

    private int corruptLineCount;

    public JsonlTimelineRepository(String sessionId) throws IOException
    {
        this(TickSenseDataPaths.defaultPaths(), sessionId, new Gson(), Clock.systemUTC());
    }

    public JsonlTimelineRepository(TickSenseDataPaths dataPaths, String sessionId, Gson gson, Clock clock) throws IOException
    {
        this.dataPaths = Objects.requireNonNull(dataPaths, "dataPaths");
        this.sessionId = requireText(sessionId, "sessionId");
        this.gson = Objects.requireNonNull(gson, "gson");
        this.clock = Objects.requireNonNull(clock, "clock");

        Files.createDirectories(dataPaths.getTimelinesDirectory());
        ensureSchemaVersionFile();
        this.timelineFile = dataPaths.getTimelinesDirectory().resolve(FILE_STAMP.format(clock.instant()) + "-" + this.sessionId + ".jsonl");
        this.writer = Files.newBufferedWriter(
            timelineFile,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND,
            StandardOpenOption.WRITE);
    }

    public Path getTimelineFile()
    {
        return timelineFile;
    }

    public int getCorruptLineCount()
    {
        return corruptLineCount;
    }

    @Override
    public synchronized void append(TelemetryEnvelope event) throws IOException
    {
        appendRecord(new PersistedTimelineRecord(
            TelemetrySchema.MVP_SCHEMA_VERSION,
            TimelineRecordType.TELEMETRY_EVENT,
            event.getSessionId(),
            null,
            TelemetryJson.toJsonLine(Objects.requireNonNull(event, "event"))));
    }

    @Override
    public synchronized void appendActivityMarker(ActivityMarker marker) throws IOException
    {
        appendRecord(new PersistedTimelineRecord(
            TelemetrySchema.MVP_SCHEMA_VERSION,
            TimelineRecordType.ACTIVITY_MARKER,
            sessionId,
            marker.getActivityId().getValue(),
            gson.toJson(PersistedActivityMarker.from(Objects.requireNonNull(marker, "marker")))));
    }

    @Override
    public synchronized void appendOpportunityMarker(OpportunityMarker marker) throws IOException
    {
        appendRecord(new PersistedTimelineRecord(
            TelemetrySchema.MVP_SCHEMA_VERSION,
            TimelineRecordType.OPPORTUNITY_MARKER,
            sessionId,
            marker.getActivityId().getValue(),
            gson.toJson(PersistedOpportunityMarker.from(Objects.requireNonNull(marker, "marker")))));
    }

    @Override
    public synchronized CompletedActivityTimeline readActivityTimeline(ActivityId activityId) throws IOException
    {
        final ActivityId normalizedActivityId = Objects.requireNonNull(activityId, "activityId");
        final List<TimelineRecord> records = readActivityRecords(normalizedActivityId);
        final List<TelemetryEnvelope> telemetryEvents = new ArrayList<>();
        final List<ActivityMarker> activityMarkers = new ArrayList<>();
        final List<OpportunityMarker> opportunityMarkers = new ArrayList<>();
        for (TimelineRecord record : records)
        {
            switch (record.getType())
            {
                case TELEMETRY_EVENT:
                    telemetryEvents.add(record.getTelemetryEvent());
                    break;
                case ACTIVITY_MARKER:
                    activityMarkers.add(record.getActivityMarker());
                    break;
                case OPPORTUNITY_MARKER:
                    opportunityMarkers.add(record.getOpportunityMarker());
                    break;
                default:
                    throw new IllegalStateException("Unsupported timeline record type: " + record.getType());
            }
        }
        return new CompletedActivityTimeline(normalizedActivityId, telemetryEvents, activityMarkers, opportunityMarkers);
    }

    @Override
    public synchronized List<TimelineRecord> readActivityRecords(ActivityId activityId) throws IOException
    {
        ensureSchemaVersionFile();
        final ActivityId normalizedActivityId = Objects.requireNonNull(activityId, "activityId");
        final List<TimelineRecord> allRecords = readAllRecords();
        final List<ActivityMarker> markers = allRecords.stream()
            .filter(record -> record.getType() == TimelineRecordType.ACTIVITY_MARKER)
            .map(TimelineRecord::getActivityMarker)
            .filter(marker -> marker.getActivityId().equals(normalizedActivityId))
            .collect(Collectors.toList());
        if (markers.isEmpty())
        {
            return Collections.emptyList();
        }

        final long startMillis = markers.get(0).getTime().getWallTimeMillis();
        final long endMillis = markers.get(markers.size() - 1).getTime().getWallTimeMillis();
        final List<TimelineRecord> filtered = new ArrayList<>();
        for (TimelineRecord record : allRecords)
        {
            switch (record.getType())
            {
                case TELEMETRY_EVENT:
                    final long eventMillis = record.getTime().getWallTimeMillis();
                    if (eventMillis >= startMillis && eventMillis <= endMillis)
                    {
                        filtered.add(record);
                    }
                    break;
                case ACTIVITY_MARKER:
                case OPPORTUNITY_MARKER:
                    if (normalizedActivityId.equals(record.getActivityId()))
                    {
                        filtered.add(record);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unsupported timeline record type: " + record.getType());
            }
        }
        filtered.sort(Comparator.comparing(record -> record.getTime().getWallTimeMillis()));
        return Collections.unmodifiableList(filtered);
    }

    @Override
    public synchronized void close() throws IOException
    {
        writer.close();
    }

    private void appendRecord(PersistedTimelineRecord record) throws IOException
    {
        ensureSchemaVersionFile();
        writer.write(gson.toJson(record));
        writer.newLine();
        writer.flush();
    }

    private List<TimelineRecord> readAllRecords() throws IOException
    {
        corruptLineCount = 0;
        final List<TimelineRecord> records = new ArrayList<>();
        try (Stream<Path> fileStream = Files.list(dataPaths.getTimelinesDirectory()))
        {
            final List<Path> files = fileStream
                .filter(path -> path.getFileName().toString().endsWith(".jsonl"))
                .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                .collect(Collectors.toList());

            for (Path file : files)
            {
                for (String line : Files.readAllLines(file, StandardCharsets.UTF_8))
                {
                    if (line.trim().isEmpty())
                    {
                        continue;
                    }
                    try
                    {
                        records.add(readRecord(line));
                    }
                    catch (IllegalArgumentException ex)
                    {
                        if (ex.getMessage() != null && ex.getMessage().startsWith("Unsupported telemetry schema version"))
                        {
                            throw ex;
                        }
                        corruptLineCount++;
                    }
                    catch (JsonParseException ex)
                    {
                        corruptLineCount++;
                    }
                }
            }
        }
        return records;
    }

    private TimelineRecord readRecord(String line)
    {
        final PersistedTimelineRecord persisted = gson.fromJson(line, PersistedTimelineRecord.class);
        if (persisted == null)
        {
            throw new IllegalArgumentException("Timeline record must not be null");
        }
        TelemetrySchema.requireSupported(persisted.schemaVersion);
        switch (persisted.recordType)
        {
            case TELEMETRY_EVENT:
                return TimelineRecord.telemetry(TelemetryJson.fromJsonLine(persisted.payloadJson));
            case ACTIVITY_MARKER:
                return TimelineRecord.activityMarker(gson.fromJson(persisted.payloadJson, PersistedActivityMarker.class).toActivityMarker());
            case OPPORTUNITY_MARKER:
                return TimelineRecord.opportunityMarker(gson.fromJson(persisted.payloadJson, PersistedOpportunityMarker.class).toOpportunityMarker());
            default:
                throw new IllegalArgumentException("Unsupported record type: " + persisted.recordType);
        }
    }

    private void ensureSchemaVersionFile() throws IOException
    {
        final Path schemaFile = dataPaths.getSchemaVersionFile();
        if (Files.notExists(schemaFile))
        {
            Files.write(
                schemaFile,
                Collections.singletonList(gson.toJson(new PersistedSchemaVersion(TelemetrySchema.MVP_SCHEMA_VERSION))),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
            return;
        }

        final String json = new String(Files.readAllBytes(schemaFile), StandardCharsets.UTF_8);
        final PersistedSchemaVersion persistedSchemaVersion = gson.fromJson(json, PersistedSchemaVersion.class);
        if (persistedSchemaVersion == null)
        {
            throw new IllegalArgumentException("schema-version.json must not be null");
        }
        TelemetrySchema.requireSupported(persistedSchemaVersion.schemaVersion);
    }

    private static String requireText(String value, String fieldName)
    {
        final String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static final class PersistedTimelineRecord
    {
        private final int schemaVersion;
        private final TimelineRecordType recordType;
        private final String sessionId;
        private final String activityId;
        private final String payloadJson;

        private PersistedTimelineRecord(int schemaVersion, TimelineRecordType recordType, String sessionId, String activityId, String payloadJson)
        {
            this.schemaVersion = schemaVersion;
            this.recordType = recordType;
            this.sessionId = sessionId;
            this.activityId = activityId;
            this.payloadJson = payloadJson;
        }
    }

    private static final class PersistedSchemaVersion
    {
        private final int schemaVersion;

        private PersistedSchemaVersion(int schemaVersion)
        {
            this.schemaVersion = schemaVersion;
        }
    }

    private static final class PersistedActivityMarker
    {
        private String markerId;
        private String activityId;
        private String activityType;
        private String markerType;
        private EventTime time;
        private java.util.Map<String, String> metadata;

        private static PersistedActivityMarker from(ActivityMarker marker)
        {
            final PersistedActivityMarker persisted = new PersistedActivityMarker();
            persisted.markerId = marker.getMarkerId();
            persisted.activityId = marker.getActivityId().getValue();
            persisted.activityType = marker.getActivityType().name();
            persisted.markerType = marker.getMarkerType();
            persisted.time = marker.getTime();
            persisted.metadata = marker.getMetadata();
            return persisted;
        }

        private ActivityMarker toActivityMarker()
        {
            return new ActivityMarker(
                markerId,
                ActivityId.of(activityId),
                ActivityType.valueOf(activityType),
                markerType,
                time,
                metadata);
        }
    }

    private static final class PersistedOpportunityMarker
    {
        private String markerId;
        private String opportunityInstanceId;
        private String activityId;
        private String opportunityType;
        private String status;
        private EventTime time;
        private java.util.Map<String, String> context;
        private List<OpportunityEvidence> evidence;

        private static PersistedOpportunityMarker from(OpportunityMarker marker)
        {
            final PersistedOpportunityMarker persisted = new PersistedOpportunityMarker();
            persisted.markerId = marker.getMarkerId();
            persisted.opportunityInstanceId = marker.getOpportunityInstanceId();
            persisted.activityId = marker.getActivityId().getValue();
            persisted.opportunityType = marker.getOpportunityType();
            persisted.status = marker.getStatus().name();
            persisted.time = marker.getTime();
            persisted.context = marker.getContext();
            persisted.evidence = marker.getEvidence();
            return persisted;
        }

        private OpportunityMarker toOpportunityMarker()
        {
            return new OpportunityMarker(
                markerId,
                opportunityInstanceId,
                ActivityId.of(activityId),
                opportunityType,
                OpportunityStatus.valueOf(status),
                time,
                context,
                evidence);
        }
    }
}
