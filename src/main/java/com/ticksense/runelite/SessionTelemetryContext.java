package com.ticksense.runelite;

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
        this.sessionId = RuneliteTexts.requireText(sessionId, "sessionId");
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
        return RuneliteTexts.requireText(sourceEventType, "sourceEventType") + "-" + eventSequence.incrementAndGet();
    }

    private static String newSessionId()
    {
        return "runelite-" + UUID.randomUUID();
    }
}
