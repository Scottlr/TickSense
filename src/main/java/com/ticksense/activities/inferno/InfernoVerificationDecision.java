package com.ticksense.activities.inferno;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class InfernoVerificationDecision
{
    private static final InfernoVerificationDecision CURRENT = partiallyVerified(
        "2026-07-03",
        EvidenceStatus.PARTIALLY_VERIFIED,
        EvidenceStatus.PARTIALLY_VERIFIED,
        EvidenceStatus.BLOCKED,
        EvidenceStatus.PARTIALLY_VERIFIED,
        EvidenceStatus.BLOCKED,
        listOf(
            "Official RuneLite NpcID constants in the pinned runelite-api dependency verify source-owned Inferno NPC IDs for nibblers, bats, blobs, melee units, rangers, magers, Jad variants, and Zuk.",
            "Official RuneLite ItemID constants in the pinned runelite-api dependency verify common Inferno supply items such as prayer potions, super restores, Saradomin brews, cooked karambwans, rune darts, and toxic blowpipe variants."
        ),
        listOf(
            "No source-owned normalized Inferno replay fixture currently proves wave/region boundaries or attempt segmentation.",
            "No source-owned normalized Inferno replay fixture currently proves prayer state/timing evidence safely enough for normal reports.",
            "No source-owned normalized Inferno replay fixture currently proves supply-usage timing or death-timeline evidence in TickSense's schema."
        ),
        listOf(
            "Inferno work must remain retrospective only; do not infer or display live wave solves, prayer calls, or target priorities.",
            "Prayer timing stays disabled until direct prayer-state verification exists in source-owned normalized telemetry."
        ));

    private final Status status;
    private final String verifiedOnDate;
    private final EvidenceStatus waveEvidenceStatus;
    private final EvidenceStatus nibblerEvidenceStatus;
    private final EvidenceStatus prayerEvidenceStatus;
    private final EvidenceStatus supplyEvidenceStatus;
    private final EvidenceStatus deathEvidenceStatus;
    private final List<String> evidence;
    private final List<String> blockers;
    private final List<String> notes;

    private InfernoVerificationDecision(
        Status status,
        String verifiedOnDate,
        EvidenceStatus waveEvidenceStatus,
        EvidenceStatus nibblerEvidenceStatus,
        EvidenceStatus prayerEvidenceStatus,
        EvidenceStatus supplyEvidenceStatus,
        EvidenceStatus deathEvidenceStatus,
        List<String> evidence,
        List<String> blockers,
        List<String> notes)
    {
        this.status = Objects.requireNonNull(status, "status");
        this.verifiedOnDate = normalizedValue(verifiedOnDate, "verifiedOnDate");
        this.waveEvidenceStatus = Objects.requireNonNull(waveEvidenceStatus, "waveEvidenceStatus");
        this.nibblerEvidenceStatus = Objects.requireNonNull(nibblerEvidenceStatus, "nibblerEvidenceStatus");
        this.prayerEvidenceStatus = Objects.requireNonNull(prayerEvidenceStatus, "prayerEvidenceStatus");
        this.supplyEvidenceStatus = Objects.requireNonNull(supplyEvidenceStatus, "supplyEvidenceStatus");
        this.deathEvidenceStatus = Objects.requireNonNull(deathEvidenceStatus, "deathEvidenceStatus");
        this.evidence = immutableCopy(evidence, "evidence");
        this.blockers = immutableCopy(blockers, "blockers");
        this.notes = immutableCopy(notes, "notes");
    }

    public static InfernoVerificationDecision current()
    {
        return CURRENT;
    }

    public static InfernoVerificationDecision verified(
        String verifiedOnDate,
        EvidenceStatus waveEvidenceStatus,
        EvidenceStatus nibblerEvidenceStatus,
        EvidenceStatus prayerEvidenceStatus,
        EvidenceStatus supplyEvidenceStatus,
        EvidenceStatus deathEvidenceStatus,
        List<String> evidence,
        List<String> notes)
    {
        return new InfernoVerificationDecision(
            Status.VERIFIED,
            verifiedOnDate,
            waveEvidenceStatus,
            nibblerEvidenceStatus,
            prayerEvidenceStatus,
            supplyEvidenceStatus,
            deathEvidenceStatus,
            evidence,
            Collections.emptyList(),
            notes);
    }

    public static InfernoVerificationDecision partiallyVerified(
        String verifiedOnDate,
        EvidenceStatus waveEvidenceStatus,
        EvidenceStatus nibblerEvidenceStatus,
        EvidenceStatus prayerEvidenceStatus,
        EvidenceStatus supplyEvidenceStatus,
        EvidenceStatus deathEvidenceStatus,
        List<String> evidence,
        List<String> blockers,
        List<String> notes)
    {
        return new InfernoVerificationDecision(
            Status.PARTIALLY_VERIFIED,
            verifiedOnDate,
            waveEvidenceStatus,
            nibblerEvidenceStatus,
            prayerEvidenceStatus,
            supplyEvidenceStatus,
            deathEvidenceStatus,
            evidence,
            blockers,
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

    public EvidenceStatus getWaveEvidenceStatus()
    {
        return waveEvidenceStatus;
    }

    public EvidenceStatus getNibblerEvidenceStatus()
    {
        return nibblerEvidenceStatus;
    }

    public EvidenceStatus getPrayerEvidenceStatus()
    {
        return prayerEvidenceStatus;
    }

    public EvidenceStatus getSupplyEvidenceStatus()
    {
        return supplyEvidenceStatus;
    }

    public EvidenceStatus getDeathEvidenceStatus()
    {
        return deathEvidenceStatus;
    }

    public List<String> getEvidence()
    {
        return evidence;
    }

    public List<String> getBlockers()
    {
        return blockers;
    }

    public List<String> getNotes()
    {
        return notes;
    }

    public boolean allowsPrayerTimingReports()
    {
        return prayerEvidenceStatus == EvidenceStatus.VERIFIED;
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

    public enum EvidenceStatus
    {
        VERIFIED,
        PARTIALLY_VERIFIED,
        BLOCKED
    }
}
