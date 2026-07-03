package com.ticksense.runelite;

import com.ticksense.common.TextValues;
import com.ticksense.telemetry.SessionIdProvider;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class SessionTelemetryContext implements SessionIdProvider
{
    private final AtomicLong eventSequence = new AtomicLong();

    private volatile String sessionId = newSessionId();

    @Inject
    SessionTelemetryContext()
    {
    }

    public SessionTelemetryContext(String sessionId)
    {
        this.sessionId = TextValues.requireText(sessionId, "sessionId");
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
        return TextValues.requireText(sourceEventType, "sourceEventType") + "-" + eventSequence.incrementAndGet();
    }

    private static String newSessionId()
    {
        return "runelite-" + UUID.randomUUID();
    }
}
