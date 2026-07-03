package com.ticksense.activities.araxxor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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
        this.verificationStatus = requireText(verificationStatus, "verificationStatus");
        this.spiderEngagementCount = spiderEngagementCount;
        this.bossReengagementCount = bossReengagementCount;
        this.targetReengagementCount = targetReengagementCount;
        this.spiderWindowDamage = spiderWindowDamage;
        this.bossReengagementDamage = bossReengagementDamage;
        this.targetReengagementDamage = targetReengagementDamage;
    }

    Map<String, String> toAttributes()
    {
        final Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("verificationStatus", verificationStatus);
        attributes.put("spiderEngagementCount", String.valueOf(spiderEngagementCount));
        attributes.put("bossReengagementCount", String.valueOf(bossReengagementCount));
        attributes.put("targetReengagementCount", String.valueOf(targetReengagementCount));
        attributes.put("spiderWindowDamage", String.valueOf(spiderWindowDamage));
        attributes.put("bossReengagementDamage", String.valueOf(bossReengagementDamage));
        attributes.put("targetReengagementDamage", String.valueOf(targetReengagementDamage));
        return Collections.unmodifiableMap(attributes);
    }

    private static String requireText(String value, String fieldName)
    {
        final String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
