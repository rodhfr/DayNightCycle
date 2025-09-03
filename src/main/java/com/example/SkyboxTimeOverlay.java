package com.example;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayLayer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.ZonedDateTime;

public class SkyboxTimeOverlay extends Overlay
{
    private final SkyboxPlugin plugin;

    public SkyboxTimeOverlay(SkyboxPlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        ZonedDateTime virtualTime = plugin.getVirtualTime();
        String time = String.format("%02d:%02d:%02d",
                virtualTime.getHour(),
                virtualTime.getMinute(),
                virtualTime.getSecond());

        graphics.drawString("Virtual Time: " + time, 10, 20);
        return null;
    }
}
