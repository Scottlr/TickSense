package com.ticksense.common;

import java.util.Arrays;

public final class IntIdSet
{
    private static final IntIdSet EMPTY = new IntIdSet(new int[0]);

    private final int[] values;

    private IntIdSet(int[] values)
    {
        this.values = values.clone();
        Arrays.sort(this.values);
    }

    public static IntIdSet of(int... values)
    {
        if (values == null || values.length == 0)
        {
            return EMPTY;
        }
        return new IntIdSet(values);
    }

    public boolean contains(int value)
    {
        return Arrays.binarySearch(values, value) >= 0;
    }

    public int size()
    {
        return values.length;
    }

    public boolean isEmpty()
    {
        return values.length == 0;
    }

    public int[] toArray()
    {
        return values.clone();
    }

    public String joinCsv()
    {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++)
        {
            if (i > 0)
            {
                builder.append(',');
            }
            builder.append(values[i]);
        }
        return builder.toString();
    }
}
