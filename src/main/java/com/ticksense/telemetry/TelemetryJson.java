package com.ticksense.telemetry;

import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.events.AnimationTelemetryEvent;
import com.ticksense.telemetry.events.ClientTickTelemetryEvent;
import com.ticksense.telemetry.events.DamageTelemetryEvent;
import com.ticksense.telemetry.events.EnvironmentTelemetryEvent;
import com.ticksense.telemetry.events.GameTickTelemetryEvent;
import com.ticksense.telemetry.events.GraphicTelemetryEvent;
import com.ticksense.telemetry.events.InteractingChangedTelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.MenuInteractionTelemetryEvent;
import com.ticksense.telemetry.events.MovementTelemetryEvent;
import com.ticksense.telemetry.events.NpcStateTelemetryEvent;
import com.ticksense.telemetry.events.ObjectStateTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import com.ticksense.telemetry.events.ProjectileTelemetryEvent;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import com.ticksense.telemetry.events.StatChangedTelemetryEvent;
import com.ticksense.telemetry.events.WidgetTelemetryEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TelemetryJson
{
    private TelemetryJson()
    {
    }

    public static String toJsonLine(TelemetryEnvelope envelope)
    {
        final TelemetryEvent event = Objects.requireNonNull(envelope, "envelope").getEvent();
        final Map<String, Object> root = new LinkedHashMap<>();
        root.put("schemaVersion", envelope.getSchemaVersion());
        root.put("eventId", envelope.getEventId());
        root.put("sessionId", envelope.getSessionId());
        root.put("type", event.getType());
        root.put("category", event.getCategory().name());
        root.put("time", eventTimeToMap(event.getTime()));
        root.put("tags", new LinkedHashMap<>(event.getTags()));
        root.put("payload", payloadToMap(event));
        return writeJson(root);
    }

    public static TelemetryEnvelope fromJsonLine(String jsonLine)
    {
        final Object parsed = new JsonParser(jsonLine).parse();
        final Map<String, Object> root = asMap(parsed, "root");
        final int schemaVersion = intField(root, "schemaVersion");
        TelemetrySchema.requireSupported(schemaVersion);

        final String eventId = stringField(root, "eventId");
        final String sessionId = stringField(root, "sessionId");
        final String type = stringField(root, "type");
        final TelemetryCategory category = TelemetryCategory.valueOf(stringField(root, "category"));
        final EventTime time = eventTimeFromMap(asMap(root.get("time"), "time"));
        final Map<String, String> tags = stringMap(root.get("tags"));
        final Map<String, Object> payload = asMap(root.get("payload"), "payload");
        final TelemetryEvent event = eventFromPayload(type, time, tags, payload);
        if (event.getCategory() != category)
        {
            throw new IllegalArgumentException("Telemetry category does not match type: " + type);
        }
        return new TelemetryEnvelope(schemaVersion, eventId, sessionId, event);
    }

    private static Map<String, Object> payloadToMap(TelemetryEvent event)
    {
        if (event instanceof GameTickTelemetryEvent)
        {
            final GameTickTelemetryEvent e = (GameTickTelemetryEvent) event;
            return mapOf("tick", e.getTick());
        }
        if (event instanceof ClientTickTelemetryEvent)
        {
            final ClientTickTelemetryEvent e = (ClientTickTelemetryEvent) event;
            return mapOf("clientTickSequence", e.getClientTickSequence());
        }
        if (event instanceof PlayerActionTelemetryEvent)
        {
            final PlayerActionTelemetryEvent e = (PlayerActionTelemetryEvent) event;
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("option", e.getOption());
            payload.put("target", e.getTarget());
            payload.put("targetRef", entityRefToMap(e.getTargetRef()));
            payload.put("actionKind", e.getActionKind());
            payload.put("location", worldLocationToMap(e.getLocation()));
            payload.put("menuActionId", e.getMenuActionId());
            return payload;
        }
        if (event instanceof MenuInteractionTelemetryEvent)
        {
            final MenuInteractionTelemetryEvent e = (MenuInteractionTelemetryEvent) event;
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("interactionType", e.getInteractionType());
            payload.put("entries", e.getEntries());
            payload.put("selectedOption", e.getSelectedOption());
            payload.put("target", e.getTarget());
            payload.put("identifier", e.getIdentifier());
            payload.put("param0", e.getParam0());
            payload.put("param1", e.getParam1());
            payload.put("widgetRef", entityRefToMap(e.getWidgetRef()));
            return payload;
        }
        if (event instanceof NpcStateTelemetryEvent)
        {
            final NpcStateTelemetryEvent e = (NpcStateTelemetryEvent) event;
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("npcRef", entityRefToMap(e.getNpcRef()));
            payload.put("stateChange", e.getStateChange());
            payload.put("location", worldLocationToMap(e.getLocation()));
            payload.put("animationId", e.getAnimationId());
            payload.put("graphicId", e.getGraphicId());
            payload.put("interactingRef", entityRefToMap(e.getInteractingRef()));
            payload.put("healthRatio", e.getHealthRatio());
            payload.put("healthScale", e.getHealthScale());
            return payload;
        }
        if (event instanceof ObjectStateTelemetryEvent)
        {
            final ObjectStateTelemetryEvent e = (ObjectStateTelemetryEvent) event;
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("objectId", e.getObjectId());
            payload.put("objectName", e.getObjectName());
            payload.put("location", worldLocationToMap(e.getLocation()));
            payload.put("objectType", e.getObjectType());
            payload.put("actions", e.getActions());
            payload.put("stateChange", e.getStateChange());
            return payload;
        }
        if (event instanceof InteractingChangedTelemetryEvent)
        {
            final InteractingChangedTelemetryEvent e = (InteractingChangedTelemetryEvent) event;
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("actorRef", entityRefToMap(e.getActorRef()));
            payload.put("interactingRef", entityRefToMap(e.getInteractingRef()));
            payload.put("stateChange", e.getStateChange());
            return payload;
        }
        if (event instanceof AnimationTelemetryEvent)
        {
            final AnimationTelemetryEvent e = (AnimationTelemetryEvent) event;
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("actorRef", entityRefToMap(e.getActorRef()));
            payload.put("animationId", e.getAnimationId());
            payload.put("previousAnimationId", e.getPreviousAnimationId());
            return payload;
        }
        if (event instanceof GraphicTelemetryEvent)
        {
            final GraphicTelemetryEvent e = (GraphicTelemetryEvent) event;
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("actorRef", entityRefToMap(e.getActorRef()));
            payload.put("graphicId", e.getGraphicId());
            payload.put("location", worldLocationToMap(e.getLocation()));
            return payload;
        }
        if (event instanceof ProjectileTelemetryEvent)
        {
            final ProjectileTelemetryEvent e = (ProjectileTelemetryEvent) event;
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("projectileId", e.getProjectileId());
            payload.put("sourceRef", entityRefToMap(e.getSourceRef()));
            payload.put("targetRef", entityRefToMap(e.getTargetRef()));
            payload.put("location", worldLocationToMap(e.getLocation()));
            payload.put("startCycle", e.getStartCycle());
            payload.put("endCycle", e.getEndCycle());
            return payload;
        }
        if (event instanceof DamageTelemetryEvent)
        {
            final DamageTelemetryEvent e = (DamageTelemetryEvent) event;
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("targetRef", entityRefToMap(e.getTargetRef()));
            payload.put("hitsplatType", e.getHitsplatType());
            payload.put("amount", e.getAmount());
            payload.put("healthRatio", e.getHealthRatio());
            payload.put("healthScale", e.getHealthScale());
            return payload;
        }
        if (event instanceof InventoryDeltaTelemetryEvent)
        {
            final InventoryDeltaTelemetryEvent e = (InventoryDeltaTelemetryEvent) event;
            final Map<String, Object> payload = new LinkedHashMap<>();
            final List<Object> deltas = new ArrayList<>();
            for (InventoryDeltaTelemetryEvent.ItemDelta delta : e.getDeltas())
            {
                final Map<String, Object> deltaMap = new LinkedHashMap<>();
                deltaMap.put("slot", delta.getSlot());
                deltaMap.put("beforeItemId", delta.getBeforeItemId());
                deltaMap.put("beforeQuantity", delta.getBeforeQuantity());
                deltaMap.put("afterItemId", delta.getAfterItemId());
                deltaMap.put("afterQuantity", delta.getAfterQuantity());
                if (!delta.getBeforeInventoryActions().isEmpty())
                {
                    deltaMap.put("beforeInventoryActions", delta.getBeforeInventoryActions());
                }
                deltas.add(deltaMap);
            }
            payload.put("containerId", e.getContainerId());
            payload.put("deltas", deltas);
            return payload;
        }
        if (event instanceof StatChangedTelemetryEvent)
        {
            final StatChangedTelemetryEvent e = (StatChangedTelemetryEvent) event;
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("skill", e.getSkill());
            payload.put("xp", e.getXp());
            payload.put("xpDelta", e.getXpDelta());
            payload.put("level", e.getLevel());
            payload.put("boostedLevel", e.getBoostedLevel());
            return payload;
        }
        if (event instanceof MovementTelemetryEvent)
        {
            final MovementTelemetryEvent e = (MovementTelemetryEvent) event;
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("entityRef", entityRefToMap(e.getEntityRef()));
            payload.put("fromLocation", worldLocationToMap(e.getFromLocation()));
            payload.put("toLocation", worldLocationToMap(e.getToLocation()));
            payload.put("movementKind", e.getMovementKind());
            payload.put("distanceTiles", e.getDistanceTiles());
            return payload;
        }
        if (event instanceof RegionInstanceTelemetryEvent)
        {
            final RegionInstanceTelemetryEvent e = (RegionInstanceTelemetryEvent) event;
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("world", e.getWorld());
            payload.put("regionId", e.getRegionId());
            payload.put("worldViewId", e.getWorldViewId());
            payload.put("instanced", e.isInstanced());
            payload.put("gameState", e.getGameState());
            payload.put("localPlayerLocation", worldLocationToMap(e.getLocalPlayerLocation()));
            return payload;
        }
        if (event instanceof WidgetTelemetryEvent)
        {
            final WidgetTelemetryEvent e = (WidgetTelemetryEvent) event;
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("groupId", e.getGroupId());
            payload.put("childId", e.getChildId());
            payload.put("itemId", e.getItemId());
            payload.put("text", e.getText());
            payload.put("eventKind", e.getEventKind());
            payload.put("visible", e.isVisible());
            payload.put("actions", e.getActions());
            return payload;
        }
        if (event instanceof EnvironmentTelemetryEvent)
        {
            final EnvironmentTelemetryEvent e = (EnvironmentTelemetryEvent) event;
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("fps", e.getFps());
            payload.put("world", e.getWorld());
            payload.put("gameState", e.getGameState());
            payload.put("pluginVersion", e.getPluginVersion());
            return payload;
        }
        throw new IllegalArgumentException("Unsupported telemetry event type: " + event.getType());
    }

    private static TelemetryEvent eventFromPayload(String type, EventTime time, Map<String, String> tags, Map<String, Object> payload)
    {
        if (GameTickTelemetryEvent.TYPE.equals(type))
        {
            return new GameTickTelemetryEvent(time, tags, intField(payload, "tick"));
        }
        if (ClientTickTelemetryEvent.TYPE.equals(type))
        {
            return new ClientTickTelemetryEvent(time, tags, intField(payload, "clientTickSequence"));
        }
        if (PlayerActionTelemetryEvent.TYPE.equals(type))
        {
            return new PlayerActionTelemetryEvent(
                time,
                tags,
                stringField(payload, "option"),
                stringField(payload, "target"),
                entityRefFromMap(asMap(payload.get("targetRef"), "targetRef")),
                stringField(payload, "actionKind"),
                worldLocationFromMap(asMap(payload.get("location"), "location")),
                intField(payload, "menuActionId"));
        }
        if (MenuInteractionTelemetryEvent.TYPE.equals(type))
        {
            return new MenuInteractionTelemetryEvent(
                time,
                tags,
                stringField(payload, "interactionType"),
                stringList(payload.get("entries")),
                stringField(payload, "selectedOption"),
                stringField(payload, "target"),
                intField(payload, "identifier"),
                intField(payload, "param0"),
                intField(payload, "param1"),
                entityRefFromMap(asMap(payload.get("widgetRef"), "widgetRef")));
        }
        if (NpcStateTelemetryEvent.TYPE.equals(type))
        {
            return new NpcStateTelemetryEvent(
                time,
                tags,
                entityRefFromMap(asMap(payload.get("npcRef"), "npcRef")),
                stringField(payload, "stateChange"),
                worldLocationFromMap(asMap(payload.get("location"), "location")),
                intField(payload, "animationId"),
                intField(payload, "graphicId"),
                entityRefFromMap(asMap(payload.get("interactingRef"), "interactingRef")),
                intField(payload, "healthRatio"),
                intField(payload, "healthScale"));
        }
        if (ObjectStateTelemetryEvent.TYPE.equals(type))
        {
            return new ObjectStateTelemetryEvent(
                time,
                tags,
                intField(payload, "objectId"),
                stringField(payload, "objectName"),
                worldLocationFromMap(asMap(payload.get("location"), "location")),
                stringField(payload, "objectType"),
                stringList(payload.get("actions")),
                stringField(payload, "stateChange"));
        }
        if (InteractingChangedTelemetryEvent.TYPE.equals(type))
        {
            return new InteractingChangedTelemetryEvent(
                time,
                tags,
                entityRefFromMap(asMap(payload.get("actorRef"), "actorRef")),
                entityRefFromMap(asMap(payload.get("interactingRef"), "interactingRef")),
                stringField(payload, "stateChange"));
        }
        if (AnimationTelemetryEvent.TYPE.equals(type))
        {
            return new AnimationTelemetryEvent(
                time,
                tags,
                entityRefFromMap(asMap(payload.get("actorRef"), "actorRef")),
                intField(payload, "animationId"),
                intField(payload, "previousAnimationId"));
        }
        if (GraphicTelemetryEvent.TYPE.equals(type))
        {
            return new GraphicTelemetryEvent(
                time,
                tags,
                entityRefFromMap(asMap(payload.get("actorRef"), "actorRef")),
                intField(payload, "graphicId"),
                worldLocationFromMap(asMap(payload.get("location"), "location")));
        }
        if (ProjectileTelemetryEvent.TYPE.equals(type))
        {
            return new ProjectileTelemetryEvent(
                time,
                tags,
                intField(payload, "projectileId"),
                entityRefFromMap(asMap(payload.get("sourceRef"), "sourceRef")),
                entityRefFromMap(asMap(payload.get("targetRef"), "targetRef")),
                worldLocationFromMap(asMap(payload.get("location"), "location")),
                intField(payload, "startCycle"),
                intField(payload, "endCycle"));
        }
        if (DamageTelemetryEvent.TYPE.equals(type))
        {
            return new DamageTelemetryEvent(
                time,
                tags,
                entityRefFromMap(asMap(payload.get("targetRef"), "targetRef")),
                intField(payload, "hitsplatType"),
                intField(payload, "amount"),
                intField(payload, "healthRatio"),
                intField(payload, "healthScale"));
        }
        if (InventoryDeltaTelemetryEvent.TYPE.equals(type))
        {
            return new InventoryDeltaTelemetryEvent(time, tags, intField(payload, "containerId"), itemDeltas(payload.get("deltas")));
        }
        if (StatChangedTelemetryEvent.TYPE.equals(type))
        {
            return new StatChangedTelemetryEvent(
                time,
                tags,
                stringField(payload, "skill"),
                intField(payload, "xp"),
                intField(payload, "xpDelta"),
                intField(payload, "level"),
                intField(payload, "boostedLevel"));
        }
        if (MovementTelemetryEvent.TYPE.equals(type))
        {
            return new MovementTelemetryEvent(
                time,
                tags,
                entityRefFromMap(asMap(payload.get("entityRef"), "entityRef")),
                worldLocationFromMap(asMap(payload.get("fromLocation"), "fromLocation")),
                worldLocationFromMap(asMap(payload.get("toLocation"), "toLocation")),
                stringField(payload, "movementKind"),
                nullableIntField(payload, "distanceTiles"));
        }
        if (RegionInstanceTelemetryEvent.TYPE.equals(type))
        {
            return new RegionInstanceTelemetryEvent(
                time,
                tags,
                intField(payload, "world"),
                intField(payload, "regionId"),
                stringField(payload, "worldViewId"),
                booleanField(payload, "instanced"),
                stringField(payload, "gameState"),
                worldLocationFromMap(asMap(payload.get("localPlayerLocation"), "localPlayerLocation")));
        }
        if (WidgetTelemetryEvent.TYPE.equals(type))
        {
            return new WidgetTelemetryEvent(
                time,
                tags,
                intField(payload, "groupId"),
                intField(payload, "childId"),
                intField(payload, "itemId"),
                stringField(payload, "text"),
                stringField(payload, "eventKind"),
                booleanField(payload, "visible"),
                stringList(payload.get("actions")));
        }
        if (EnvironmentTelemetryEvent.TYPE.equals(type))
        {
            return new EnvironmentTelemetryEvent(
                time,
                tags,
                intField(payload, "fps"),
                intField(payload, "world"),
                stringField(payload, "gameState"),
                stringField(payload, "pluginVersion"));
        }
        throw new IllegalArgumentException("Unknown telemetry event type: " + type);
    }

    private static Map<String, Object> eventTimeToMap(EventTime time)
    {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("wallTimeMillis", time.getWallTimeMillis());
        map.put("monotonicNanos", time.getMonotonicNanos());
        map.put("gameTick", time.getGameTick());
        map.put("clientCycle", time.getClientCycle());
        map.put("clientTickSequence", time.getClientTickSequence());
        return map;
    }

    private static EventTime eventTimeFromMap(Map<String, Object> map)
    {
        return new EventTime(
            longField(map, "wallTimeMillis"),
            longField(map, "monotonicNanos"),
            intField(map, "gameTick"),
            longField(map, "clientCycle"),
            intField(map, "clientTickSequence"));
    }

    private static Map<String, Object> entityRefToMap(EntityRef ref)
    {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", ref.getType().name());
        map.put("runtimeIndex", ref.getRuntimeIndex());
        map.put("id", ref.getId());
        map.put("name", ref.getName());
        map.put("groupId", ref.getGroupId());
        map.put("childId", ref.getChildId());
        return map;
    }

    private static EntityRef entityRefFromMap(Map<String, Object> map)
    {
        final EntityRef.Type type = EntityRef.Type.valueOf(stringField(map, "type"));
        switch (type)
        {
            case LOCAL_PLAYER:
                return EntityRef.localPlayer();
            case NPC:
                return EntityRef.npc(intField(map, "runtimeIndex"), intField(map, "id"), stringField(map, "name"));
            case WIDGET:
                return EntityRef.widget(intField(map, "groupId"), intField(map, "childId"));
            case UNKNOWN:
                return EntityRef.unknown();
            default:
                throw new IllegalArgumentException("Unsupported EntityRef type in telemetry JSON: " + type);
        }
    }

    private static Map<String, Object> worldLocationToMap(WorldLocation location)
    {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("world", location.getWorld());
        map.put("plane", location.getPlane());
        map.put("x", location.getX());
        map.put("y", location.getY());
        map.put("regionId", location.getRegionId());
        map.put("instanced", location.isInstanced());
        return map;
    }

    private static WorldLocation worldLocationFromMap(Map<String, Object> map)
    {
        return new WorldLocation(
            intField(map, "world"),
            intField(map, "plane"),
            intField(map, "x"),
            intField(map, "y"),
            intField(map, "regionId"),
            booleanField(map, "instanced"));
    }

    private static List<InventoryDeltaTelemetryEvent.ItemDelta> itemDeltas(Object value)
    {
        final List<?> raw = asList(value, "deltas");
        final List<InventoryDeltaTelemetryEvent.ItemDelta> deltas = new ArrayList<>();
        for (Object item : raw)
        {
            final Map<String, Object> map = asMap(item, "item delta");
            deltas.add(new InventoryDeltaTelemetryEvent.ItemDelta(
                intField(map, "slot"),
                intField(map, "beforeItemId"),
                intField(map, "beforeQuantity"),
                intField(map, "afterItemId"),
                intField(map, "afterQuantity"),
                stringList(map.get("beforeInventoryActions"))));
        }
        return deltas;
    }

    private static Map<String, Object> mapOf(String key, Object value)
    {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

    private static String writeJson(Object value)
    {
        final StringBuilder builder = new StringBuilder();
        writeValue(builder, value);
        return builder.toString();
    }

    private static void writeValue(StringBuilder builder, Object value)
    {
        if (value == null)
        {
            builder.append("null");
        }
        else if (value instanceof String)
        {
            writeString(builder, (String) value);
        }
        else if (value instanceof Number || value instanceof Boolean)
        {
            builder.append(value);
        }
        else if (value instanceof Map)
        {
            builder.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet())
            {
                if (!first)
                {
                    builder.append(',');
                }
                writeString(builder, String.valueOf(entry.getKey()));
                builder.append(':');
                writeValue(builder, entry.getValue());
                first = false;
            }
            builder.append('}');
        }
        else if (value instanceof Iterable)
        {
            builder.append('[');
            boolean first = true;
            for (Object item : (Iterable<?>) value)
            {
                if (!first)
                {
                    builder.append(',');
                }
                writeValue(builder, item);
                first = false;
            }
            builder.append(']');
        }
        else
        {
            throw new IllegalArgumentException("Unsupported JSON value: " + value.getClass().getName());
        }
    }

    private static void writeString(StringBuilder builder, String value)
    {
        builder.append('"');
        for (int i = 0; i < value.length(); i++)
        {
            final char c = value.charAt(i);
            switch (c)
            {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (c < 0x20)
                    {
                        builder.append(String.format("\\u%04x", (int) c));
                    }
                    else
                    {
                        builder.append(c);
                    }
                    break;
            }
        }
        builder.append('"');
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value, String fieldName)
    {
        if (!(value instanceof Map))
        {
            throw new IllegalArgumentException(fieldName + " must be a JSON object");
        }
        return (Map<String, Object>) value;
    }

    private static List<?> asList(Object value, String fieldName)
    {
        if (!(value instanceof List))
        {
            throw new IllegalArgumentException(fieldName + " must be a JSON array");
        }
        return (List<?>) value;
    }

    private static Map<String, String> stringMap(Object value)
    {
        if (value == null)
        {
            return Collections.emptyMap();
        }
        final Map<String, Object> raw = asMap(value, "tags");
        final Map<String, String> tags = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : raw.entrySet())
        {
            tags.put(entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }
        return tags;
    }

    private static List<String> stringList(Object value)
    {
        if (value == null)
        {
            return Collections.emptyList();
        }
        final List<?> raw = asList(value, "string list");
        final List<String> strings = new ArrayList<>();
        for (Object item : raw)
        {
            strings.add(item == null ? "" : String.valueOf(item));
        }
        return strings;
    }

    private static String stringField(Map<String, Object> map, String fieldName)
    {
        final Object value = map.get(fieldName);
        if (!(value instanceof String))
        {
            throw new IllegalArgumentException(fieldName + " must be a string");
        }
        return (String) value;
    }

    private static int intField(Map<String, Object> map, String fieldName)
    {
        final Object value = map.get(fieldName);
        if (!(value instanceof Number))
        {
            throw new IllegalArgumentException(fieldName + " must be a number");
        }
        return ((Number) value).intValue();
    }

    private static Integer nullableIntField(Map<String, Object> map, String fieldName)
    {
        final Object value = map.get(fieldName);
        if (value == null)
        {
            return null;
        }
        if (!(value instanceof Number))
        {
            throw new IllegalArgumentException(fieldName + " must be a number or null");
        }
        return ((Number) value).intValue();
    }

    private static long longField(Map<String, Object> map, String fieldName)
    {
        final Object value = map.get(fieldName);
        if (!(value instanceof Number))
        {
            throw new IllegalArgumentException(fieldName + " must be a number");
        }
        return ((Number) value).longValue();
    }

    private static boolean booleanField(Map<String, Object> map, String fieldName)
    {
        final Object value = map.get(fieldName);
        if (!(value instanceof Boolean))
        {
            throw new IllegalArgumentException(fieldName + " must be a boolean");
        }
        return (Boolean) value;
    }

    private static final class JsonParser
    {
        private final String input;
        private int position;

        private JsonParser(String input)
        {
            this.input = Objects.requireNonNull(input, "input");
        }

        private Object parse()
        {
            skipWhitespace();
            final Object value = parseValue();
            skipWhitespace();
            if (position != input.length())
            {
                throw error("Unexpected trailing input");
            }
            return value;
        }

        private Object parseValue()
        {
            skipWhitespace();
            if (position >= input.length())
            {
                throw error("Unexpected end of input");
            }
            final char c = input.charAt(position);
            if (c == '{')
            {
                return parseObject();
            }
            if (c == '[')
            {
                return parseArray();
            }
            if (c == '"')
            {
                return parseString();
            }
            if (c == 't')
            {
                expectLiteral("true");
                return Boolean.TRUE;
            }
            if (c == 'f')
            {
                expectLiteral("false");
                return Boolean.FALSE;
            }
            if (c == 'n')
            {
                expectLiteral("null");
                return null;
            }
            if (c == '-' || Character.isDigit(c))
            {
                return parseNumber();
            }
            throw error("Unexpected JSON value");
        }

        private Map<String, Object> parseObject()
        {
            expect('{');
            final Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (peek('}'))
            {
                expect('}');
                return map;
            }
            while (true)
            {
                skipWhitespace();
                final String key = parseString();
                skipWhitespace();
                expect(':');
                final Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (peek('}'))
                {
                    expect('}');
                    return map;
                }
                expect(',');
            }
        }

        private List<Object> parseArray()
        {
            expect('[');
            final List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (peek(']'))
            {
                expect(']');
                return list;
            }
            while (true)
            {
                list.add(parseValue());
                skipWhitespace();
                if (peek(']'))
                {
                    expect(']');
                    return list;
                }
                expect(',');
            }
        }

        private String parseString()
        {
            expect('"');
            final StringBuilder builder = new StringBuilder();
            while (position < input.length())
            {
                final char c = input.charAt(position++);
                if (c == '"')
                {
                    return builder.toString();
                }
                if (c == '\\')
                {
                    if (position >= input.length())
                    {
                        throw error("Unterminated escape sequence");
                    }
                    final char escaped = input.charAt(position++);
                    switch (escaped)
                    {
                        case '"':
                        case '\\':
                        case '/':
                            builder.append(escaped);
                            break;
                        case 'b':
                            builder.append('\b');
                            break;
                        case 'f':
                            builder.append('\f');
                            break;
                        case 'n':
                            builder.append('\n');
                            break;
                        case 'r':
                            builder.append('\r');
                            break;
                        case 't':
                            builder.append('\t');
                            break;
                        case 'u':
                            builder.append(parseUnicodeEscape());
                            break;
                        default:
                            throw error("Unsupported escape sequence");
                    }
                }
                else
                {
                    builder.append(c);
                }
            }
            throw error("Unterminated string");
        }

        private char parseUnicodeEscape()
        {
            if (position + 4 > input.length())
            {
                throw error("Invalid unicode escape");
            }
            final String hex = input.substring(position, position + 4);
            position += 4;
            try
            {
                return (char) Integer.parseInt(hex, 16);
            }
            catch (NumberFormatException ex)
            {
                throw error("Invalid unicode escape");
            }
        }

        private Number parseNumber()
        {
            final int start = position;
            if (peek('-'))
            {
                position++;
            }
            while (position < input.length() && Character.isDigit(input.charAt(position)))
            {
                position++;
            }
            if (position < input.length() && input.charAt(position) == '.')
            {
                position++;
                while (position < input.length() && Character.isDigit(input.charAt(position)))
                {
                    position++;
                }
                return Double.valueOf(input.substring(start, position));
            }
            if (position < input.length() && (input.charAt(position) == 'e' || input.charAt(position) == 'E'))
            {
                position++;
                if (position < input.length() && (input.charAt(position) == '+' || input.charAt(position) == '-'))
                {
                    position++;
                }
                while (position < input.length() && Character.isDigit(input.charAt(position)))
                {
                    position++;
                }
                return Double.valueOf(input.substring(start, position));
            }
            return Long.valueOf(input.substring(start, position));
        }

        private boolean peek(char expected)
        {
            return position < input.length() && input.charAt(position) == expected;
        }

        private void expect(char expected)
        {
            skipWhitespace();
            if (position >= input.length() || input.charAt(position) != expected)
            {
                throw error("Expected '" + expected + "'");
            }
            position++;
        }

        private void expectLiteral(String literal)
        {
            if (!input.startsWith(literal, position))
            {
                throw error("Expected " + literal);
            }
            position += literal.length();
        }

        private void skipWhitespace()
        {
            while (position < input.length() && Character.isWhitespace(input.charAt(position)))
            {
                position++;
            }
        }

        private IllegalArgumentException error(String message)
        {
            return new IllegalArgumentException(message + " at offset " + position);
        }
    }
}
