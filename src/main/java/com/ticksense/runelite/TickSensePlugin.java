package com.ticksense.runelite;

import com.google.inject.Provides;
import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.ActivityMarkerSink;
import com.ticksense.activities.ActivityRegistry;
import com.ticksense.activities.ActivityStrategyEngine;
import com.ticksense.activities.gemmining.GemMiningIds;
import com.ticksense.activities.gemmining.GemMiningStrategy;
import com.ticksense.activities.OpportunitySink;
import com.ticksense.storage.DeleteAllDataService;
import com.ticksense.storage.JsonReportRepository;
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
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
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
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
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
    private RuneLiteEventCapture eventCapture;

    @Inject
    private RuneLiteEventAdapter eventAdapter;

    @Inject
    private TelemetryBus telemetryBus;

    @Inject
    private SessionTelemetryContext sessionTelemetryContext;

    @Inject
    private DebugEventRecorder debugEventRecorder;

    @Inject
    private TickSenseConfig config;

    @Inject
    private ActivityStrategyEngine activityStrategyEngine;

    @Inject
    private ReportRepository reportRepository;

    @Inject
    private DeleteAllDataService deleteAllDataService;

    @Inject
    private ConfigManager configManager;

    private TickSensePanel panel;
    private NavigationButton navButton;

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
        telemetryBus.addSink(activityStrategyEngine);
        panel = new TickSensePanel(reportRepository, deleteAllDataService, configManager, config);
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
        telemetryBus.removeSink(activityStrategyEngine);
        debugEventRecorder.close();

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
    ActivityRegistry provideActivityRegistry()
    {
        final ActivityRegistry.Builder builder = ActivityRegistry.builder();
        if (GemMiningIds.verificationDecision().allowsStrategyEnablement())
        {
            builder.register(new GemMiningStrategy());
        }
        return builder.build();
    }

    @Provides
    ActivityMarkerSink provideActivityMarkerSink()
    {
        return new ActivityMarkerSink()
        {
            @Override
            public void accept(ActivityMarker marker)
            {
            }
        };
    }

    @Provides
    OpportunitySink provideOpportunitySink()
    {
        return marker -> { };
    }

    @Provides
    ActivityStrategyEngine provideActivityStrategyEngine(
        ActivityRegistry registry,
        ActivityMarkerSink activityMarkerSink,
        OpportunitySink opportunitySink,
        TickSenseConfig tickSenseConfig)
    {
        return new ActivityStrategyEngine(registry, activityMarkerSink, opportunitySink, tickSenseConfig.debugActivityDiagnostics());
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        publish(eventCapture.onGameTickEnvelope(), envelope -> eventAdapter.mapGameTick(event, envelope));
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

    private void publish(String sourceEventType, Function<RuneLiteEventEnvelope, Optional<TelemetryEnvelope>> mapper)
    {
        publish(eventCapture.captureEnvelope(sourceEventType), mapper);
    }

    private void publish(RuneLiteEventEnvelope envelope, Function<RuneLiteEventEnvelope, Optional<TelemetryEnvelope>> mapper)
    {
        mapper.apply(envelope).ifPresent(telemetryBus::accept);
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
