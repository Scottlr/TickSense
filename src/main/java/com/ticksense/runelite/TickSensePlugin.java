package com.ticksense.runelite;

import com.google.inject.Provides;
import com.ticksense.activities.ActivityStrategyFactory;
import com.ticksense.activities.araxxor.AraxxorIds;
import com.ticksense.activities.araxxor.AraxxorStrategy;
import com.ticksense.activities.araxxor.AraxxorVerificationDecision;
import com.ticksense.analytics.TrendAnalyzer;
import com.ticksense.activities.construction.ConstructionIds;
import com.ticksense.activities.construction.ConstructionStrategy;
import com.ticksense.activities.gemmining.GemMiningIds;
import com.ticksense.activities.gemmining.GemMiningStrategy;
import com.ticksense.activities.inferno.InfernoIds;
import com.ticksense.activities.inferno.InfernoStrategy;
import com.ticksense.activities.vardorvis.VardorvisIds;
import com.ticksense.activities.vardorvis.VardorvisStrategy;
import com.ticksense.core.EntityRef;
import com.ticksense.core.WorldLocation;
import com.ticksense.storage.DeleteAllDataService;
import com.ticksense.storage.ExportBundleWriter;
import com.ticksense.storage.JsonReportRepository;
import com.ticksense.storage.ReportIndexMaintenanceService;
import com.ticksense.storage.ReportRepository;
import com.ticksense.storage.debug.DebugEventRecorder;
import com.ticksense.telemetry.TelemetryBus;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.ui.NotifyingReportRepository;
import com.ticksense.ui.TickSensePanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import lombok.extern.slf4j.Slf4j;
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
            tickSenseConfig,
            () -> services == null ? Collections.emptyList() : services.getStrategyEngine().getDiagnostics(),
            java.time.Clock.systemUTC());
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
    ActivityStrategyFactory provideActivityStrategyFactory()
    {
        return () ->
        {
            final List<com.ticksense.activities.ActivityStrategy> strategies = new java.util.ArrayList<>();
            if (GemMiningIds.verificationDecision().allowsStrategyEnablement())
            {
                strategies.add(new GemMiningStrategy());
            }
            if (ConstructionIds.verificationDecision().allowsStrategyEnablement())
            {
                strategies.add(new ConstructionStrategy());
            }
            if (AraxxorVerificationDecision.current().allowsNormalStrategyEnablement() && AraxxorIds.verifiedRegionIds().length > 0)
            {
                strategies.add(new AraxxorStrategy());
            }
            if (VardorvisIds.verificationDecision().allowsNormalReports())
            {
                strategies.add(new VardorvisStrategy());
            }
            if (InfernoIds.verificationDecision().allowsStrategyEnablement() && InfernoIds.verifiedRegionIds().length > 0)
            {
                strategies.add(new InfernoStrategy());
            }
            return strategies;
        };
    }

    @Provides
    TickSenseServices provideTickSenseServices(
        TelemetryBus telemetryBus,
        SessionTelemetryContext sessionTelemetryContext,
        ReportRepository reportRepository,
        ActivityStrategyFactory strategyFactory,
        TickSenseConfig tickSenseConfig)
    {
        try
        {
            return TickSenseServices.createForSession(
                telemetryBus,
                sessionTelemetryContext.getSessionId(),
                reportRepository,
                strategyFactory,
                tickSenseConfig.debugActivityDiagnostics());
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
        publish("NpcSpawned", envelope -> eventAdapter.mapNpcSpawned(event, envelope));
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event)
    {
        publish("NpcDespawned", envelope -> eventAdapter.mapNpcDespawned(event, envelope));
    }

    @Subscribe
    public void onNpcChanged(NpcChanged event)
    {
        publish("NpcChanged", envelope -> eventAdapter.mapNpcChanged(event, envelope));
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event)
    {
        publish("InteractingChanged", envelope -> eventAdapter.mapInteractingChanged(event, envelope));
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event)
    {
        publish("AnimationChanged", envelope -> eventAdapter.mapAnimationChanged(event, envelope));
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event)
    {
        publish("HitsplatApplied", envelope -> eventAdapter.mapHitsplatApplied(event, envelope));
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
        publish("ProjectileMoved", envelope -> eventAdapter.mapProjectileMoved(event, envelope));
    }

    @Subscribe
    public void onGraphicChanged(GraphicChanged event)
    {
        publish("GraphicChanged", envelope -> eventAdapter.mapGraphicChanged(event, envelope));
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        publishGameObjectSnapshot("GameObjectSpawned", event.getGameObject(), "AVAILABLE");
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event)
    {
        publishGameObjectSnapshot("GameObjectDespawned", event.getGameObject(), "DEPLETED");
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
