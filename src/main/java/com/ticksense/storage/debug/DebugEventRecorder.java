package com.ticksense.storage.debug;

import com.google.gson.Gson;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryJson;
import com.ticksense.telemetry.TelemetrySink;
import com.ticksense.telemetry.SessionIdProvider;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public final class DebugEventRecorder implements TelemetrySink, AutoCloseable
{
    private static final long BYTES_PER_MEGABYTE = 1024L * 1024L;

    private final Path debugDirectory;
    private final SessionIdProvider sessionIdProvider;
    private final Gson gson = new Gson();

    private BufferedWriter writer;
    private Path sessionFile;
    private DebugLimits debugLimits = DebugLimits.disabled();
    private long bytesWritten;
    private boolean active;
    private boolean warnedFailure;

    @Inject
    DebugEventRecorder(SessionIdProvider sessionIdProvider)
    {
        this(defaultDebugDirectory(), sessionIdProvider);
    }

    DebugEventRecorder(Path debugDirectory, SessionIdProvider sessionIdProvider)
    {
        this.debugDirectory = Objects.requireNonNull(debugDirectory, "debugDirectory");
        this.sessionIdProvider = Objects.requireNonNull(sessionIdProvider, "sessionIdProvider");
    }

    public synchronized void startSession(boolean enabled, int maxDebugFileSizeMb, int maxDebugSessions)
    {
        close();
        warnedFailure = false;
        if (!enabled)
        {
            return;
        }

        debugLimits = DebugLimits.fromConfig(maxDebugFileSizeMb, maxDebugSessions);

        try
        {
            Files.createDirectories(debugDirectory);
            sessionFile = debugDirectory.resolve(sessionFileName());
            writer = Files.newBufferedWriter(
                sessionFile,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
            bytesWritten = 0L;
            active = true;
            applyRetention();
        }
        catch (IOException ex)
        {
            disableForFailure("Failed to start debug event recorder", ex);
        }
    }

    public synchronized boolean isActive()
    {
        return active;
    }

    @Override
    public synchronized void accept(TelemetryEnvelope envelope)
    {
        if (!active || writer == null)
        {
            return;
        }

        final String telemetryJson = TelemetryJson.toJsonLine(envelope);
        final DebugEventRecord record = DebugEventRecord.normalizedTelemetry(
            envelope.getSessionId(),
            envelope.getEvent().getTags().getOrDefault("source", envelope.getEvent().getType()),
            envelope.getEvent().getTime(),
            telemetryJson);
        final String jsonLine = gson.toJson(record) + System.lineSeparator();
        final byte[] lineBytes = jsonLine.getBytes(StandardCharsets.UTF_8);
        if (debugLimits.exceedsMaxFileBytes(bytesWritten, lineBytes.length))
        {
            close();
            return;
        }

        try
        {
            writer.write(jsonLine);
            writer.flush();
            bytesWritten += lineBytes.length;
            Files.setLastModifiedTime(sessionFile, FileTime.fromMillis(System.currentTimeMillis()));
        }
        catch (IOException ex)
        {
            disableForFailure("Failed to write debug telemetry to " + sessionFile, ex);
        }
    }

    @Override
    public synchronized void close()
    {
        if (writer != null)
        {
            try
            {
                writer.close();
            }
            catch (IOException ex)
            {
                if (!warnedFailure)
                {
                    warnedFailure = true;
                    log.warn("Failed to close debug event recorder for {}", sessionFile, ex);
                }
            }
        }
        writer = null;
        sessionFile = null;
        bytesWritten = 0L;
        active = false;
    }

    private void applyRetention() throws IOException
    {
        try (Stream<Path> fileStream = Files.list(debugDirectory))
        {
            final List<Path> debugFiles = fileStream
                .filter(path -> path.getFileName().toString().endsWith(".jsonl"))
                .sorted(Comparator.comparing((Path path) -> path.getFileName().toString()).reversed())
                .collect(Collectors.toCollection(ArrayList::new));

            for (int i = debugLimits.getMaxSessions(); i < debugFiles.size(); i++)
            {
                Files.deleteIfExists(debugFiles.get(i));
            }
        }
    }

    private String sessionFileName()
    {
        return "session-" + System.currentTimeMillis() + "-" + sessionIdProvider.getSessionId() + ".jsonl";
    }

    private void disableForFailure(String message, IOException ex)
    {
        if (!warnedFailure)
        {
            warnedFailure = true;
            log.warn("{} at {}", message, debugDirectory, ex);
        }
        close();
    }

    private static Path defaultDebugDirectory()
    {
        return Paths.get(System.getProperty("user.home"), ".runelite", "ticksense", "debug");
    }

    private static final class DebugLimits
    {
        private final long maxFileBytes;
        private final int maxSessions;

        private DebugLimits(long maxFileBytes, int maxSessions)
        {
            this.maxFileBytes = maxFileBytes;
            this.maxSessions = maxSessions;
        }

        private static DebugLimits disabled()
        {
            return new DebugLimits(BYTES_PER_MEGABYTE, 1);
        }

        private static DebugLimits fromConfig(int maxDebugFileSizeMb, int maxDebugSessions)
        {
            return new DebugLimits(
                Math.max(1L, (long) maxDebugFileSizeMb) * BYTES_PER_MEGABYTE,
                Math.max(1, maxDebugSessions));
        }

        private boolean exceedsMaxFileBytes(long currentBytes, int nextBytes)
        {
            return currentBytes + nextBytes > maxFileBytes;
        }

        private int getMaxSessions()
        {
            return maxSessions;
        }
    }
}
