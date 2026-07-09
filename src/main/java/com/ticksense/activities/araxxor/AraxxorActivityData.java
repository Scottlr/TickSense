package com.ticksense.activities.araxxor;

import com.ticksense.activities.ActivityReportAttributes;
import com.ticksense.common.TextValues;
import java.util.Map;

final class AraxxorActivityData
{
    private final String verificationStatus;
    private final int spiderEngagementCount;
    private final int bossReengagementCount;
    private final int targetReengagementCount;
    private final int spiderWindowDamage;
    private final int bossReengagementDamage;
    private final int targetReengagementDamage;

    AraxxorActivityData(
        String verificationStatus,
        int spiderEngagementCount,
        int bossReengagementCount,
        int targetReengagementCount,
        int spiderWindowDamage,
        int bossReengagementDamage,
        int targetReengagementDamage)
    {
        this.verificationStatus = TextValues.requireText(verificationStatus, "verificationStatus");
        this.spiderEngagementCount = spiderEngagementCount;
        this.bossReengagementCount = bossReengagementCount;
        this.targetReengagementCount = targetReengagementCount;
        this.spiderWindowDamage = spiderWindowDamage;
        this.bossReengagementDamage = bossReengagementDamage;
        this.targetReengagementDamage = targetReengagementDamage;
    }

    Map<String, String> toAttributes()
    {
        return ActivityReportAttributes.builder()
            .putText("verificationStatus", verificationStatus)
            .putInt("spiderEngagementCount", spiderEngagementCount)
            .putInt("bossReengagementCount", bossReengagementCount)
            .putInt("targetReengagementCount", targetReengagementCount)
            .putInt("spiderWindowDamage", spiderWindowDamage)
            .putInt("bossReengagementDamage", bossReengagementDamage)
            .putInt("targetReengagementDamage", targetReengagementDamage)
            .build()
            .asMap();
    }
}
