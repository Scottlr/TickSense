package com.ticksense.activities.gemmining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class GemMiningVerificationDecision
{
    private final Status status;
    private final List<String> verifiedEvidence;
    private final List<String> blockers;
    private final List<String> notes;

    private GemMiningVerificationDecision(
        Status status,
        List<String> verifiedEvidence,
        List<String> blockers,
        List<String> notes)
    {
        this.status = Objects.requireNonNull(status, "status");
        this.verifiedEvidence = immutableCopy(verifiedEvidence, "verifiedEvidence");
        this.blockers = immutableCopy(blockers, "blockers");
        this.notes = immutableCopy(notes, "notes");
    }

    public static GemMiningVerificationDecision verified(List<String> verifiedEvidence, List<String> notes)
    {
        return new GemMiningVerificationDecision(Status.VERIFIED, verifiedEvidence, Collections.emptyList(), notes);
    }

    public static GemMiningVerificationDecision partiallyVerified(
        List<String> verifiedEvidence,
        List<String> blockers,
        List<String> notes)
    {
        return new GemMiningVerificationDecision(Status.PARTIALLY_VERIFIED, verifiedEvidence, blockers, notes);
    }

    public static GemMiningVerificationDecision blocked(List<String> blockers, List<String> notes)
    {
        return new GemMiningVerificationDecision(Status.BLOCKED, Collections.emptyList(), blockers, notes);
    }

    public Status getStatus()
    {
        return status;
    }

    public List<String> getVerifiedEvidence()
    {
        return verifiedEvidence;
    }

    public List<String> getBlockers()
    {
        return blockers;
    }

    public List<String> getNotes()
    {
        return notes;
    }

    public boolean allowsStrategyEnablement()
    {
        return status == Status.VERIFIED;
    }

    private static List<String> immutableCopy(List<String> values, String fieldName)
    {
        Objects.requireNonNull(values, fieldName);
        final List<String> copy = new ArrayList<>(values.size());
        for (String value : values)
        {
            final String normalized = Objects.requireNonNull(value, fieldName + " entry").trim();
            if (normalized.isEmpty())
            {
                throw new IllegalArgumentException(fieldName + " entries must not be blank");
            }
            copy.add(normalized);
        }
        return Collections.unmodifiableList(copy);
    }

    public enum Status
    {
        VERIFIED,
        PARTIALLY_VERIFIED,
        BLOCKED
    }
}
