package com.ticksense.activities.vardorvis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class VardorvisVerificationDecision
{
    private static final VardorvisVerificationDecision CURRENT = partiallyVerified(
        "2026-07-03",
        listOf(
            "boss-presence",
            "head-presence"
        ),
        listOf(
            "Official RuneLite NpcID constants in the pinned runelite-api dependency verify Vardorvis boss variants 12223, 12224, 12228, 12425, 12426, and 13656.",
            "Official RuneLite NpcID constants in the pinned runelite-api dependency verify the detached Vardorvis head NPC 12226 as a source-owned primitive cue."
        ),
        listOf(
            "No source-owned normalized Vardorvis replay fixture currently proves ranged-head projectile IDs or their timing reliability.",
            "No source-owned normalized Vardorvis replay fixture currently proves blood-splat graphics, axe projectiles/graphics, prayer-response evidence, or damage attribution during mechanic windows.",
            "No source-owned normalized Vardorvis replay fixture currently proves arena/region evidence for constraining normal reports to the intended boss context."
        ),
        listOf(
            "Observe projectiles and graphics only; do not recolor, replace, or otherwise alter live boss visuals.",
            "Normal Vardorvis mechanic reports must stay disabled until projectile, graphic, animation, and damage evidence are source-owned and reviewed."
        ));

    private final Status status;
    private final String verifiedOnDate;
    private final List<String> verifiedMechanics;
    private final List<String> evidence;
    private final List<String> unresolvedMechanics;
    private final List<String> notes;

    private VardorvisVerificationDecision(
        Status status,
        String verifiedOnDate,
        List<String> verifiedMechanics,
        List<String> evidence,
        List<String> unresolvedMechanics,
        List<String> notes)
    {
        this.status = Objects.requireNonNull(status, "status");
        this.verifiedOnDate = normalizedValue(verifiedOnDate, "verifiedOnDate");
        this.verifiedMechanics = immutableCopy(verifiedMechanics, "verifiedMechanics");
        this.evidence = immutableCopy(evidence, "evidence");
        this.unresolvedMechanics = immutableCopy(unresolvedMechanics, "unresolvedMechanics");
        this.notes = immutableCopy(notes, "notes");
    }

    public static VardorvisVerificationDecision current()
    {
        return CURRENT;
    }

    public static VardorvisVerificationDecision verified(
        String verifiedOnDate,
        List<String> verifiedMechanics,
        List<String> evidence,
        List<String> notes)
    {
        return new VardorvisVerificationDecision(
            Status.VERIFIED,
            verifiedOnDate,
            verifiedMechanics,
            evidence,
            Collections.emptyList(),
            notes);
    }

    public static VardorvisVerificationDecision partiallyVerified(
        String verifiedOnDate,
        List<String> verifiedMechanics,
        List<String> evidence,
        List<String> unresolvedMechanics,
        List<String> notes)
    {
        return new VardorvisVerificationDecision(
            Status.PARTIALLY_VERIFIED,
            verifiedOnDate,
            verifiedMechanics,
            evidence,
            unresolvedMechanics,
            notes);
    }

    public static VardorvisVerificationDecision blocked(
        String verifiedOnDate,
        List<String> unresolvedMechanics,
        List<String> notes)
    {
        return new VardorvisVerificationDecision(
            Status.BLOCKED,
            verifiedOnDate,
            Collections.emptyList(),
            Collections.emptyList(),
            unresolvedMechanics,
            notes);
    }

    public Status getStatus()
    {
        return status;
    }

    public String getVerifiedOnDate()
    {
        return verifiedOnDate;
    }

    public List<String> getVerifiedMechanics()
    {
        return verifiedMechanics;
    }

    public List<String> getEvidence()
    {
        return evidence;
    }

    public List<String> getUnresolvedMechanics()
    {
        return unresolvedMechanics;
    }

    public List<String> getNotes()
    {
        return notes;
    }

    public boolean allowsNormalReports()
    {
        return status == Status.VERIFIED;
    }

    public boolean allowsMechanicReports(String mechanic)
    {
        final String normalizedMechanic = normalizedValue(mechanic, "mechanic");
        return allowsNormalReports() && verifiedMechanics.contains(normalizedMechanic);
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
