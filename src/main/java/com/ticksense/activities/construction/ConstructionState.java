package com.ticksense.activities.construction;

import com.ticksense.activities.EvidenceStrength;
import com.ticksense.activities.OpportunityDefinition;
import com.ticksense.activities.OpportunityEvidence;
import com.ticksense.activities.OpportunityInstance;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.activities.OpportunityTracker;
import com.ticksense.core.ActivityId;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class ConstructionState
{
    static final String OPPORTUNITY_MENU_LATENCY = "CONSTRUCTION_MENU_LATENCY";
    static final String OPPORTUNITY_BUILD_REMOVE_CADENCE = "CONSTRUCTION_BUILD_REMOVE_CADENCE";
    static final String OPPORTUNITY_INVENTORY_CYCLE = "CONSTRUCTION_INVENTORY_CYCLE";
    static final String OPPORTUNITY_BANKING_DOWNTIME = "CONSTRUCTION_BANKING_DOWNTIME";

    private static final OpportunityDefinition MENU_LATENCY = new OpportunityDefinition(
        OPPORTUNITY_MENU_LATENCY,
        "Construction menu latency",
        com.ticksense.core.ActivityType.CONSTRUCTION,
        0L,
        Collections.singletonList("Click the intended build or remove option"));

    private static final OpportunityDefinition BUILD_REMOVE_CADENCE = new OpportunityDefinition(
        OPPORTUNITY_BUILD_REMOVE_CADENCE,
        "Construction build/remove cadence",
        com.ticksense.core.ActivityType.CONSTRUCTION,
        0L,
        Collections.singletonList("Continue the next verified build/remove step"));

    private static final OpportunityDefinition INVENTORY_CYCLE = new OpportunityDefinition(
        OPPORTUNITY_INVENTORY_CYCLE,
        "Construction inventory cycle",
        com.ticksense.core.ActivityType.CONSTRUCTION,
        0L,
        Collections.singletonList("Refill planks and tools"));

    private static final OpportunityDefinition BANKING_DOWNTIME = new OpportunityDefinition(
        OPPORTUNITY_BANKING_DOWNTIME,
        "Construction banking downtime",
        com.ticksense.core.ActivityType.CONSTRUCTION,
        0L,
        Collections.singletonList("Reach bank or servant refill confirmation"));

    private int currentRegionId = -1;
    private WorldLocation currentPlayerLocation = WorldLocation.unknown();
    private BuildSpotAvailability availableBuildSpot;
    private BuiltObject builtObject;
    private MenuOpen pendingMenu;
    private PendingBuild pendingBuild;
    private PendingBanking pendingBanking;
    private OpportunityInstance inventoryCycle;
    private OpportunityInstance cadenceOpportunity;
    private ActivityId activeActivityId;
    private OpportunityTracker tracker;
    private final List<OpportunityInstance> opportunities = new ArrayList<>();
    private int menuLatencyCount;
    private int buildRemoveCadenceCount;
    private int inventoryCycleCount;
    private int bankingDowntimeCount;
    private int buildConfirmationCount;
    private int removeClickCount;

    void updateRegion(WorldLocation playerLocation)
    {
        if (playerLocation == null)
        {
            return;
        }
        currentPlayerLocation = playerLocation;
        currentRegionId = playerLocation.getRegionId();
    }

    boolean hasVerifiedMethod()
    {
        return ConstructionIds.verificationDecision().allowsStrategyEnablement();
    }

    boolean matchesAvailableBuildSpot(WorldLocation location)
    {
        return availableBuildSpot != null && availableBuildSpot.location.equals(location);
    }

    void markObjectState(int objectId, WorldLocation location, String objectName, String stateChange, EventTime time)
    {
        if (contains(ConstructionIds.buildSpotObjectIds(), objectId) && "AVAILABLE".equals(stateChange))
        {
            availableBuildSpot = new BuildSpotAvailability(objectId, objectName, location, time);
            builtObject = null;
            return;
        }
        if (contains(ConstructionIds.builtObjectIds(), objectId) && "BUILT".equals(stateChange))
        {
            builtObject = new BuiltObject(objectId, objectName, location, time);
            availableBuildSpot = null;
            noteBuildConfirmation(time, "object.state", "Verified oak larder object became built.");
            return;
        }
        if (contains(ConstructionIds.buildSpotObjectIds(), objectId) && "AVAILABLE".equals(stateChange) && cadenceOpportunity != null)
        {
            builtObject = null;
        }
    }

    void noteMenuOpened(EventTime time, String selectedOption, String target)
    {
        pendingMenu = new MenuOpen(time, selectedOption, target);
    }

    void noteBuildClick(EventTime time, WorldLocation location)
    {
        pendingBuild = new PendingBuild(time, location);
    }

    void noteRemoveClick(EventTime time, WorldLocation location)
    {
        removeClickCount++;
        if (cadenceOpportunity != null && cadenceOpportunity.getStatus() == OpportunityStatus.OPEN)
        {
            tracker.complete(
                cadenceOpportunity.getInstanceId(),
                time,
                evidence(time, "player.action", "Verified remove click followed the last build confirmation."));
            buildRemoveCadenceCount++;
            cadenceOpportunity = null;
        }

        if (pendingBanking != null && !pendingBanking.startedByInventoryExhaustion)
        {
            pendingBanking = new PendingBanking(time, location, false);
        }
    }

    void noteMenuClick(EventTime time, String option, String target, WorldLocation location)
    {
        if (pendingMenu != null
            && normalize(pendingMenu.option).equals(normalize(option))
            && normalize(pendingMenu.target).equals(normalize(target)))
        {
            final OpportunityInstance menuLatency = startOpportunity(
                MENU_LATENCY,
                pendingMenu.time,
                context(
                    "option", option,
                    "target", target,
                    "regionId", String.valueOf(location.getRegionId())));
            tracker.complete(menuLatency.getInstanceId(), time, evidence(time, "player.action", "Verified menu click followed menu-open evidence."));
            menuLatencyCount++;
        }
        pendingMenu = null;
    }

    void noteConstructionWidget(EventTime time, int groupId, int childId)
    {
        if (contains(ConstructionIds.constructionWidgetGroupIds(), groupId)
            && contains(ConstructionIds.constructionWidgetChildIds(), childId))
        {
            noteBuildConfirmation(time, "widget", "Verified construction widget confirmed the oak larder build choice.");
        }
    }

    void noteConstructionAnimation(EventTime time, int animationId)
    {
        if (contains(ConstructionIds.buildAnimationIds(), animationId))
        {
            noteBuildConfirmation(time, "animation", "Verified Construction build animation confirmed progress.");
        }
    }

    void noteInventoryDelta(EventTime time, int beforeItemId, int afterItemId, int afterQuantity)
    {
        if (beforeItemId == 8778 || afterItemId == 8778)
        {
            noteBuildConfirmation(time, "inventory.delta", "Oak plank inventory changed during the verified build flow.");
            if (afterItemId == -1 || afterQuantity <= 0)
            {
                pendingBanking = new PendingBanking(time, currentPlayerLocation, true);
            }
        }
    }

    void noteConstructionXp(EventTime time, int xpDelta)
    {
        if (xpDelta > 0)
        {
            noteBuildConfirmation(time, "stat.changed", "Construction XP gain confirmed the verified oak larder build.");
        }
    }

    boolean isBankWidget(int groupId)
    {
        return contains(ConstructionIds.bankWidgetGroupIds(), groupId);
    }

    void noteBankWidget(EventTime time, int groupId)
    {
        if (!isBankWidget(groupId))
        {
            return;
        }
        if (inventoryCycle != null && inventoryCycle.getStatus() == OpportunityStatus.OPEN)
        {
            tracker.complete(
                inventoryCycle.getInstanceId(),
                time,
                evidence(time, "widget", "Verified bank widget completed the construction inventory cycle."));
            inventoryCycleCount++;
            inventoryCycle = null;
        }
        if (pendingBanking != null)
        {
            final OpportunityInstance downtime = startOpportunity(
                BANKING_DOWNTIME,
                pendingBanking.startTime,
                context(
                    "method", ConstructionIds.approvedMethodName(),
                    "refillPath", pendingBanking.startedByInventoryExhaustion ? "bank-after-inventory-exhaustion" : "bank-after-remove"));
            tracker.complete(
                downtime.getInstanceId(),
                time,
                evidence(time, "widget", "Verified bank widget ended Construction downtime."));
            bankingDowntimeCount++;
            pendingBanking = null;
        }
    }

    double activationConfidenceForBuildClick(WorldLocation location)
    {
        if (!hasVerifiedMethod())
        {
            return 0.0D;
        }
        if (matchesAvailableBuildSpot(location))
        {
            return 0.92D;
        }
        return 0.60D;
    }

    List<String> activationEvidence(WorldLocation location)
    {
        final List<String> evidence = new ArrayList<>();
        evidence.add("Construction verification decision is VERIFIED for method " + ConstructionIds.approvedMethodName() + ".");
        if (availableBuildSpot != null)
        {
            evidence.add("Verified build spot " + availableBuildSpot.objectId + " is available at "
                + availableBuildSpot.location.getX() + "," + availableBuildSpot.location.getY() + ".");
        }
        evidence.add("Observed verified Build click on Larder space at region " + location.getRegionId() + ".");
        return Collections.unmodifiableList(evidence);
    }

    EventTime activationStartTime(EventTime fallback)
    {
        return availableBuildSpot == null ? fallback : availableBuildSpot.availableSince;
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

    void flushActivationDerivedOpportunities()
    {
        if (tracker == null || activeActivityId == null || pendingBuild == null)
        {
            return;
        }
        if (!pendingBuild.inventoryCycleStarted)
        {
            inventoryCycle = startOpportunity(
                INVENTORY_CYCLE,
                pendingBuild.clickTime,
                context("method", ConstructionIds.approvedMethodName(), "regionId", String.valueOf(pendingBuild.location.getRegionId())));
            pendingBuild.inventoryCycleStarted = true;
        }
        if (!pendingBuild.menuLatencyEmitted && pendingMenu != null)
        {
            final OpportunityInstance menuLatency = startOpportunity(
                MENU_LATENCY,
                pendingMenu.time,
                context(
                    "option", pendingMenu.option,
                    "target", pendingMenu.target,
                    "regionId", String.valueOf(pendingBuild.location.getRegionId())));
            tracker.complete(
                menuLatency.getInstanceId(),
                pendingBuild.clickTime,
                evidence(pendingBuild.clickTime, "player.action", "Verified menu click followed menu-open evidence."));
            menuLatencyCount++;
            pendingBuild.menuLatencyEmitted = true;
            pendingMenu = null;
        }
    }

    void cancelOpenOpportunities(EventTime endTime, String detail)
    {
        if (tracker == null || activeActivityId == null)
        {
            return;
        }
        tracker.cancelOpenOpportunities(
            activeActivityId,
            endTime,
            Collections.singletonList(new OpportunityEvidence(endTime, "region.instance", EvidenceStrength.CONFIRMING, detail)));
    }

    Map<String, String> snapshotAttributes()
    {
        final Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("verificationStatus", ConstructionIds.verificationDecision().getStatus().name());
        attributes.put("methodName", ConstructionIds.approvedMethodName());
        attributes.put("menuLatencyCount", String.valueOf(menuLatencyCount));
        attributes.put("buildRemoveCadenceCount", String.valueOf(buildRemoveCadenceCount));
        attributes.put("inventoryCycleCount", String.valueOf(inventoryCycleCount));
        attributes.put("bankingDowntimeCount", String.valueOf(bankingDowntimeCount));
        attributes.put("buildConfirmationCount", String.valueOf(buildConfirmationCount));
        attributes.put("removeClickCount", String.valueOf(removeClickCount));
        return attributes;
    }

    void resetForNextSession()
    {
        availableBuildSpot = null;
        builtObject = null;
        pendingMenu = null;
        pendingBuild = null;
        pendingBanking = null;
        inventoryCycle = null;
        cadenceOpportunity = null;
        activeActivityId = null;
        tracker = null;
        opportunities.clear();
        menuLatencyCount = 0;
        buildRemoveCadenceCount = 0;
        inventoryCycleCount = 0;
        bankingDowntimeCount = 0;
        buildConfirmationCount = 0;
        removeClickCount = 0;
        currentPlayerLocation = WorldLocation.unknown();
        currentRegionId = -1;
    }

    private void noteBuildConfirmation(EventTime time, String sourceType, String detail)
    {
        if (pendingBuild == null || tracker == null || activeActivityId == null)
        {
            return;
        }
        buildConfirmationCount++;
        if (cadenceOpportunity == null || cadenceOpportunity.getStatus() != OpportunityStatus.OPEN)
        {
            cadenceOpportunity = startOpportunity(
                BUILD_REMOVE_CADENCE,
                time,
                context(
                    "method", ConstructionIds.approvedMethodName(),
                    "buildTick", String.valueOf(time.getGameTick())));
        }
        pendingBuild.confirmed = true;
        pendingBuild.confirmationSources.add(sourceType);
        pendingBuild.lastConfirmationDetail = detail;
    }

    private OpportunityInstance startOpportunity(
        OpportunityDefinition definition,
        EventTime startTime,
        Map<String, String> context)
    {
        final OpportunityInstance instance = tracker.start(activeActivityId, definition, startTime, context);
        opportunities.add(instance);
        return instance;
    }

    private static List<OpportunityEvidence> evidence(EventTime time, String sourceType, String detail)
    {
        return Collections.singletonList(new OpportunityEvidence(time, sourceType, EvidenceStrength.CONFIRMING, detail));
    }

    private static Map<String, String> context(String... values)
    {
        final Map<String, String> context = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2)
        {
            context.put(values[i], values[i + 1]);
        }
        return context;
    }

    static boolean isBuildAction(String option, String target)
    {
        return normalize(option).equals("build") && normalize(target).contains("larder space");
    }

    static boolean isRemoveAction(String option, String target)
    {
        return normalize(option).equals("remove") && normalize(target).contains("oak larder");
    }

    private static boolean contains(int[] values, int needle)
    {
        return Arrays.stream(values).anyMatch(value -> value == needle);
    }

    private static String normalize(String text)
    {
        if (text == null)
        {
            return "";
        }
        return text.replaceAll("<[^>]+>", "").trim().toLowerCase(Locale.ENGLISH);
    }

    private static final class BuildSpotAvailability
    {
        private final int objectId;
        private final String objectName;
        private final WorldLocation location;
        private final EventTime availableSince;

        private BuildSpotAvailability(int objectId, String objectName, WorldLocation location, EventTime availableSince)
        {
            this.objectId = objectId;
            this.objectName = objectName;
            this.location = location;
            this.availableSince = availableSince;
        }
    }

    private static final class BuiltObject
    {
        private final int objectId;
        private final String objectName;
        private final WorldLocation location;
        private final EventTime builtAt;

        private BuiltObject(int objectId, String objectName, WorldLocation location, EventTime builtAt)
        {
            this.objectId = objectId;
            this.objectName = objectName;
            this.location = location;
            this.builtAt = builtAt;
        }
    }

    private static final class MenuOpen
    {
        private final EventTime time;
        private final String option;
        private final String target;

        private MenuOpen(EventTime time, String option, String target)
        {
            this.time = time;
            this.option = option;
            this.target = target;
        }
    }

    private static final class PendingBuild
    {
        private final EventTime clickTime;
        private final WorldLocation location;
        private final List<String> confirmationSources = new ArrayList<>();
        private boolean confirmed;
        private boolean inventoryCycleStarted;
        private boolean menuLatencyEmitted;
        private String lastConfirmationDetail = "";

        private PendingBuild(EventTime clickTime, WorldLocation location)
        {
            this.clickTime = clickTime;
            this.location = location;
        }
    }

    private static final class PendingBanking
    {
        private final EventTime startTime;
        private final WorldLocation location;
        private final boolean startedByInventoryExhaustion;

        private PendingBanking(EventTime startTime, WorldLocation location, boolean startedByInventoryExhaustion)
        {
            this.startTime = startTime;
            this.location = location;
            this.startedByInventoryExhaustion = startedByInventoryExhaustion;
        }
    }
}
