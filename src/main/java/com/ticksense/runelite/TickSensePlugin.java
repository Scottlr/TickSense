package com.ticksense.runelite;

import com.google.inject.Provides;
import com.ticksense.ui.TickSensePanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
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
