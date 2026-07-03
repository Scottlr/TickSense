package com.ticksense.common;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IntIdSetTest
{
    @Test
    public void containsAndCopiesIds()
    {
        final int[] ids = {42, 7, 99};
        final IntIdSet idSet = IntIdSet.of(ids);
        ids[0] = 1;

        assertTrue(idSet.contains(42));
        assertTrue(idSet.contains(7));
        assertFalse(idSet.contains(1));
        assertEquals(3, idSet.size());
        assertEquals("7,42,99", idSet.joinCsv());

        final int[] copied = idSet.toArray();
        copied[0] = 1000;
        assertArrayEquals(new int[] {7, 42, 99}, idSet.toArray());
    }

    @Test
    public void emptySetIsReusable()
    {
        final IntIdSet idSet = IntIdSet.of();

        assertTrue(idSet.isEmpty());
        assertFalse(idSet.contains(1));
        assertArrayEquals(new int[0], idSet.toArray());
    }
}
