package com.ticksense.core;

import java.util.Objects;

public final class WorldLocation
{
    private static final int UNKNOWN_INT = -1;

    private final int world;
    private final int plane;
    private final int x;
    private final int y;
    private final int regionId;
    private final boolean instanced;

    public WorldLocation(int world, int plane, int x, int y, int regionId, boolean instanced)
    {
        this.world = world;
        this.plane = plane;
        this.x = x;
        this.y = y;
        this.regionId = regionId;
        this.instanced = instanced;
    }

    public static WorldLocation unknown()
    {
        return new WorldLocation(UNKNOWN_INT, UNKNOWN_INT, UNKNOWN_INT, UNKNOWN_INT, UNKNOWN_INT, false);
    }

    public int getWorld()
    {
        return world;
    }

    public int getPlane()
    {
        return plane;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getRegionId()
    {
        return regionId;
    }

    public boolean isInstanced()
    {
        return instanced;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof WorldLocation))
        {
            return false;
        }
        final WorldLocation that = (WorldLocation) other;
        return world == that.world
            && plane == that.plane
            && x == that.x
            && y == that.y
            && regionId == that.regionId
            && instanced == that.instanced;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(world, plane, x, y, regionId, instanced);
    }
}
