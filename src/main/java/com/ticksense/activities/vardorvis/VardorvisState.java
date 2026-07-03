package com.ticksense.activities.vardorvis;

import com.ticksense.activities.ActivityContext;
import com.ticksense.activities.EvidenceStrength;
import com.ticksense.activities.OpportunityDefinition;
import com.ticksense.activities.OpportunityEvidence;
import com.ticksense.activities.OpportunityInstance;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.activities.OpportunityLifecycle;
import com.ticksense.activities.execution.CommonExecutionTrackers;
import com.ticksense.activities.execution.ExecutionTrackerSet;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.DamageTelemetryEvent;
import com.ticksense.telemetry.events.MovementTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import com.ticksense.telemetry.events.ProjectileTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class VardorvisState
{
    static final String MECHANIC_RANGED_HEAD_RESPONSE = "ranged-head-response";
    static final String OPPORTUNITY_RANGED_HEAD_RESPONSE = "VARDORVIS_RANGED_HEAD_RESPONSE";
    static final String OPPORTUNITY_BLOOD_SPLAT_MOVEMENT = "VARDORVIS_BLOOD_SPLAT_MOVEMENT";
    static final String OPPORTUNITY_AXE_DODGE = "VARDORVIS_AXE_DODGE";
    static final String OPPORTUNITY_PRAYER_RESPONSE = "VARDORVIS_PRAYER_RESPONSE";

    private static final OpportunityDefinition RANGED_HEAD_RESPONSE = new OpportunityDefinition(
        OPPORTUNITY_RANGED_HEAD_RESPONSE,
        "Vardorvis ranged head response",
        com.ticksense.core.ActivityType.VARDORVIS,
        2_400L,
        Collections.singletonList("Move or act after the verified ranged-head cue"));

    private final VardorvisVerificationDecision verificationDecision;
    private final int[] bossNpcIds;
    private final int[] headNpcIds;
    private final int[] rangedHeadProjectileIds;
    private final int[] bloodSplatGraphicIds;
    private final int[] axeMechanicIds;
    private final int[] verifiedRegionIds;
    private final ExecutionTrackerSet reusableExecutionTrackers = CommonExecutionTrackers.combatSupport();

    private int currentRegionId = -1;
    private WorldLocation currentPlayerLocation = WorldLocation.unknown();
    private ActivityId activeActivityId;
    private OpportunityLifecycle opportunityLifecycle;
    private PendingProjectile pendingRangedHeadProjectile;
    private OpportunityInstance rangedHeadOpportunity;
    private int rangedHeadResponseCount;
    private int rangedHeadDamageFailures;

    VardorvisState(
        VardorvisVerificationDecision verificationDecision,
        int[] bossNpcIds,
        int[] headNpcIds,
        int[] rangedHeadProjectileIds,
        int[] bloodSplatGraphicIds,
        int[] axeMechanicIds,
        int[] verifiedRegionIds)
    {
        this.verificationDecision = verificationDecision;
        this.bossNpcIds = bossNpcIds.clone();
        this.headNpcIds = headNpcIds.clone();
        this.rangedHeadProjectileIds = rangedHeadProjectileIds.clone();
        this.bloodSplatGraphicIds = bloodSplatGraphicIds.clone();
        this.axeMechanicIds = axeMechanicIds.clone();
        this.verifiedRegionIds = verifiedRegionIds.clone();
    }

    boolean allowsNormalReports()
    {
        return verificationDecision.allowsNormalReports();
    }

    boolean allowsMechanic(String mechanic)
    {
        return verificationDecision.allowsMechanicReports(mechanic);
    }

    void updateRegion(WorldLocation location)
    {
        if (location == null)
        {
            return;
        }
        currentPlayerLocation = location;
        currentRegionId = location.getRegionId();
    }

    boolean isVerifiedRegion(int regionId)
    {
        return contains(verifiedRegionIds, regionId);
    }

    boolean canActivateFromRangedHead(ProjectileTelemetryEvent event)
    {
        return allowsMechanic(MECHANIC_RANGED_HEAD_RESPONSE)
            && isVerifiedRegion(currentRegionId)
            && contains(rangedHeadProjectileIds, event.getProjectileId())
            && isHeadRef(event.getSourceRef())
            && event.getTargetRef().getType() == EntityRef.Type.LOCAL_PLAYER;
    }

    void noteActivationProjectile(ProjectileTelemetryEvent event)
    {
        pendingRangedHeadProjectile = new PendingProjectile(event);
    }

    void startActivity(ActivityId activityId)
    {
        activeActivityId = activityId;
        reusableExecutionTrackers.startActivity(activityId);
    }

    void ensureOpportunityLifecycle(OpportunityLifecycle nextLifecycle)
    {
        if (opportunityLifecycle == null)
        {
            opportunityLifecycle = nextLifecycle;
        }
        reusableExecutionTrackers.ensureOpportunityLifecycle(nextLifecycle);
    }

    void noteReusableExecutionEvent(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        reusableExecutionTrackers.onEvent(context, session, event);
    }

    void flushActivationDerivedOpportunities()
    {
        if (pendingRangedHeadProjectile == null || opportunityLifecycle == null || activeActivityId == null)
        {
            return;
        }
        openRangedHeadOpportunity(pendingRangedHeadProjectile.event);
        pendingRangedHeadProjectile = null;
    }

    void noteProjectile(ProjectileTelemetryEvent event)
    {
        if (!canActivateFromRangedHead(event) || isOpen(rangedHeadOpportunity))
        {
            return;
        }
        openRangedHeadOpportunity(event);
    }

    void noteMovementResponse(MovementTelemetryEvent event)
    {
        if (!isOpen(rangedHeadOpportunity) || event.getEntityRef().getType() != EntityRef.Type.LOCAL_PLAYER)
        {
            return;
        }
        opportunityLifecycle.complete(
            rangedHeadOpportunity.getInstanceId(),
            event.getTime(),
            Collections.singletonList(new OpportunityEvidence(
                event.getTime(),
                "movement.location",
                EvidenceStrength.CONFIRMING,
                "Local player movement responded to the verified Vardorvis ranged-head cue.")));
        rangedHeadResponseCount++;
        rangedHeadOpportunity = null;
    }

    void noteActionResponse(PlayerActionTelemetryEvent event)
    {
        if (!isOpen(rangedHeadOpportunity))
        {
            return;
        }
        opportunityLifecycle.complete(
            rangedHeadOpportunity.getInstanceId(),
            event.getTime(),
            Collections.singletonList(new OpportunityEvidence(
                event.getTime(),
                PlayerActionTelemetryEvent.TYPE,
                EvidenceStrength.CONFIRMING,
                "Player action responded to the verified Vardorvis ranged-head cue.")));
        rangedHeadResponseCount++;
        rangedHeadOpportunity = null;
    }

    void noteDamage(DamageTelemetryEvent event)
    {
        if (!isOpen(rangedHeadOpportunity) || event.getTargetRef().getType() != EntityRef.Type.LOCAL_PLAYER)
        {
            return;
        }
        opportunityLifecycle.fail(
            rangedHeadOpportunity.getInstanceId(),
            event.getTime(),
            Collections.singletonList(new OpportunityEvidence(
                event.getTime(),
                "damage",
                EvidenceStrength.CONFIRMING,
                "Local player took " + event.getAmount() + " damage during the verified Vardorvis ranged-head window.")));
        rangedHeadDamageFailures++;
        rangedHeadOpportunity = null;
    }

    void cancelOpenOpportunities(EventTime endTime, String detail)
    {
        if (opportunityLifecycle == null || activeActivityId == null)
        {
            return;
        }
        opportunityLifecycle.cancelOpenOpportunities(
            activeActivityId,
            endTime,
            Collections.singletonList(new OpportunityEvidence(endTime, RegionInstanceTelemetryEvent.TYPE, EvidenceStrength.CONFIRMING, detail)));
        reusableExecutionTrackers.cancelOpenOpportunities(endTime, detail);
        rangedHeadOpportunity = null;
    }

    void expireTimedOut(EventTime time)
    {
        if (opportunityLifecycle != null)
        {
            opportunityLifecycle.expireTimedOut(time);
        }
        reusableExecutionTrackers.expireTimedOut(time);
        if (!isOpen(rangedHeadOpportunity))
        {
            rangedHeadOpportunity = null;
        }
    }

    Map<String, String> snapshotAttributes()
    {
        final Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("verificationStatus", verificationDecision.getStatus().name());
        attributes.put("verifiedMechanics", String.join(",", verificationDecision.getVerifiedMechanics()));
        attributes.put("rangedHeadResponseCount", String.valueOf(rangedHeadResponseCount));
        attributes.put("rangedHeadDamageFailures", String.valueOf(rangedHeadDamageFailures));
        attributes.put("bloodSplatGraphicIdCount", String.valueOf(bloodSplatGraphicIds.length));
        attributes.put("axeMechanicIdCount", String.valueOf(axeMechanicIds.length));
        return attributes;
    }

    void resetForNextSession()
    {
        currentRegionId = -1;
        currentPlayerLocation = WorldLocation.unknown();
        activeActivityId = null;
        opportunityLifecycle = null;
        pendingRangedHeadProjectile = null;
        rangedHeadOpportunity = null;
        rangedHeadResponseCount = 0;
        rangedHeadDamageFailures = 0;
        reusableExecutionTrackers.reset();
    }

    java.util.List<String> activationEvidence(ProjectileTelemetryEvent event)
    {
        return Arrays.asList(
            "Vardorvis verification status is " + verificationDecision.getStatus().name() + ".",
            "Verified region " + currentRegionId + " contains the ranged-head cue.",
            "Verified ranged-head projectile " + event.getProjectileId() + " targeted the local player from NPC " + event.getSourceRef().getId() + ".");
    }

    EventTime activationStartTime(ProjectileTelemetryEvent event)
    {
        return event.getTime();
    }

    private void openRangedHeadOpportunity(ProjectileTelemetryEvent event)
    {
        if (opportunityLifecycle == null || activeActivityId == null)
        {
            return;
        }
        rangedHeadOpportunity = opportunityLifecycle.start(
            activeActivityId,
            RANGED_HEAD_RESPONSE,
            event.getTime(),
            context(
                "mechanic", MECHANIC_RANGED_HEAD_RESPONSE,
                "projectileId", String.valueOf(event.getProjectileId()),
                "sourceNpcId", String.valueOf(event.getSourceRef().getId()),
                "regionId", String.valueOf(currentRegionId)));
    }

    private boolean isHeadRef(EntityRef ref)
    {
        return ref.getType() == EntityRef.Type.NPC && contains(headNpcIds, ref.getId());
    }

    private static boolean contains(int[] values, int needle)
    {
        return Arrays.stream(values).anyMatch(value -> value == needle);
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

    private static boolean isOpen(OpportunityInstance instance)
    {
        return instance != null && instance.getStatus() == OpportunityStatus.OPEN;
    }

    private static final class PendingProjectile
    {
        private final ProjectileTelemetryEvent event;

        private PendingProjectile(ProjectileTelemetryEvent event)
        {
            this.event = event;
        }
    }
}
