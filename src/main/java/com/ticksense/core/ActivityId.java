package com.ticksense.core;

public final class ActivityId
{
    private final String value;

    public ActivityId(String value)
    {
        this.value = CoreTexts.requireText(value, "value");
    }

    public static ActivityId of(String value)
    {
        return new ActivityId(value);
    }

    public static ActivityId parse(String value)
    {
        return new ActivityId(value);
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return value;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof ActivityId))
        {
            return false;
        }
        final ActivityId that = (ActivityId) other;
        return value.equals(that.value);
    }

    @Override
    public int hashCode()
    {
        return value.hashCode();
    }
}
