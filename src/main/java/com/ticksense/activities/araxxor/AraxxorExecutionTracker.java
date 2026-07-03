package com.ticksense.activities.araxxor;

import com.ticksense.activities.EvidenceStrength;
import com.ticksense.activities.ExecutionTracker;
import com.ticksense.activities.OpportunityDefinition;
import com.ticksense.activities.OpportunityEvidence;
import com.ticksense.activities.OpportunityInstance;
import com.ticksense.activities.OpportunityLifecycle;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class AraxxorExecutionTracker implements ExecutionTracker
{
    private static final String ID = "araxxor.execution";

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
    private OpportunityLifecycle opportunityLifecycle;
    private OpportunityInstance spiderEngagement;
    private OpportunityInstance bossReengagement;
    private OpportunityInstance targetReengagement;
    private int spiderEngagementCount;
    private int bossReengagementCount;
    private int targetReengagementCount;
    private int spiderWindowDamage;
    private int bossReengagementDamage;
    private int targetReengagementDamage;

    @Override
    public String id()
    {
        return ID;
    }

    @Override
    public void startActivity(ActivityId activityId)
    {
        activeActivityId = activityId;
    }

    @Override
    public void ensureOpportunityLifecycle(OpportunityLifecycle nextLifecycle)
    {
        if (opportunityLifecycle == null)
        {
            opportunityLifecycle = nextLifecycle;
        }
    }

    void openSpiderEngagement(EventTime time, int spiderNpcId, int regionId)
    {
        if (opportunityLifecycle == null || activeActivityId == null || isOpen(spiderEngagement))
        {
            return;
        }
        spiderEngagement = opportunityLifecycle.start(
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
        opportunityLifecycle.complete(spiderEngagement.getInstanceId(), time, evidence(evidenceType, time, detail));
        spiderEngagementCount++;
        spiderEngagement = null;
    }

    void failSpiderEngagement(EventTime time, String detail)
    {
        if (!isOpen(spiderEngagement))
        {
            return;
        }
        opportunityLifecycle.fail(spiderEngagement.getInstanceId(), time, evidence("npc.state", time, detail));
        spiderEngagement = null;
    }

    void openBossReengagement(EventTime time, int spiderNpcId, int regionId)
    {
        if (opportunityLifecycle == null || activeActivityId == null || isOpen(bossReengagement))
        {
            return;
        }
        bossReengagement = opportunityLifecycle.start(
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
        opportunityLifecycle.complete(bossReengagement.getInstanceId(), time, evidence(evidenceType, time, detail));
        bossReengagementCount++;
        bossReengagement = null;
    }

    void openTargetReengagement(EventTime time, int regionId)
    {
        if (opportunityLifecycle == null || activeActivityId == null || isOpen(targetReengagement))
        {
            return;
        }
        targetReengagement = opportunityLifecycle.start(
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
        opportunityLifecycle.complete(targetReengagement.getInstanceId(), time, evidence(evidenceType, time, detail));
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

    @Override
    public void expireTimedOut(EventTime time)
    {
        if (opportunityLifecycle != null)
        {
            opportunityLifecycle.expireTimedOut(time);
        }
        clearIfTerminal();
    }

    @Override
    public void cancelOpenOpportunities(EventTime time, String detail)
    {
        if (opportunityLifecycle == null || activeActivityId == null)
        {
            return;
        }
        opportunityLifecycle.cancelOpenOpportunities(activeActivityId, time, evidence("region.instance", time, detail));
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

    @Override
    public void reset()
    {
        activeActivityId = null;
        opportunityLifecycle = null;
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
