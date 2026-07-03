package com.ticksense.activities.araxxor;

import com.ticksense.activities.ActivityContext;
import com.ticksense.activities.OpportunityLifecycle;
import com.ticksense.activities.execution.CommonExecutionTrackers;
import com.ticksense.activities.execution.ExecutionTrackerSet;
import com.ticksense.activities.execution.MovementResponseTracker;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.DamageTelemetryEvent;
import com.ticksense.telemetry.events.InteractingChangedTelemetryEvent;
import com.ticksense.telemetry.events.NpcStateTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

final class AraxxorState
{
    private static final int BOSS_ABSENT_IDLE_TICKS = 5;

    private final AraxxorVerificationDecision verificationDecision;
    private final int[] araxxorNpcIds;
    private final int[] spiderNpcIds;
    private final int[] verifiedRegionIds;
    private final AraxxorExecutionTracker executionTracker = new AraxxorExecutionTracker();
    private final ExecutionTrackerSet reusableExecutionTrackers = ExecutionTrackerSet.of(
        "araxxor-reusable-execution",
        CommonExecutionTrackers.combatSupport(),
        new MovementResponseTracker());

    private int currentRegionId = -1;
    private boolean currentInstanced;
    private boolean bossPresent;
    private boolean spiderPresent;
    private int lastLocalTargetNpcId = -1;
    private TargetType lastLocalTarget = TargetType.NONE;
    private int lastBossSeenTick = -1;

    AraxxorState(
        AraxxorVerificationDecision verificationDecision,
        int[] araxxorNpcIds,
        int[] spiderNpcIds,
        int[] verifiedRegionIds)
    {
        this.verificationDecision = verificationDecision;
        this.araxxorNpcIds = araxxorNpcIds.clone();
        this.spiderNpcIds = spiderNpcIds.clone();
        this.verifiedRegionIds = verifiedRegionIds.clone();
    }

    boolean allowsNormalStrategyEnablement()
    {
        return verificationDecision.allowsNormalStrategyEnablement();
    }

    void updateRegion(WorldLocation location)
    {
        if (location != null)
        {
            currentRegionId = location.getRegionId();
            currentInstanced = location.isInstanced();
        }
    }

    boolean canActivateFromBossInteraction(InteractingChangedTelemetryEvent event)
    {
        return allowsNormalStrategyEnablement()
            && isVerifiedRegion(currentRegionId)
            && event.getActorRef().getType() == EntityRef.Type.LOCAL_PLAYER
            && event.getInteractingRef().getType() == EntityRef.Type.NPC
            && contains(araxxorNpcIds, event.getInteractingRef().getId());
    }

    void noteActivationInteraction(InteractingChangedTelemetryEvent event)
    {
        bossPresent = true;
        lastBossSeenTick = event.getTime().getGameTick();
        lastLocalTarget = TargetType.BOSS;
        lastLocalTargetNpcId = event.getInteractingRef().getId();
    }

    List<String> activationEvidence(InteractingChangedTelemetryEvent event)
    {
        return Arrays.asList(
            "Araxxor verification status is " + verificationDecision.getStatus().name() + ".",
            "Verified Araxxor region " + currentRegionId + " is active.",
            "Local player interacted with verified Araxxor NPC " + event.getInteractingRef().getId() + ".");
    }

    EventTime activationStartTime(InteractingChangedTelemetryEvent event)
    {
        return event.getTime();
    }

    void startActivity(ActivityId activityId)
    {
        executionTracker.startActivity(activityId);
        reusableExecutionTrackers.startActivity(activityId);
    }

    void ensureOpportunityLifecycle(OpportunityLifecycle opportunityLifecycle)
    {
        executionTracker.ensureOpportunityLifecycle(opportunityLifecycle);
        reusableExecutionTrackers.ensureOpportunityLifecycle(opportunityLifecycle);
    }

    void noteReusableExecutionEvent(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        reusableExecutionTrackers.onEvent(context, session, event);
    }

    void noteNpc(NpcStateTelemetryEvent event)
    {
        if (!isVerifiedRegion(currentRegionId) || event.getNpcRef().getType() != EntityRef.Type.NPC)
        {
            return;
        }

        final int npcId = event.getNpcRef().getId();
        if (contains(araxxorNpcIds, npcId))
        {
            bossPresent = !"DESPAWNED".equals(event.getStateChange()) && event.getHealthRatio() != 0;
            if (bossPresent)
            {
                lastBossSeenTick = event.getTime().getGameTick();
            }
            return;
        }

        if (!contains(spiderNpcIds, npcId))
        {
            return;
        }

        if ("SPAWNED".equals(event.getStateChange()))
        {
            spiderPresent = true;
            executionTracker.openSpiderEngagement(event.getTime(), npcId, currentRegionId);
            return;
        }

        if ("DESPAWNED".equals(event.getStateChange()) || event.getHealthRatio() == 0)
        {
            if (spiderPresent)
            {
                executionTracker.failSpiderEngagement(
                    event.getTime(),
                    "Verified Araxxor spider window closed before engagement evidence completed.");
                executionTracker.openBossReengagement(event.getTime(), npcId, currentRegionId);
            }
            spiderPresent = false;
        }
    }

    void noteAction(PlayerActionTelemetryEvent event)
    {
        if (event.getTargetRef().getType() != EntityRef.Type.NPC)
        {
            return;
        }

        final int npcId = event.getTargetRef().getId();
        if (contains(spiderNpcIds, npcId) && isAttackAction(event))
        {
            executionTracker.completeSpiderEngagement(
                event.getTime(),
                "player.action",
                "Local player attacked the verified Araxxor spider.");
            lastLocalTarget = TargetType.SPIDER;
            lastLocalTargetNpcId = npcId;
            return;
        }

        if (!contains(araxxorNpcIds, npcId) || !isAttackAction(event))
        {
            return;
        }

        bossPresent = true;
        lastBossSeenTick = event.getTime().getGameTick();
        executionTracker.completeBossReengagement(
            event.getTime(),
            "player.action",
            "Local player re-engaged verified Araxxor with an attack click.");
        executionTracker.completeTargetReengagement(
            event.getTime(),
            "player.action",
            "Local player recovered Araxxor target with an attack click.");
        lastLocalTarget = TargetType.BOSS;
        lastLocalTargetNpcId = npcId;
    }

    void noteInteraction(InteractingChangedTelemetryEvent event)
    {
        if (event.getActorRef().getType() != EntityRef.Type.LOCAL_PLAYER)
        {
            return;
        }

        if (event.getInteractingRef().getType() == EntityRef.Type.NPC)
        {
            final int npcId = event.getInteractingRef().getId();
            if (contains(spiderNpcIds, npcId))
            {
                executionTracker.completeSpiderEngagement(
                    event.getTime(),
                    "interacting.changed",
                    "Local player engaged the verified Araxxor spider.");
                lastLocalTarget = TargetType.SPIDER;
                lastLocalTargetNpcId = npcId;
                return;
            }

            if (contains(araxxorNpcIds, npcId))
            {
                bossPresent = true;
                lastBossSeenTick = event.getTime().getGameTick();
                executionTracker.completeBossReengagement(
                    event.getTime(),
                    "interacting.changed",
                    "Local player re-engaged verified Araxxor.");
                executionTracker.completeTargetReengagement(
                    event.getTime(),
                    "interacting.changed",
                    "Local player restored Araxxor targeting.");
                lastLocalTarget = TargetType.BOSS;
                lastLocalTargetNpcId = npcId;
                return;
            }
        }

        if (event.getInteractingRef().getType() == EntityRef.Type.UNKNOWN
            && lastLocalTarget == TargetType.BOSS
            && contains(araxxorNpcIds, lastLocalTargetNpcId)
            && bossPresent
            && !spiderPresent)
        {
            executionTracker.openTargetReengagement(event.getTime(), currentRegionId);
        }

        lastLocalTarget = TargetType.NONE;
        lastLocalTargetNpcId = -1;
    }

    void noteDamage(DamageTelemetryEvent event)
    {
        if (event.getTargetRef().getType() == EntityRef.Type.LOCAL_PLAYER)
        {
            executionTracker.noteDamage(event.getAmount());
        }
    }

    boolean isVerifiedPlayerDeath(DamageTelemetryEvent event)
    {
        return event.getTargetRef().getType() == EntityRef.Type.LOCAL_PLAYER
            && event.getHealthRatio() <= 0;
    }

    boolean isBossDefeat(NpcStateTelemetryEvent event)
    {
        return contains(araxxorNpcIds, event.getNpcRef().getId())
            && ("DESPAWNED".equals(event.getStateChange()) || event.getHealthRatio() == 0);
    }

    boolean leftVerifiedRegion()
    {
        return !isVerifiedRegion(currentRegionId) || !currentInstanced;
    }

    boolean shouldIdleTerminate(EventTime time)
    {
        return lastBossSeenTick >= 0
            && !bossPresent
            && !spiderPresent
            && time.getGameTick() - lastBossSeenTick >= BOSS_ABSENT_IDLE_TICKS;
    }

    void expireTimedOut(EventTime time)
    {
        executionTracker.expireTimedOut(time);
        reusableExecutionTrackers.expireTimedOut(time);
    }

    void cancelOpenOpportunities(EventTime time, String detail)
    {
        executionTracker.cancelOpenOpportunities(time, detail);
        reusableExecutionTrackers.cancelOpenOpportunities(time, detail);
    }

    Map<String, String> snapshotAttributes()
    {
        return executionTracker.snapshotData(verificationDecision.getStatus().name()).toAttributes();
    }

    void resetForNextSession()
    {
        currentRegionId = -1;
        currentInstanced = false;
        bossPresent = false;
        spiderPresent = false;
        lastLocalTargetNpcId = -1;
        lastLocalTarget = TargetType.NONE;
        lastBossSeenTick = -1;
        executionTracker.reset();
        reusableExecutionTrackers.reset();
    }

    private static boolean isAttackAction(PlayerActionTelemetryEvent event)
    {
        return "Attack".equalsIgnoreCase(event.getOption())
            || event.getActionKind().startsWith("NPC_");
    }

    private boolean contains(int[] values, int needle)
    {
        return Arrays.stream(values).anyMatch(value -> value == needle);
    }

    private boolean isVerifiedRegion(int regionId)
    {
        return contains(verifiedRegionIds, regionId);
    }

    private enum TargetType
    {
        NONE,
        BOSS,
        SPIDER
    }
}
