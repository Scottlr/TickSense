package com.ticksense.activities.construction;

import com.ticksense.activities.VerificationTexts;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ConstructionVerificationDecision
{
    private static final ConstructionVerificationDecision CURRENT = verified(
        "oak-larder",
        "2026-07-03",
        VerificationTexts.listOf(
            "Approved method scope is limited to oak larders because the roadmap example and current registry values target one narrow build/remove flow.",
            "Official RuneLite ObjectID constants in the pinned runelite-api dependency verify oak larder space 15403 plus built oak larder variants 13565, 13566, and 13567.",
            "Official RuneLite ItemID constants in the pinned runelite-api dependency verify oak plank 8778, hammer 2347, saw 8794, crystal saw 9625, and Amy's saw offhand 29774 as candidate inventory evidence for the method.",
            "Official RuneLite AnimationID constants in the pinned runelite-api dependency verify Construction build animations 3676 and 8912 as candidate build confirmation evidence.",
            "Official RuneLite WidgetID constants in the pinned runelite-api dependency verify bank widget groups 12 and 15 as reusable banking evidence categories for Construction refill flows.",
            "The source-owned normalized verification fixture src/test/resources/replays/construction-basic.jsonl proves menu-open timing, build click, construction-widget confirmation, build animation, oak plank inventory consumption, Construction XP gain, remove click, rebuilt object transition, and bank-open evidence for the approved oak-larder flow."
        ),
        VerificationTexts.listOf(
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
        this.methodName = VerificationTexts.normalizedValue(methodName, "methodName");
        this.verifiedOnDate = VerificationTexts.normalizedValue(verifiedOnDate, "verifiedOnDate");
        this.verifiedEvidence = VerificationTexts.immutableCopy(verifiedEvidence, "verifiedEvidence");
        this.blockers = VerificationTexts.immutableCopy(blockers, "blockers");
        this.notes = VerificationTexts.immutableCopy(notes, "notes");
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

    public enum Status
    {
        VERIFIED,
        PARTIALLY_VERIFIED,
        BLOCKED
    }
}
