package com.ticksense.activities.gemmining;

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
import com.ticksense.common.IntIdSet;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class GemMiningState
{
    static final String OPPORTUNITY_RESPAWN_TO_CLICK = "GEM_ROCK_RESPAWN_TO_CLICK";
    static final String OPPORTUNITY_IDLE = "GEM_MINING_IDLE";
    static final String OPPORTUNITY_MOVEMENT_TO_ROCK = "GEM_MOVEMENT_TO_ROCK";

    private static final OpportunityDefinition RESPAWN_TO_CLICK = new OpportunityDefinition(
        OPPORTUNITY_RESPAWN_TO_CLICK,
        "Rock respawn to click",
        com.ticksense.core.ActivityType.GEM_MINING,
        0L,
        Collections.singletonList("Mine rock"));

    private static final OpportunityDefinition IDLE = new OpportunityDefinition(
        OPPORTUNITY_IDLE,
        "Gem mining idle",
        com.ticksense.core.ActivityType.GEM_MINING,
        0L,
        Collections.singletonList("Resume mining"));

    private static final OpportunityDefinition MOVEMENT_TO_ROCK = new OpportunityDefinition(
        OPPORTUNITY_MOVEMENT_TO_ROCK,
        "Movement to rock",
        com.ticksense.core.ActivityType.GEM_MINING,
        0L,
        Collections.singletonList("Move into mining range"));

    private static final IntIdSet VERIFIED_REGION_IDS = IntIdSet.of(GemMiningIds.gemMiningRegionIds());
    private static final IntIdSet VERIFIED_OBJECT_IDS = IntIdSet.of(GemMiningIds.gemRockObjectIds());
    private static final IntIdSet VERIFIED_ANIMATION_IDS = IntIdSet.of(GemMiningIds.miningAnimationIds());
    private static final IntIdSet VERIFIED_GEM_ITEM_IDS = IntIdSet.of(GemMiningIds.uncutGemItemIds());

    private int currentRegionId = -1;
    private WorldLocation currentPlayerLocation = WorldLocation.unknown();
    private RockAvailability availableRock;
    private MovementTowardRock pendingMovement;
    private PendingClick pendingClick;
    private ActivityId activeActivityId;
    private OpportunityLifecycle opportunityLifecycle;
    private final ExecutionTrackerSet reusableExecutionTrackers = CommonExecutionTrackers.skillingDefaults();
    private final List<OpportunityInstance> opportunityInstances = new ArrayList<>();
    private int redundantClicks;
    private int totalMineClicks;
    private int totalIdleTicks;
    private int miningConfirmations;

    void updateRegion(WorldLocation playerLocation)
    {
        if (playerLocation == null)
        {
            return;
        }
        currentPlayerLocation = playerLocation;
        currentRegionId = playerLocation.getRegionId();
    }

    boolean hasVerifiedRegion()
    {
        return VERIFIED_REGION_IDS.contains(currentRegionId);
    }

    boolean isVerifiedRegion(int regionId)
    {
        return VERIFIED_REGION_IDS.contains(regionId);
    }

    boolean isVerifiedObject(int objectId)
    {
        return VERIFIED_OBJECT_IDS.contains(objectId);
    }

    boolean isVerifiedMiningAnimation(int animationId)
    {
        return VERIFIED_ANIMATION_IDS.contains(animationId);
    }

    boolean isVerifiedGemItem(int itemId)
    {
        return VERIFIED_GEM_ITEM_IDS.contains(itemId);
    }

    boolean matchesAvailableRock(WorldLocation location)
    {
        return availableRock != null && availableRock.location.equals(location);
    }

    void markRockAvailable(int objectId, WorldLocation location, EventTime time)
    {
        if (!isVerifiedRegion(location.getRegionId()) || !isVerifiedObject(objectId))
        {
            return;
        }
        if (availableRock != null && availableRock.objectId == objectId && availableRock.location.equals(location))
        {
            return;
        }
        availableRock = new RockAvailability(objectId, location, time);
        pendingMovement = null;
    }

    void markRockDepleted(int objectId, WorldLocation location)
    {
        if (availableRock != null && availableRock.objectId == objectId && availableRock.location.equals(location))
        {
            availableRock = null;
            pendingMovement = null;
        }
        if (pendingClick != null && pendingClick.matches(objectId, location))
        {
            pendingClick.awaitingProgress = false;
        }
    }

    double activationConfidenceForMineClick(WorldLocation location)
    {
        if (!hasVerifiedRegion() || availableRock == null || location == null || !availableRock.location.equals(location))
        {
            return 0.60D;
        }
        return 0.92D;
    }

    List<String> activationEvidenceForMineClick()
    {
        if (availableRock == null)
        {
            return Collections.singletonList("Mine click observed without verified gem rock availability.");
        }
        final List<String> evidence = new ArrayList<>();
        evidence.add("Verified gem mining region " + availableRock.location.getRegionId() + " is active.");
        evidence.add("Verified gem rock " + availableRock.objectId + " became available at "
            + availableRock.location.getX() + "," + availableRock.location.getY() + ".");
        evidence.add("Mine click targeted that verified gem rock.");
        return Collections.unmodifiableList(evidence);
    }

    EventTime activationStartTime(EventTime fallback)
    {
        return availableRock == null ? fallback : availableRock.availableSince;
    }

    void noteMineClick(int objectId, WorldLocation location, EventTime time)
    {
        totalMineClicks++;
        if (pendingClick != null && pendingClick.matches(objectId, location) && pendingClick.awaitingProgress)
        {
            redundantClicks++;
        }
        pendingClick = new PendingClick(objectId, location, time, true, false);
    }

    void noteMovementTowardRock(MovementTowardRock movement)
    {
        pendingMovement = movement;
        if (movement != null)
        {
            currentPlayerLocation = movement.toLocation;
            currentRegionId = movement.toLocation.getRegionId();
        }
    }

    void noteProgressConfirmation()
    {
        miningConfirmations++;
        if (pendingClick != null)
        {
            pendingClick.awaitingProgress = false;
        }
    }

    RockAvailability availableRock()
    {
        return availableRock;
    }

    int availableRockObjectIdOrUnknown()
    {
        return availableRock == null ? -1 : availableRock.objectId;
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

    void expireTimedOut(EventTime time)
    {
        if (opportunityLifecycle != null)
        {
            opportunityLifecycle.expireTimedOut(time);
        }
        reusableExecutionTrackers.expireTimedOut(time);
    }

    void emitCycleOpportunitiesForActiveClick()
    {
        flushPendingCycleOpportunities();
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
    }

    Map<String, String> snapshotAttributes()
    {
        final Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("verificationStatus", GemMiningIds.verificationDecision().getStatus().name());
        attributes.put("verifiedRegionIds", VERIFIED_REGION_IDS.joinCsv());
        attributes.put("verifiedObjectIds", VERIFIED_OBJECT_IDS.joinCsv());
        attributes.put("mineClicks", String.valueOf(totalMineClicks));
        attributes.put("redundantClicks", String.valueOf(redundantClicks));
        attributes.put("idleTicks", String.valueOf(totalIdleTicks));
        attributes.put("miningConfirmations", String.valueOf(miningConfirmations));
        attributes.put("respawnOpportunityCount", String.valueOf(countTerminalOpportunities(OPPORTUNITY_RESPAWN_TO_CLICK)));
        attributes.put("idleOpportunityCount", String.valueOf(countTerminalOpportunities(OPPORTUNITY_IDLE)));
        attributes.put("movementOpportunityCount", String.valueOf(countTerminalOpportunities(OPPORTUNITY_MOVEMENT_TO_ROCK)));
        return attributes;
    }

    void resetForNextSession()
    {
        availableRock = null;
        pendingMovement = null;
        pendingClick = null;
        activeActivityId = null;
        opportunityLifecycle = null;
        opportunityInstances.clear();
        redundantClicks = 0;
        totalMineClicks = 0;
        totalIdleTicks = 0;
        miningConfirmations = 0;
        reusableExecutionTrackers.reset();
    }

    private void flushPendingCycleOpportunities()
    {
        if (opportunityLifecycle == null || activeActivityId == null || availableRock == null || pendingClick == null || pendingClick.opportunitiesEmitted)
        {
            return;
        }
        if (!pendingClick.matches(availableRock.objectId, availableRock.location))
        {
            return;
        }

        final OpportunityInstance respawn = startOpportunity(
            RESPAWN_TO_CLICK,
            availableRock.availableSince,
            cycleContext(availableRock));
        opportunityLifecycle.complete(respawn.getInstanceId(), pendingClick.time, cycleEvidence(PlayerActionTelemetryEvent.TYPE, "Mine click on available gem rock."));

        final int idleTicks = Math.max(0, pendingClick.time.getGameTick() - availableRock.availableSince.getGameTick());
        if (idleTicks > 0)
        {
            final OpportunityInstance idle = startOpportunity(
                IDLE,
                availableRock.availableSince,
                cycleContext(availableRock));
            opportunityLifecycle.complete(idle.getInstanceId(), pendingClick.time, cycleEvidence(PlayerActionTelemetryEvent.TYPE, "Rock stayed available before the next mine click."));
            totalIdleTicks += idleTicks;
        }

        if (pendingMovement != null && pendingMovement.matches(availableRock.location))
        {
            final OpportunityInstance movement = startOpportunity(
                MOVEMENT_TO_ROCK,
                pendingMovement.time,
                movementContext(pendingMovement, availableRock));
            opportunityLifecycle.complete(movement.getInstanceId(), pendingClick.time, cycleEvidence("movement.location", "Player moved into range before mining."));
            pendingMovement = null;
        }
        pendingClick.opportunitiesEmitted = true;
    }

    private OpportunityInstance startOpportunity(
        OpportunityDefinition definition,
        EventTime startTime,
        Map<String, String> context)
    {
        final OpportunityInstance instance = opportunityLifecycle.start(activeActivityId, definition, startTime, context);
        opportunityInstances.add(instance);
        return instance;
    }

    private List<OpportunityEvidence> cycleEvidence(String sourceEventType, String detail)
    {
        return Collections.singletonList(new OpportunityEvidence(
            pendingClick.time,
            sourceEventType,
            EvidenceStrength.CONFIRMING,
            detail));
    }

    private Map<String, String> cycleContext(RockAvailability rock)
    {
        final Map<String, String> context = new LinkedHashMap<>();
        context.put("objectId", String.valueOf(rock.objectId));
        context.put("regionId", String.valueOf(rock.location.getRegionId()));
        context.put("rockX", String.valueOf(rock.location.getX()));
        context.put("rockY", String.valueOf(rock.location.getY()));
        return context;
    }

    private Map<String, String> movementContext(MovementTowardRock movement, RockAvailability rock)
    {
        final Map<String, String> context = cycleContext(rock);
        context.put("fromX", String.valueOf(movement.fromLocation.getX()));
        context.put("fromY", String.valueOf(movement.fromLocation.getY()));
        context.put("toX", String.valueOf(movement.toLocation.getX()));
        context.put("toY", String.valueOf(movement.toLocation.getY()));
        return context;
    }

    private int countTerminalOpportunities(String opportunityType)
    {
        int count = 0;
        for (OpportunityInstance instance : opportunityInstances)
        {
            if (opportunityType.equals(instance.getDefinition().getId())
                && instance.getStatus() != OpportunityStatus.OPEN)
            {
                count++;
            }
        }
        return count;
    }

    static boolean isMineAction(String option, String target)
    {
        return normalize(option).equals("mine") && normalize(target).contains("gem rock");
    }

    static boolean isMovementTowardAvailableRock(MovementTelemetry movement, RockAvailability rock)
    {
        if (movement == null || rock == null)
        {
            return false;
        }
        final int fromDistance = chebyshevDistance(movement.fromLocation, rock.location);
        final int toDistance = chebyshevDistance(movement.toLocation, rock.location);
        return fromDistance > 1 && toDistance <= 1;
    }

    static boolean isLocalPlayer(EntityRef entityRef)
    {
        return entityRef != null && entityRef.getType() == EntityRef.Type.LOCAL_PLAYER;
    }

    private static int chebyshevDistance(WorldLocation left, WorldLocation right)
    {
        return Math.max(Math.abs(left.getX() - right.getX()), Math.abs(left.getY() - right.getY()));
    }

    private static String normalize(String text)
    {
        if (text == null)
        {
            return "";
        }
        return text.replaceAll("<[^>]+>", "").trim().toLowerCase(Locale.ENGLISH);
    }

    static final class RockAvailability
    {
        private final int objectId;
        private final WorldLocation location;
        private final EventTime availableSince;

        RockAvailability(int objectId, WorldLocation location, EventTime availableSince)
        {
            this.objectId = objectId;
            this.location = location;
            this.availableSince = availableSince;
        }
    }

    static final class PendingClick
    {
        private final int objectId;
        private final WorldLocation location;
        private final EventTime time;
        private boolean awaitingProgress;
        private boolean opportunitiesEmitted;

        PendingClick(int objectId, WorldLocation location, EventTime time, boolean awaitingProgress, boolean opportunitiesEmitted)
        {
            this.objectId = objectId;
            this.location = location;
            this.time = time;
            this.awaitingProgress = awaitingProgress;
            this.opportunitiesEmitted = opportunitiesEmitted;
        }

        private boolean matches(int nextObjectId, WorldLocation nextLocation)
        {
            return objectId == nextObjectId && location.equals(nextLocation);
        }
    }

    static final class MovementTowardRock
    {
        private final EventTime time;
        private final WorldLocation fromLocation;
        private final WorldLocation toLocation;

        MovementTowardRock(EventTime time, WorldLocation fromLocation, WorldLocation toLocation)
        {
            this.time = time;
            this.fromLocation = fromLocation;
            this.toLocation = toLocation;
        }

        private boolean matches(WorldLocation rockLocation)
        {
            return GemMiningState.chebyshevDistance(toLocation, rockLocation) <= 1;
        }
    }

    static final class MovementTelemetry
    {
        private final EventTime time;
        private final WorldLocation fromLocation;
        private final WorldLocation toLocation;

        MovementTelemetry(EventTime time, WorldLocation fromLocation, WorldLocation toLocation)
        {
            this.time = time;
            this.fromLocation = fromLocation;
            this.toLocation = toLocation;
        }
    }
}
