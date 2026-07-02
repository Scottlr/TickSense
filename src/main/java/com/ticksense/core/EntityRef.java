package com.ticksense.core;

import java.util.Objects;

public final class EntityRef
{
    private static final int UNKNOWN_INT = -1;

    public enum Type
    {
        LOCAL_PLAYER,
        PLAYER,
        NPC,
        OBJECT,
        ITEM,
        PROJECTILE,
        WIDGET,
        TILE,
        UNKNOWN
    }

    private final Type type;
    private final int runtimeIndex;
    private final int id;
    private final String name;
    private final int groupId;
    private final int childId;

    private EntityRef(Type type, int runtimeIndex, int id, String name, int groupId, int childId)
    {
        this.type = Objects.requireNonNull(type, "type");
        this.runtimeIndex = runtimeIndex;
        this.id = id;
        this.name = name == null ? "" : name;
        this.groupId = groupId;
        this.childId = childId;
    }

    public static EntityRef localPlayer()
    {
        return new EntityRef(Type.LOCAL_PLAYER, UNKNOWN_INT, UNKNOWN_INT, "local_player", UNKNOWN_INT, UNKNOWN_INT);
    }

    public static EntityRef npc(int runtimeIndex, int id, String name)
    {
        return new EntityRef(Type.NPC, runtimeIndex, id, name, UNKNOWN_INT, UNKNOWN_INT);
    }

    public static EntityRef widget(int groupId, int childId)
    {
        return new EntityRef(Type.WIDGET, UNKNOWN_INT, UNKNOWN_INT, "widget", groupId, childId);
    }

    public static EntityRef unknown()
    {
        return new EntityRef(Type.UNKNOWN, UNKNOWN_INT, UNKNOWN_INT, "unknown", UNKNOWN_INT, UNKNOWN_INT);
    }

    public Type getType()
    {
        return type;
    }

    public int getRuntimeIndex()
    {
        return runtimeIndex;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public int getGroupId()
    {
        return groupId;
    }

    public int getChildId()
    {
        return childId;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof EntityRef))
        {
            return false;
        }
        final EntityRef that = (EntityRef) other;
        return runtimeIndex == that.runtimeIndex
            && id == that.id
            && groupId == that.groupId
            && childId == that.childId
            && type == that.type
            && name.equals(that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, runtimeIndex, id, name, groupId, childId);
    }
}
