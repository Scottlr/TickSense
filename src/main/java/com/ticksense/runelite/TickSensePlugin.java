package com.ticksense.runelite;

import com.google.gson.Gson;
import com.google.inject.Provides;
import com.ticksense.activities.ActivityModule;
import com.ticksense.activities.ActivityModuleCatalog;
import com.ticksense.activities.ActivityStrategyFactory;
import com.ticksense.analytics.TrendAnalyzer;
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.storage.DeleteAllDataService;
import com.ticksense.storage.ExportBundleWriter;
import com.ticksense.storage.ExportConfigSnapshotProvider;
import com.ticksense.storage.JsonReportRepository;
import com.ticksense.storage.ReportIndexMaintenanceService;
import com.ticksense.storage.ReportRepository;
import com.ticksense.storage.debug.DebugEventKind;
import com.ticksense.storage.debug.DebugEventRecorder;
import com.ticksense.telemetry.StateChanges;
import com.ticksense.telemetry.TelemetryBus;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.SessionIdProvider;
import com.ticksense.ui.NotifyingReportRepository;
import com.ticksense.ui.TickSensePanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PostClientTick;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetDrag;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.events.WorldViewLoaded;
import net.runelite.api.events.WorldViewUnloaded;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;
import net.runelite.api.Projectile;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.api.GameObject;
import net.runelite.api.ObjectComposition;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@PluginDescriptor(name = "TickSense")
public class TickSensePlugin extends Plugin
{
    private static final int NAV_ICON_SIZE = 16;
    private static final int UNKNOWN = -1;
    private static final Gson DEBUG_GSON = new Gson();

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private Client client;

    @Inject
    private RuneLiteEventCapture eventCapture;

    @Inject
    private RuneLiteEventAdapter eventAdapter;

    @Inject
    private TelemetryBus telemetryBus;

    @Inject
    private RuneLiteSnapshotter snapshotter;

    @Inject
    private SessionTelemetryContext sessionTelemetryContext;

    @Inject
    private DebugEventRecorder debugEventRecorder;

    @Inject
    private TickSenseConfig config;

    @Inject
    private Provider<TickSenseServices> tickSenseServicesProvider;

    @Inject
    private DeleteAllDataService deleteAllDataService;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ExportBundleWriter exportBundleWriter;

    @Inject
    private ReportIndexMaintenanceService reportIndexMaintenanceService;

    private TickSensePanel panel;
    private NavigationButton navButton;
    private TickSenseServices services;
    private WorldLocation lastLocalPlayerLocation = WorldLocation.unknown();

    @Override
    protected void startUp()
    {
        sessionTelemetryContext.resetSession();
        debugEventRecorder.startSession(
            config.debugEventRecorder(),
            config.maxDebugFileSizeMb(),
            config.maxDebugSessions());
        if (debugEventRecorder.isActive())
        {
            telemetryBus.addSink(debugEventRecorder);
        }
        services = tickSenseServicesProvider.get();
        services.start();
        lastLocalPlayerLocation = WorldLocation.unknown();
        panel = new TickSensePanel(
            services,
            services.getReportRepository(),
            deleteAllDataService,
            exportBundleWriter,
            reportIndexMaintenanceService,
            new TrendAnalyzer(),
            configManager,
            config);
        panel.initialize();
        navButton = NavigationButton.builder()
            .tooltip("TickSense")
            .icon(createNavigationIcon())
            .panel(panel)
            .priority(10)
            .build();
        clientToolbar.addNavigation(navButton);
        log.debug("TickSense started");
    }

    @Override
    protected void shutDown()
    {
        telemetryBus.removeSink(debugEventRecorder);
        debugEventRecorder.close();
        lastLocalPlayerLocation = WorldLocation.unknown();

        if (services != null)
        {
            try
            {
                services.close();
            }
            catch (IOException ex)
            {
                log.warn("TickSense could not close services cleanly", ex);
            }
            services = null;
        }

        if (navButton != null)
        {
            clientToolbar.removeNavigation(navButton);
            navButton = null;
        }

        panel = null;
        log.debug("TickSense stopped");
    }

    @Provides
    TickSenseConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(TickSenseConfig.class);
    }

    @Provides
    ReportRepository provideReportRepository()
    {
        return new NotifyingReportRepository(new JsonReportRepository());
    }

    @Provides
    DeleteAllDataService provideDeleteAllDataService()
    {
        return new DeleteAllDataService();
    }

    @Provides
    ExportBundleWriter provideExportBundleWriter(ReportRepository reportRepository, TickSenseConfig tickSenseConfig)
    {
        return new ExportBundleWriter(
            com.ticksense.storage.TickSenseDataPaths.defaultPaths(),
            reportRepository,
            new com.google.gson.Gson(),
            configSnapshotProvider(tickSenseConfig),
            () -> services == null ? Collections.emptyList() : services.getStrategyEngine().getDiagnostics(),
            java.time.Clock.systemUTC());
    }

    private static ExportConfigSnapshotProvider configSnapshotProvider(TickSenseConfig tickSenseConfig)
    {
        return () ->
        {
            final Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("debugEventRecorder", tickSenseConfig.debugEventRecorder());
            snapshot.put("debugActivityDiagnostics", tickSenseConfig.debugActivityDiagnostics());
            snapshot.put("maxDebugFileSizeMb", tickSenseConfig.maxDebugFileSizeMb());
            snapshot.put("maxDebugSessions", tickSenseConfig.maxDebugSessions());
            return snapshot;
        };
    }

    @Provides
    ReportIndexMaintenanceService provideReportIndexMaintenanceService(ReportRepository reportRepository)
    {
        final ReportRepository normalizedRepository = reportRepository instanceof NotifyingReportRepository
            ? ((NotifyingReportRepository) reportRepository).getDelegate()
            : reportRepository;
        if (!(normalizedRepository instanceof JsonReportRepository))
        {
            throw new IllegalStateException("TickSense report index maintenance requires JsonReportRepository");
        }
        return new ReportIndexMaintenanceService(
            com.ticksense.storage.TickSenseDataPaths.defaultPaths(),
            (JsonReportRepository) normalizedRepository,
            java.time.Clock.systemUTC());
    }

    @Provides
    List<ActivityModule> provideActivityModules()
    {
        return ActivityModuleCatalog.productionModules();
    }

    @Provides
    ActivityStrategyFactory provideActivityStrategyFactory(List<ActivityModule> activityModules)
    {
        return ActivityModuleCatalog.strategyFactory(activityModules);
    }

    @Provides
    SessionIdProvider provideSessionIdProvider(SessionTelemetryContext sessionTelemetryContext)
    {
        return sessionTelemetryContext;
    }

    @Provides
    TickSenseServices provideTickSenseServices(
        TelemetryBus telemetryBus,
        SessionTelemetryContext sessionTelemetryContext,
        ReportRepository reportRepository,
        List<ActivityModule> activityModules,
        TickSenseConfig tickSenseConfig,
        DebugEventRecorder debugEventRecorder)
    {
        try
        {
            return TickSenseServices.createForSession(
                telemetryBus,
                sessionTelemetryContext.getSessionId(),
                reportRepository,
                activityModules,
                tickSenseConfig.debugActivityDiagnostics(),
                debugEventRecorder);
        }
        catch (IOException ex)
        {
            throw new IllegalStateException("TickSense could not initialize local timeline storage", ex);
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        final RuneLiteEventEnvelope envelope = eventCapture.onGameTickEnvelope();
        publish(envelope, captured -> eventAdapter.mapGameTick(event, captured));
        publishMovementSnapshot(envelope);
    }

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        publish(eventCapture.onClientTickEnvelope(), envelope -> eventAdapter.mapClientTick(event, envelope));
    }

    @Subscribe
    public void onPostClientTick(PostClientTick event)
    {
        publish("PostClientTick", envelope -> eventAdapter.mapPostClientTick(event, envelope));
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        publish("GameStateChanged", envelope -> eventAdapter.mapGameStateChanged(event, envelope));
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        publish("MenuOptionClicked", envelope -> eventAdapter.mapMenuOptionClicked(event, envelope));
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        publish("MenuEntryAdded", envelope -> eventAdapter.mapMenuEntryAdded(event, envelope));
    }

    @Subscribe
    public void onMenuOpened(MenuOpened event)
    {
        publish("MenuOpened", envelope -> eventAdapter.mapMenuOpened(event, envelope));
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        final RuneLiteEventEnvelope envelope = eventCapture.captureEnvelope("NpcSpawned");
        recordNpcObservation("NpcSpawned", envelope, event.getNpc(), "SPAWNED");
        publish(envelope, captured -> eventAdapter.mapNpcSpawned(event, captured));
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event)
    {
        final RuneLiteEventEnvelope envelope = eventCapture.captureEnvelope("NpcDespawned");
        recordNpcObservation("NpcDespawned", envelope, event.getNpc(), "DESPAWNED");
        publish(envelope, captured -> eventAdapter.mapNpcDespawned(event, captured));
    }

    @Subscribe
    public void onNpcChanged(NpcChanged event)
    {
        final RuneLiteEventEnvelope envelope = eventCapture.captureEnvelope("NpcChanged");
        recordNpcObservation("NpcChanged", envelope, event.getNpc(), "CHANGED");
        publish(envelope, captured -> eventAdapter.mapNpcChanged(event, captured));
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event)
    {
        publish("InteractingChanged", envelope -> eventAdapter.mapInteractingChanged(event, envelope));
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event)
    {
        final RuneLiteEventEnvelope envelope = eventCapture.captureEnvelope("AnimationChanged");
        recordActorObservation("AnimationChanged", envelope, event.getActor(), "animationId", event.getActor() == null ? UNKNOWN : event.getActor().getAnimation());
        publish(envelope, captured -> eventAdapter.mapAnimationChanged(event, captured));
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event)
    {
        final RuneLiteEventEnvelope envelope = eventCapture.captureEnvelope("HitsplatApplied");
        recordHitsplatObservation(envelope, event.getActor(), event.getHitsplat());
        publish(envelope, captured -> eventAdapter.mapHitsplatApplied(event, captured));
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        publish("ItemContainerChanged", envelope -> eventAdapter.mapItemContainerChanged(event, envelope));
    }

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        publish("StatChanged", envelope -> eventAdapter.mapStatChanged(event, envelope));
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        publish("WidgetLoaded", envelope -> eventAdapter.mapWidgetLoaded(event, envelope));
    }

    @Subscribe
    public void onWidgetClosed(WidgetClosed event)
    {
        publish("WidgetClosed", envelope -> eventAdapter.mapWidgetClosed(event, envelope));
    }

    @Subscribe
    public void onWidgetDrag(WidgetDrag event)
    {
        eventCapture.capture("WidgetDrag");
    }

    @Subscribe
    public void onWorldViewLoaded(WorldViewLoaded event)
    {
        publish("WorldViewLoaded", envelope -> eventAdapter.mapWorldViewLoaded(event, envelope));
    }

    @Subscribe
    public void onWorldViewUnloaded(WorldViewUnloaded event)
    {
        publish("WorldViewUnloaded", envelope -> eventAdapter.mapWorldViewUnloaded(event, envelope));
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event)
    {
        final RuneLiteEventEnvelope envelope = eventCapture.captureEnvelope("ProjectileMoved");
        recordProjectileObservation(envelope, event.getProjectile());
        publish(envelope, captured -> eventAdapter.mapProjectileMoved(event, captured));
    }

    @Subscribe
    public void onGraphicChanged(GraphicChanged event)
    {
        final RuneLiteEventEnvelope envelope = eventCapture.captureEnvelope("GraphicChanged");
        recordActorObservation("GraphicChanged", envelope, event.getActor(), "graphicId", event.getActor() == null ? UNKNOWN : event.getActor().getGraphic());
        publish(envelope, captured -> eventAdapter.mapGraphicChanged(event, captured));
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        publishGameObjectSnapshot("GameObjectSpawned", event.getGameObject(), StateChanges.AVAILABLE);
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event)
    {
        publishGameObjectSnapshot("GameObjectDespawned", event.getGameObject(), StateChanges.DEPLETED);
    }

    private void publish(String sourceEventType, Function<RuneLiteEventEnvelope, Optional<TelemetryEnvelope>> mapper)
    {
        publish(eventCapture.captureEnvelope(sourceEventType), mapper);
    }

    private void publish(RuneLiteEventEnvelope envelope, Function<RuneLiteEventEnvelope, Optional<TelemetryEnvelope>> mapper)
    {
        mapper.apply(envelope).ifPresent(telemetryBus::accept);
    }

    private void publishMovementSnapshot(RuneLiteEventEnvelope envelope)
    {
        final WorldLocation currentLocation = snapshotter.localPlayerLocation(envelope);
        if (WorldLocation.unknown().equals(currentLocation))
        {
            lastLocalPlayerLocation = currentLocation;
            return;
        }
        if (!WorldLocation.unknown().equals(lastLocalPlayerLocation) && !lastLocalPlayerLocation.equals(currentLocation))
        {
            final int distanceTiles = Math.max(
                Math.abs(lastLocalPlayerLocation.getX() - currentLocation.getX()),
                Math.abs(lastLocalPlayerLocation.getY() - currentLocation.getY()));
            eventAdapter.mapMovementSnapshot(
                "MovementSnapshot",
                EntityRef.localPlayer(),
                lastLocalPlayerLocation,
                currentLocation,
                "WALK",
                distanceTiles,
                envelope).ifPresent(telemetryBus::accept);
        }
        lastLocalPlayerLocation = currentLocation;
    }

    private void publishGameObjectSnapshot(String sourceEventType, GameObject gameObject, String stateChange)
    {
        if (gameObject == null)
        {
            return;
        }

        final RuneLiteEventEnvelope envelope = eventCapture.captureEnvelope(sourceEventType);
        final ObjectComposition objectDefinition = objectDefinition(gameObject.getId());
        final String objectName = objectDefinition == null ? "" : objectDefinition.getName();
        final List<String> actions = objectDefinition == null || objectDefinition.getActions() == null
            ? Collections.emptyList()
            : Arrays.asList(objectDefinition.getActions());
        recordGameObjectObservation(sourceEventType, envelope, gameObject, objectName, filterActions(actions), stateChange);

        eventAdapter.mapObjectSnapshot(
            "ObjectSnapshot",
            gameObject.getId(),
            objectName,
            snapshotter.worldLocation(gameObject.getWorldLocation(), envelope, false),
            "GAME_OBJECT",
            filterActions(actions),
            stateChange,
            envelope).ifPresent(telemetryBus::accept);
    }

    private void recordNpcObservation(String sourceEventType, RuneLiteEventEnvelope envelope, NPC npc, String stateChange)
    {
        final Map<String, Object> payload = baseObservationPayload(envelope);
        payload.put("stateChange", stateChange);
        payload.put("npcRef", entityRefPayload(snapshotter.npcRef(npc)));
        payload.put("location", locationPayload(snapshotter.actorLocation(npc, envelope)));
        payload.put("animationId", npc == null ? UNKNOWN : npc.getAnimation());
        payload.put("graphicId", npc == null ? UNKNOWN : npc.getGraphic());
        payload.put("interactingRef", npc == null ? entityRefPayload(EntityRef.unknown()) : entityRefPayload(snapshotter.actorRef(npc.getInteracting())));
        payload.put("healthRatio", npc == null ? UNKNOWN : npc.getHealthRatio());
        payload.put("healthScale", npc == null ? UNKNOWN : npc.getHealthScale());
        recordAdapterObservation(sourceEventType, envelope, payload);
    }

    private void recordActorObservation(String sourceEventType, RuneLiteEventEnvelope envelope, Actor actor, String idFieldName, int id)
    {
        final Map<String, Object> payload = baseObservationPayload(envelope);
        payload.put("actorRef", entityRefPayload(snapshotter.actorRef(actor)));
        payload.put("location", locationPayload(snapshotter.actorLocation(actor, envelope)));
        payload.put(idFieldName, id);
        recordAdapterObservation(sourceEventType, envelope, payload);
    }

    private void recordHitsplatObservation(RuneLiteEventEnvelope envelope, Actor actor, Hitsplat hitsplat)
    {
        final Map<String, Object> payload = baseObservationPayload(envelope);
        payload.put("targetRef", entityRefPayload(snapshotter.actorRef(actor)));
        payload.put("location", locationPayload(snapshotter.actorLocation(actor, envelope)));
        payload.put("hitsplatType", hitsplat == null ? UNKNOWN : hitsplat.getHitsplatType());
        payload.put("amount", hitsplat == null ? 0 : hitsplat.getAmount());
        payload.put("healthRatio", actor == null ? UNKNOWN : actor.getHealthRatio());
        payload.put("healthScale", actor == null ? UNKNOWN : actor.getHealthScale());
        recordAdapterObservation("HitsplatApplied", envelope, payload);
    }

    private void recordProjectileObservation(RuneLiteEventEnvelope envelope, Projectile projectile)
    {
        final Map<String, Object> payload = baseObservationPayload(envelope);
        payload.put("projectileId", projectile == null ? UNKNOWN : projectile.getId());
        payload.put("sourceRef", projectile == null ? entityRefPayload(EntityRef.unknown()) : entityRefPayload(snapshotter.actorRef(projectile.getSourceActor())));
        payload.put("targetRef", projectile == null ? entityRefPayload(EntityRef.unknown()) : entityRefPayload(snapshotter.actorRef(projectile.getTargetActor())));
        payload.put("targetLocation", projectile == null ? locationPayload(WorldLocation.unknown()) : locationPayload(snapshotter.worldLocation(projectile.getTargetPoint(), envelope, false)));
        payload.put("startCycle", projectile == null ? UNKNOWN : projectile.getStartCycle());
        payload.put("endCycle", projectile == null ? UNKNOWN : projectile.getEndCycle());
        recordAdapterObservation("ProjectileMoved", envelope, payload);
    }

    private void recordGameObjectObservation(
        String sourceEventType,
        RuneLiteEventEnvelope envelope,
        GameObject gameObject,
        String objectName,
        List<String> actions,
        String stateChange)
    {
        final Map<String, Object> payload = baseObservationPayload(envelope);
        payload.put("stateChange", stateChange);
        payload.put("objectId", gameObject.getId());
        payload.put("objectName", objectName);
        payload.put("location", locationPayload(snapshotter.worldLocation(gameObject.getWorldLocation(), envelope, false)));
        payload.put("objectType", "GAME_OBJECT");
        payload.put("actions", actions);
        recordAdapterObservation(sourceEventType, envelope, payload);
    }

    private void recordAdapterObservation(String sourceEventType, RuneLiteEventEnvelope envelope, Map<String, Object> payload)
    {
        if (!debugEventRecorder.isActive())
        {
            return;
        }
        final EventTime time = snapshotter.eventTime(envelope);
        debugEventRecorder.record(
            DebugEventKind.ADAPTER_OBSERVATION,
            sessionTelemetryContext.getSessionId(),
            sourceEventType,
            time,
            DEBUG_GSON.toJson(payload));
    }

    private static Map<String, Object> baseObservationPayload(RuneLiteEventEnvelope envelope)
    {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sourceEventType", envelope.getSourceEventType());
        payload.put("world", envelope.getWorld());
        payload.put("gameTick", envelope.getGameTick());
        payload.put("clientCycle", envelope.getClientCycle());
        payload.put("clientTickSequence", envelope.getClientTickSequence());
        payload.put("gameState", envelope.getGameState());
        return payload;
    }

    private static Map<String, Object> entityRefPayload(EntityRef ref)
    {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", ref.getType().name());
        payload.put("runtimeIndex", ref.getRuntimeIndex());
        payload.put("id", ref.getId());
        payload.put("name", ref.getName());
        payload.put("groupId", ref.getGroupId());
        payload.put("childId", ref.getChildId());
        return payload;
    }

    private static Map<String, Object> locationPayload(WorldLocation location)
    {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("world", location.getWorld());
        payload.put("plane", location.getPlane());
        payload.put("x", location.getX());
        payload.put("y", location.getY());
        payload.put("regionId", location.getRegionId());
        payload.put("instanced", location.isInstanced());
        return payload;
    }

    private ObjectComposition objectDefinition(int objectId)
    {
        try
        {
            return objectId < 0 ? null : client.getObjectDefinition(objectId);
        }
        catch (RuntimeException ex)
        {
            log.debug("Ignoring object definition lookup failure for {}", objectId, ex);
            return null;
        }
    }

    private static List<String> filterActions(List<String> actions)
    {
        if (actions == null || actions.isEmpty())
        {
            return Collections.emptyList();
        }
        final java.util.ArrayList<String> filtered = new java.util.ArrayList<>();
        for (String action : actions)
        {
            if (action != null && !action.trim().isEmpty())
            {
                filtered.add(action);
            }
        }
        return filtered;
    }

    private static BufferedImage createNavigationIcon()
    {
        final BufferedImage icon = new BufferedImage(NAV_ICON_SIZE, NAV_ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = icon.createGraphics();
        try
        {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(36, 34, 30, 255));
            graphics.fillOval(0, 0, NAV_ICON_SIZE - 1, NAV_ICON_SIZE - 1);
            graphics.setColor(new Color(255, 152, 31, 255));
            graphics.drawOval(1, 1, NAV_ICON_SIZE - 3, NAV_ICON_SIZE - 3);
            graphics.drawLine(8, 3, 8, 8);
            graphics.drawLine(8, 8, 12, 10);
        }
        finally
        {
            graphics.dispose();
        }
        return icon;
    }
}
