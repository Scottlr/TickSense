package com.ticksense.activities.inferno;

import com.ticksense.activities.EvidenceStrength;
import com.ticksense.activities.OpportunityDefinition;
import com.ticksense.activities.OpportunityEvidence;
import com.ticksense.activities.OpportunityInstance;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.activities.OpportunityTracker;
import com.ticksense.core.ActivityId;
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.events.DamageTelemetryEvent;
import com.ticksense.telemetry.events.InteractingChangedTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.NpcStateTelemetryEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class InfernoState
{
    static final String OPPORTUNITY_WAVE = "INFERNO_WAVE";
    static final String OPPORTUNITY_NIBBLER_WINDOW = "INFERNO_NIBBLER_WINDOW";
    static final String OPPORTUNITY_PRAYER_WINDOW = "INFERNO_PRAYER_WINDOW";

    private static final OpportunityDefinition WAVE_SPAN = new OpportunityDefinition(
        OPPORTUNITY_WAVE,
        "Inferno wave",
        com.ticksense.core.ActivityType.INFERNO,
        0L,
        Collections.singletonList("Finish the verified wave span"));

    private static final OpportunityDefinition NIBBLER_WINDOW = new OpportunityDefinition(
        OPPORTUNITY_NIBBLER_WINDOW,
        "Inferno nibbler response",
        com.ticksense.core.ActivityType.INFERNO,
        2_400L,
        Collections.singletonList("Engage the verified nibbler"));

    private final InfernoVerificationDecision verificationDecision;
    private final int[] nibblerNpcIds;
    private final int[] waveNpcIds;
    private final int[] supplyItemIds;
    private final int[] verifiedRegionIds;

    private int currentRegionId = -1;
    private ActivityId activeActivityId;
    private OpportunityTracker tracker;
    private PendingWave pendingWave;
    private OpportunityInstance waveSpan;
    private OpportunityInstance nibblerWindow;
    private int waveSpanCount;
    private int nibblerResponseCount;
    private int supplyUseCount;
    private int prayerWindowCount;
    private final List<String> deathTimelineEvidence = new ArrayList<>();

    InfernoState(
        InfernoVerificationDecision verificationDecision,
        int[] nibblerNpcIds,
        int[] waveNpcIds,
        int[] supplyItemIds,
        int[] verifiedRegionIds)
    {
        this.verificationDecision = verificationDecision;
        this.nibblerNpcIds = nibblerNpcIds.clone();
        this.waveNpcIds = waveNpcIds.clone();
        this.supplyItemIds = supplyItemIds.clone();
        this.verifiedRegionIds = verifiedRegionIds.clone();
    }

    boolean allowsStrategyEnablement()
    {
        return verificationDecision.allowsStrategyEnablement();
    }

    boolean allowsPrayerTimingReports()
    {
        return verificationDecision.allowsPrayerTimingReports();
    }

    boolean allowsDeathTimelineEvidence()
    {
        return verificationDecision.allowsDeathTimelineEvidence();
    }

    void updateRegion(WorldLocation location)
    {
        if (location != null)
        {
            currentRegionId = location.getRegionId();
        }
    }

    boolean isVerifiedRegion(int regionId)
    {
        return contains(verifiedRegionIds, regionId);
    }

    boolean canActivateFromWaveNpc(NpcStateTelemetryEvent event)
    {
        return verificationDecision.allowsWaveSpans()
            && isVerifiedRegion(currentRegionId)
            && "SPAWNED".equals(event.getStateChange())
            && contains(waveNpcIds, event.getNpcRef().getId());
    }

    void noteActivationWave(NpcStateTelemetryEvent event)
    {
        pendingWave = new PendingWave(event);
    }

    void startActivity(ActivityId activityId)
    {
        activeActivityId = activityId;
    }

    void ensureTracker(OpportunityTracker nextTracker)
    {
        if (tracker == null)
        {
            tracker = nextTracker;
        }
    }

    void flushActivationDerivedSpans()
    {
        if (pendingWave == null || tracker == null || activeActivityId == null)
        {
            return;
        }
        openWaveSpan(pendingWave.event);
        pendingWave = null;
    }

    void noteWaveNpc(NpcStateTelemetryEvent event)
    {
        if (!verificationDecision.allowsWaveSpans()
            || !"SPAWNED".equals(event.getStateChange())
            || !contains(waveNpcIds, event.getNpcRef().getId())
            || isOpen(waveSpan))
        {
            return;
        }
        openWaveSpan(event);
    }

    void noteNibblerNpc(NpcStateTelemetryEvent event)
    {
        if (!verificationDecision.allowsNibblerWindows()
            || !"SPAWNED".equals(event.getStateChange())
            || !contains(nibblerNpcIds, event.getNpcRef().getId())
            || !isOpen(waveSpan)
            || isOpen(nibblerWindow))
        {
            return;
        }
        nibblerWindow = tracker.start(
            activeActivityId,
            NIBBLER_WINDOW,
            event.getTime(),
            context(
                "nibblerNpcId", String.valueOf(event.getNpcRef().getId()),
                "regionId", String.valueOf(currentRegionId)));
        appendDeathTimeline("Tick " + event.getTime().getGameTick() + ": verified nibbler " + event.getNpcRef().getId() + " opened.");
    }

    void noteNibblerInteraction(InteractingChangedTelemetryEvent event)
    {
        if (!isOpen(nibblerWindow)
            || event.getActorRef().getType() != EntityRef.Type.LOCAL_PLAYER
            || event.getInteractingRef().getType() != EntityRef.Type.NPC
            || !contains(nibblerNpcIds, event.getInteractingRef().getId()))
        {
            return;
        }
        tracker.complete(
            nibblerWindow.getInstanceId(),
            event.getTime(),
            Collections.singletonList(new OpportunityEvidence(
                event.getTime(),
                "interacting.changed",
                EvidenceStrength.CONFIRMING,
                "Local player engaged the verified Inferno nibbler.")));
        nibblerResponseCount++;
        appendDeathTimeline("Tick " + event.getTime().getGameTick() + ": local player engaged a verified nibbler.");
        nibblerWindow = null;
    }

    void noteSupplyUsage(InventoryDeltaTelemetryEvent event)
    {
        if (!verificationDecision.allowsSupplyUsageTracking())
        {
            return;
        }
        for (InventoryDeltaTelemetryEvent.ItemDelta delta : event.getDeltas())
        {
            if (contains(supplyItemIds, delta.getBeforeItemId()) && delta.getAfterQuantity() < delta.getBeforeQuantity())
            {
                supplyUseCount += (delta.getBeforeQuantity() - delta.getAfterQuantity());
                appendDeathTimeline("Tick " + event.getTime().getGameTick() + ": used verified supply item " + delta.getBeforeItemId() + ".");
            }
        }
    }

    void noteDamage(DamageTelemetryEvent event)
    {
        if (event.getTargetRef().getType() != EntityRef.Type.LOCAL_PLAYER || !verificationDecision.allowsDeathTimelineEvidence())
        {
            return;
        }
        appendDeathTimeline("Tick " + event.getTime().getGameTick() + ": local player took " + event.getAmount() + " damage.");
    }

    boolean isVerifiedPlayerDeath(DamageTelemetryEvent event)
    {
        return verificationDecision.allowsDeathTimelineEvidence()
            && event.getTargetRef().getType() == EntityRef.Type.LOCAL_PLAYER
            && event.getHealthRatio() <= 0;
    }

    void completeWaveSpan(EventTime time, String detail)
    {
        if (isOpen(waveSpan))
        {
            tracker.complete(
                waveSpan.getInstanceId(),
                time,
                Collections.singletonList(new OpportunityEvidence(time, "region.instance", EvidenceStrength.CONFIRMING, detail)));
            waveSpanCount++;
            waveSpan = null;
        }
        if (isOpen(nibblerWindow))
        {
            tracker.cancel(
                nibblerWindow.getInstanceId(),
                time,
                Collections.singletonList(new OpportunityEvidence(time, "region.instance", EvidenceStrength.CONFIRMING, detail)));
            nibblerWindow = null;
        }
    }

    Map<String, String> snapshotAttributes()
    {
        final Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("verificationStatus", verificationDecision.getStatus().name());
        attributes.put("waveSpanCount", String.valueOf(waveSpanCount));
        attributes.put("nibblerResponseCount", String.valueOf(nibblerResponseCount));
        attributes.put("supplyUseCount", String.valueOf(supplyUseCount));
        attributes.put("prayerWindowCount", String.valueOf(prayerWindowCount));
        attributes.put("prayerEvidenceStatus", verificationDecision.getPrayerEvidenceStatus().name());
        attributes.put("deathTimelineEventCount", String.valueOf(deathTimelineEvidence.size()));
        attributes.put("deathTimelineEvidence", String.join(" | ", deathTimelineEvidence));
        return attributes;
    }

    void resetForNextSession()
    {
        currentRegionId = -1;
        activeActivityId = null;
        tracker = null;
        pendingWave = null;
        waveSpan = null;
        nibblerWindow = null;
        waveSpanCount = 0;
        nibblerResponseCount = 0;
        supplyUseCount = 0;
        prayerWindowCount = 0;
        deathTimelineEvidence.clear();
    }

    java.util.List<String> activationEvidence(NpcStateTelemetryEvent event)
    {
        return Arrays.asList(
            "Inferno verification status is " + verificationDecision.getStatus().name() + ".",
            "Verified Inferno region " + currentRegionId + " is active.",
            "Verified wave NPC " + event.getNpcRef().getId() + " opened the Inferno attempt.");
    }

    EventTime activationStartTime(NpcStateTelemetryEvent event)
    {
        return event.getTime();
    }

    private void openWaveSpan(NpcStateTelemetryEvent event)
    {
        if (tracker == null || activeActivityId == null)
        {
            return;
        }
        waveSpan = tracker.start(
            activeActivityId,
            WAVE_SPAN,
            event.getTime(),
            context(
                "waveNpcId", String.valueOf(event.getNpcRef().getId()),
                "regionId", String.valueOf(currentRegionId)));
        appendDeathTimeline("Tick " + event.getTime().getGameTick() + ": verified Inferno wave span opened.");
    }

    private void appendDeathTimeline(String detail)
    {
        if (!verificationDecision.allowsDeathTimelineEvidence())
        {
            return;
        }
        if (deathTimelineEvidence.size() == 5)
        {
            deathTimelineEvidence.remove(0);
        }
        deathTimelineEvidence.add(detail);
    }

    private static Map<String, String> context(String... pairs)
    {
        final Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pairs.length; i += 2)
        {
            values.put(pairs[i], pairs[i + 1]);
        }
        return values;
    }

    private static boolean contains(int[] values, int needle)
    {
        return Arrays.stream(values).anyMatch(value -> value == needle);
    }

    private static boolean isOpen(OpportunityInstance instance)
    {
        return instance != null && instance.getStatus() == OpportunityStatus.OPEN;
    }

    private static final class PendingWave
    {
        private final NpcStateTelemetryEvent event;

        private PendingWave(NpcStateTelemetryEvent event)
        {
            this.event = event;
        }
    }
}
