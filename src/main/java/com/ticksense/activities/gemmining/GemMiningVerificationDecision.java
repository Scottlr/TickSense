package com.ticksense.activities.gemmining;

import com.ticksense.common.TextValues;
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
        this.verifiedEvidence = TextValues.immutableTextList(verifiedEvidence, "verifiedEvidence");
        this.blockers = TextValues.immutableTextList(blockers, "blockers");
        this.notes = TextValues.immutableTextList(notes, "notes");
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
    public enum Status
    {
        VERIFIED,
        PARTIALLY_VERIFIED,
        BLOCKED
    }
}
