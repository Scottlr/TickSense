package com.ticksense.activities.araxxor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class AraxxorVerificationDecision
{
    private static final AraxxorVerificationDecision CURRENT = blocked(
        "2026-07-03",
        Arrays.asList(
            "Official RuneLite NpcID constants confirm Araxxor boss IDs 13668 and 13669.",
            "Official RuneLite NpcID constants confirm named spider IDs 13671, 13673, 13675, and 13680.",
            "Araxxor fixture placeholders are reserved under src/test/resources/replays/ for future source-owned normalized captures."
        ),
        Arrays.asList(
            "No source-owned normalized Araxxor replay fixture currently proves spider spawn or spider availability evidence.",
            "No source-owned normalized Araxxor replay fixture currently proves attack click, interaction-changed, or damage evidence during a spider window.",
            "Teleport-mid-kill termination evidence is still unverified and must be captured in sanitized normalized telemetry before Araxxor can move beyond BLOCKED."
        ));

    private final Status status;
    private final String verifiedOnDate;
    private final List<String> evidence;
    private final List<String> unresolvedQuestions;

    private AraxxorVerificationDecision(
        Status status,
        String verifiedOnDate,
        List<String> evidence,
        List<String> unresolvedQuestions)
    {
        this.status = Objects.requireNonNull(status, "status");
        this.verifiedOnDate = normalizedValue(verifiedOnDate, "verifiedOnDate");
        this.evidence = immutableCopy(evidence, "evidence");
        this.unresolvedQuestions = immutableCopy(unresolvedQuestions, "unresolvedQuestions");
    }

    public static AraxxorVerificationDecision verified(
        String verifiedOnDate,
        List<String> evidence,
        List<String> unresolvedQuestions)
    {
        return new AraxxorVerificationDecision(Status.VERIFIED, verifiedOnDate, evidence, unresolvedQuestions);
    }

    public static AraxxorVerificationDecision partiallyVerified(
        String verifiedOnDate,
        List<String> evidence,
        List<String> unresolvedQuestions)
    {
        return new AraxxorVerificationDecision(Status.PARTIALLY_VERIFIED, verifiedOnDate, evidence, unresolvedQuestions);
    }

    public static AraxxorVerificationDecision blocked(
        String verifiedOnDate,
        List<String> evidence,
        List<String> unresolvedQuestions)
    {
        return new AraxxorVerificationDecision(Status.BLOCKED, verifiedOnDate, evidence, unresolvedQuestions);
    }

    public static AraxxorVerificationDecision current()
    {
        return CURRENT;
    }

    public Status getStatus()
    {
        return status;
    }

    public String getVerifiedOnDate()
    {
        return verifiedOnDate;
    }

    public List<String> getEvidence()
    {
        return evidence;
    }

    public List<String> getUnresolvedQuestions()
    {
        return unresolvedQuestions;
    }

    public boolean allowsNormalStrategyEnablement()
    {
        return status == Status.VERIFIED;
    }

    private static List<String> immutableCopy(List<String> values, String fieldName)
    {
        Objects.requireNonNull(values, fieldName);
        final List<String> copy = new ArrayList<>(values.size());
        for (String value : values)
        {
            copy.add(normalizedValue(value, fieldName + " entry"));
        }
        return Collections.unmodifiableList(copy);
    }

    private static String normalizedValue(String value, String fieldName)
    {
        final String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    public enum Status
    {
        VERIFIED,
        PARTIALLY_VERIFIED,
        BLOCKED
    }
}
