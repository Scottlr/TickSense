package com.ticksense.runelite;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(name = "TickSense")
public class TickSensePlugin extends Plugin
{
    @Override
    protected void startUp()
    {
        log.debug("TickSense started");
    }

    @Override
    protected void shutDown()
    {
        log.debug("TickSense stopped");
    }
}
