package com.ticksense.activities;

public interface ActivityDiagnosticSink
{
    void accept(String sessionId, ActivityDiagnostic diagnostic);
}
