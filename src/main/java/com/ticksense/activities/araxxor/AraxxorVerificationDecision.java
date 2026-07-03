package com.ticksense.activities.araxxor;

import com.ticksense.activities.VerificationTexts;
import java.util.List;
import java.util.Objects;

public final class AraxxorVerificationDecision
{
    private static final AraxxorVerificationDecision CURRENT = blocked(
        "2026-07-03",
        VerificationTexts.listOf(
            "Official RuneLite NpcID constants confirm Araxxor boss IDs 13668 and 13669.",
            "Official RuneLite NpcID constants confirm named spider IDs 13671, 13673, 13675, and 13680.",
            "Araxxor fixture placeholders are reserved under src/test/resources/replays/ for future source-owned normalized captures."
        ),
        VerificationTexts.listOf(
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
        this.verifiedOnDate = VerificationTexts.normalizedValue(verifiedOnDate, "verifiedOnDate");
        this.evidence = VerificationTexts.immutableCopy(evidence, "evidence");
        this.unresolvedQuestions = VerificationTexts.immutableCopy(unresolvedQuestions, "unresolvedQuestions");
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

    public enum Status
    {
        VERIFIED,
        PARTIALLY_VERIFIED,
        BLOCKED
    }
}
