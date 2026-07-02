package com.ticksense.activities.construction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ConstructionVerificationDecision
{
    private static final ConstructionVerificationDecision CURRENT = partiallyVerified(
        "oak-larder",
        "2026-07-03",
        listOf(
            "Approved method scope is limited to oak larders because the roadmap example and current registry values target one narrow build/remove flow.",
            "Official RuneLite ObjectID constants in the pinned runelite-api dependency verify oak larder space 15403 plus built oak larder variants 13565, 13566, and 13567.",
            "Official RuneLite ItemID constants in the pinned runelite-api dependency verify oak plank 8778, hammer 2347, saw 8794, crystal saw 9625, and Amy's saw offhand 29774 as candidate inventory evidence for the method.",
            "Official RuneLite AnimationID constants in the pinned runelite-api dependency verify Construction build animations 3676 and 8912 as candidate build confirmation evidence.",
            "Official RuneLite WidgetID constants in the pinned runelite-api dependency verify bank widget groups 12 and 15 as reusable banking evidence categories for Construction refill flows."
        ),
        listOf(
            "No source-owned normalized construction replay fixture currently proves menu-open timing, build click, remove click, or construction-widget confirmation evidence for oak larders.",
            "No source-owned normalized construction replay fixture currently proves inventory delta, Construction XP confirmation, or servant-mediated refill evidence for the approved method.",
            "Construction widget IDs remain content-sensitive and must be captured from sanitized normalized telemetry before normal Construction strategy enablement is allowed."
        ),
        listOf(
            "Observe menus and widgets only; do not mutate entries, automate clicks, or infer widget IDs from hearsay.",
            "Refresh the fixture and registry comments together after OSRS or RuneLite updates that affect POH Construction flows."
        ));

    private final Status status;
    private final String methodName;
    private final String verifiedOnDate;
    private final List<String> verifiedEvidence;
    private final List<String> blockers;
    private final List<String> notes;

    private ConstructionVerificationDecision(
        Status status,
        String methodName,
        String verifiedOnDate,
        List<String> verifiedEvidence,
        List<String> blockers,
        List<String> notes)
    {
        this.status = Objects.requireNonNull(status, "status");
        this.methodName = normalizedValue(methodName, "methodName");
        this.verifiedOnDate = normalizedValue(verifiedOnDate, "verifiedOnDate");
        this.verifiedEvidence = immutableCopy(verifiedEvidence, "verifiedEvidence");
        this.blockers = immutableCopy(blockers, "blockers");
        this.notes = immutableCopy(notes, "notes");
    }

    public static ConstructionVerificationDecision current()
    {
        return CURRENT;
    }

    public static ConstructionVerificationDecision verified(
        String methodName,
        String verifiedOnDate,
        List<String> verifiedEvidence,
        List<String> notes)
    {
        return new ConstructionVerificationDecision(
            Status.VERIFIED,
            methodName,
            verifiedOnDate,
            verifiedEvidence,
            Collections.emptyList(),
            notes);
    }

    public static ConstructionVerificationDecision partiallyVerified(
        String methodName,
        String verifiedOnDate,
        List<String> verifiedEvidence,
        List<String> blockers,
        List<String> notes)
    {
        return new ConstructionVerificationDecision(
            Status.PARTIALLY_VERIFIED,
            methodName,
            verifiedOnDate,
            verifiedEvidence,
            blockers,
            notes);
    }

    public static ConstructionVerificationDecision blocked(
        String methodName,
        String verifiedOnDate,
        List<String> blockers,
        List<String> notes)
    {
        return new ConstructionVerificationDecision(
            Status.BLOCKED,
            methodName,
            verifiedOnDate,
            Collections.emptyList(),
            blockers,
            notes);
    }

    public Status getStatus()
    {
        return status;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public String getVerifiedOnDate()
    {
        return verifiedOnDate;
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

    private static List<String> listOf(String... values)
    {
        final List<String> list = new ArrayList<>(values.length);
        Collections.addAll(list, values);
        return list;
    }

    public enum Status
    {
        VERIFIED,
        PARTIALLY_VERIFIED,
        BLOCKED
    }
}
