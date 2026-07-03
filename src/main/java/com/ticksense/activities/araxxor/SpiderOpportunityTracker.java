package com.ticksense.activities.araxxor;

import com.ticksense.activities.EvidenceStrength;
import com.ticksense.activities.OpportunityDefinition;
import com.ticksense.activities.OpportunityEvidence;
import com.ticksense.activities.OpportunityInstance;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.activities.OpportunityTracker;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class SpiderOpportunityTracker
{
    static final String OPPORTUNITY_SPIDER_ENGAGEMENT = "ARAXXOR_SPIDER_ENGAGEMENT";
    static final String OPPORTUNITY_BOSS_REENGAGEMENT = "ARAXXOR_BOSS_REENGAGEMENT";
    static final String OPPORTUNITY_TARGET_REENGAGEMENT = "ARAXXOR_TARGET_REENGAGEMENT";

    private static final OpportunityDefinition SPIDER_ENGAGEMENT = new OpportunityDefinition(
        OPPORTUNITY_SPIDER_ENGAGEMENT,
        "Araxxor spider engagement",
        ActivityType.ARAXXOR,
        3_600L,
        Collections.singletonList("Attack or engage the verified spider"));

    private static final OpportunityDefinition BOSS_REENGAGEMENT = new OpportunityDefinition(
        OPPORTUNITY_BOSS_REENGAGEMENT,
        "Araxxor boss re-engagement",
        ActivityType.ARAXXOR,
        3_600L,
        Collections.singletonList("Re-engage Araxxor after the spider phase"));

    private static final OpportunityDefinition TARGET_REENGAGEMENT = new OpportunityDefinition(
        OPPORTUNITY_TARGET_REENGAGEMENT,
        "Araxxor target re-engagement",
        ActivityType.ARAXXOR,
        2_400L,
        Collections.singletonList("Re-engage Araxxor after target loss"));

    private ActivityId activeActivityId;
    private OpportunityTracker tracker;
    private OpportunityInstance spiderEngagement;
    private OpportunityInstance bossReengagement;
    private OpportunityInstance targetReengagement;
    private int spiderEngagementCount;
    private int bossReengagementCount;
    private int targetReengagementCount;
    private int spiderWindowDamage;
    private int bossReengagementDamage;
    private int targetReengagementDamage;

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

    void openSpiderEngagement(EventTime time, int spiderNpcId, int regionId)
    {
        if (tracker == null || activeActivityId == null || isOpen(spiderEngagement))
        {
            return;
        }
        spiderEngagement = tracker.start(
            activeActivityId,
            SPIDER_ENGAGEMENT,
            time,
            context(
                "spiderNpcId", String.valueOf(spiderNpcId),
                "regionId", String.valueOf(regionId)));
    }

    void completeSpiderEngagement(EventTime time, String evidenceType, String detail)
    {
        if (!isOpen(spiderEngagement))
        {
            return;
        }
        tracker.complete(spiderEngagement.getInstanceId(), time, evidence(evidenceType, time, detail));
        spiderEngagementCount++;
        spiderEngagement = null;
    }

    void failSpiderEngagement(EventTime time, String detail)
    {
        if (!isOpen(spiderEngagement))
        {
            return;
        }
        tracker.fail(spiderEngagement.getInstanceId(), time, evidence("npc.state", time, detail));
        spiderEngagement = null;
    }

    void openBossReengagement(EventTime time, int spiderNpcId, int regionId)
    {
        if (tracker == null || activeActivityId == null || isOpen(bossReengagement))
        {
            return;
        }
        bossReengagement = tracker.start(
            activeActivityId,
            BOSS_REENGAGEMENT,
            time,
            context(
                "spiderNpcId", String.valueOf(spiderNpcId),
                "regionId", String.valueOf(regionId)));
    }

    void completeBossReengagement(EventTime time, String evidenceType, String detail)
    {
        if (!isOpen(bossReengagement))
        {
            return;
        }
        tracker.complete(bossReengagement.getInstanceId(), time, evidence(evidenceType, time, detail));
        bossReengagementCount++;
        bossReengagement = null;
    }

    void openTargetReengagement(EventTime time, int regionId)
    {
        if (tracker == null || activeActivityId == null || isOpen(targetReengagement))
        {
            return;
        }
        targetReengagement = tracker.start(
            activeActivityId,
            TARGET_REENGAGEMENT,
            time,
            context("regionId", String.valueOf(regionId)));
    }

    void completeTargetReengagement(EventTime time, String evidenceType, String detail)
    {
        if (!isOpen(targetReengagement))
        {
            return;
        }
        tracker.complete(targetReengagement.getInstanceId(), time, evidence(evidenceType, time, detail));
        targetReengagementCount++;
        targetReengagement = null;
    }

    void noteDamage(int amount)
    {
        if (isOpen(spiderEngagement))
        {
            spiderWindowDamage += amount;
        }
        if (isOpen(bossReengagement))
        {
            bossReengagementDamage += amount;
        }
        if (isOpen(targetReengagement))
        {
            targetReengagementDamage += amount;
        }
    }

    void expireTimedOut(EventTime time)
    {
        if (tracker != null)
        {
            tracker.expireTimedOut(time);
        }
        clearIfTerminal();
    }

    void cancelOpenOpportunities(EventTime time, String detail)
    {
        if (tracker == null || activeActivityId == null)
        {
            return;
        }
        tracker.cancelOpenOpportunities(activeActivityId, time, evidence("region.instance", time, detail));
        spiderEngagement = null;
        bossReengagement = null;
        targetReengagement = null;
    }

    AraxxorActivityData snapshotData(String verificationStatus)
    {
        return new AraxxorActivityData(
            verificationStatus,
            spiderEngagementCount,
            bossReengagementCount,
            targetReengagementCount,
            spiderWindowDamage,
            bossReengagementDamage,
            targetReengagementDamage);
    }

    void reset()
    {
        activeActivityId = null;
        tracker = null;
        spiderEngagement = null;
        bossReengagement = null;
        targetReengagement = null;
        spiderEngagementCount = 0;
        bossReengagementCount = 0;
        targetReengagementCount = 0;
        spiderWindowDamage = 0;
        bossReengagementDamage = 0;
        targetReengagementDamage = 0;
    }

    private void clearIfTerminal()
    {
        if (!isOpen(spiderEngagement))
        {
            spiderEngagement = null;
        }
        if (!isOpen(bossReengagement))
        {
            bossReengagement = null;
        }
        if (!isOpen(targetReengagement))
        {
            targetReengagement = null;
        }
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

    private static List<OpportunityEvidence> evidence(String type, EventTime time, String detail)
    {
        return Collections.singletonList(new OpportunityEvidence(time, type, EvidenceStrength.CONFIRMING, detail));
    }

    private static boolean isOpen(OpportunityInstance instance)
    {
        return instance != null && instance.getStatus() == OpportunityStatus.OPEN;
    }
}
