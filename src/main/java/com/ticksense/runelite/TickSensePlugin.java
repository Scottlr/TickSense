package com.ticksense.runelite;

import com.google.inject.Provides;
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

@Slf4j
@PluginDescriptor(name = "TickSense")
public class TickSensePlugin extends Plugin
{
    private static final int NAV_ICON_SIZE = 16;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private RuneLiteEventCapture eventCapture;

    private TickSensePanel panel;
    private NavigationButton navButton;

    @Override
    protected void startUp()
    {
        panel = new TickSensePanel();
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

    @Subscribe
    public void onGameTick(GameTick event)
    {
        eventCapture.onGameTick();
    }

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        eventCapture.onClientTick();
    }

    @Subscribe
    public void onPostClientTick(PostClientTick event)
    {
        eventCapture.capture("PostClientTick");
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        eventCapture.capture("GameStateChanged");
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        eventCapture.capture("MenuOptionClicked");
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        eventCapture.capture("MenuEntryAdded");
    }

    @Subscribe
    public void onMenuOpened(MenuOpened event)
    {
        eventCapture.capture("MenuOpened");
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        eventCapture.capture("NpcSpawned");
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event)
    {
        eventCapture.capture("NpcDespawned");
    }

    @Subscribe
    public void onNpcChanged(NpcChanged event)
    {
        eventCapture.capture("NpcChanged");
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event)
    {
        eventCapture.capture("InteractingChanged");
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event)
    {
        eventCapture.capture("AnimationChanged");
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event)
    {
        eventCapture.capture("HitsplatApplied");
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        eventCapture.capture("ItemContainerChanged");
    }

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        eventCapture.capture("StatChanged");
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        eventCapture.capture("WidgetLoaded");
    }

    @Subscribe
    public void onWidgetClosed(WidgetClosed event)
    {
        eventCapture.capture("WidgetClosed");
    }

    @Subscribe
    public void onWidgetDrag(WidgetDrag event)
    {
        eventCapture.capture("WidgetDrag");
    }

    @Subscribe
    public void onWorldViewLoaded(WorldViewLoaded event)
    {
        eventCapture.capture("WorldViewLoaded");
    }

    @Subscribe
    public void onWorldViewUnloaded(WorldViewUnloaded event)
    {
        eventCapture.capture("WorldViewUnloaded");
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event)
    {
        eventCapture.capture("ProjectileMoved");
    }

    @Subscribe
    public void onGraphicChanged(GraphicChanged event)
    {
        eventCapture.capture("GraphicChanged");
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
