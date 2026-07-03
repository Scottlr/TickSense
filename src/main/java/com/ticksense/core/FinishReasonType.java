package com.ticksense.core;

import java.util.Locale;

public enum FinishReasonType
{
    COMPLETED,
    BOSS_DEAD,
    PLAYER_DEAD,
    TELEPORTED,
    LOGGED_OUT,
    HOPPED_WORLD,
    LEFT_REGION,
    LEFT_INSTANCE,
    INVENTORY_EXHAUSTED,
    BANK_OPENED,
    IDLE_TIMEOUT,
    ACTIVITY_CHANGED,
    ROOM_COMPLETE,
    WAVE_COMPLETE,
    REWARD_RECEIVED,
    CLIENT_SHUTDOWN,
    AMBIGUOUS_CONTEXT_LOST,
    UNKNOWN;

    public String displayName()
    {
        final String[] words = name().split("_");
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++)
        {
            if (i > 0)
            {
                builder.append(' ');
            }
            final String word = words[i].toLowerCase(Locale.US);
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.toString();
    }
}
