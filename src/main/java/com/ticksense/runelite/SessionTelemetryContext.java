package com.ticksense.runelite;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class SessionTelemetryContext
{
    private final AtomicLong eventSequence = new AtomicLong();

    private volatile String sessionId = newSessionId();

    @Inject
    SessionTelemetryContext()
    {
    }

    public SessionTelemetryContext(String sessionId)
    {
        this.sessionId = requireText(sessionId, "sessionId");
    }

    public synchronized void resetSession()
    {
        sessionId = newSessionId();
        eventSequence.set(0L);
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public String nextEventId(String sourceEventType)
    {
        return requireText(sourceEventType, "sourceEventType") + "-" + eventSequence.incrementAndGet();
    }

    private static String newSessionId()
    {
        return "runelite-" + UUID.randomUUID();
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
}
