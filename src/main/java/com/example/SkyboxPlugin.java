/*
 * Copyright (c) 2019 logarrhytmic <https://github.com/logarrhythmic>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.example;

import com.google.inject.Inject;
import com.google.inject.Provides;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.Color;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.time.LocalDate;


@PluginDescriptor(
        name = "Night Mode",
        description = "Day/Night Cycle in OldSchool Runescape",
        enabledByDefault = false,
        tags = {"night"}
)
public class SkyboxPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private SkyboxPluginConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private SkyboxTimeOverlay timeOverlay;

    @Provides
    SkyboxPluginConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(SkyboxPluginConfig.class);
    }
    @Provides
    SkyboxTimeOverlay provideTimeOverlay() {
        return new SkyboxTimeOverlay(this);
    }

    @Override
    protected void startUp() {
        overlayManager.remove(timeOverlay);
        if (config.showOverlay()) {
            overlayManager.add(timeOverlay);
        }
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(timeOverlay);
        client.setSkyboxColor(0);
    }
    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
        {
            client.setSkyboxColor(0); // reseta o céu ao abrir a tela de login
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!event.getGroup().equals("skybox")) return;

        if (event.getKey().equals("showOverlay")) {
            overlayManager.remove(timeOverlay);
            if (config.showOverlay()) {
                overlayManager.add(timeOverlay);
            }
        }
    }


    // -----------------------
    // Color mapping
    // -----------------------
    // Classe interna para mapear cor + posição no ciclo
    private static class ColorPoint {
        float position; // 0..1
        Color color;
        ColorPoint(float position, Color color) { this.position = position; this.color = color; }
    }

    // -----------------------
    // Calculate normalized cycle (0..1)
    // -----------------------
    // Calcula tempo normalizado 0..1 baseado em ciclo rápido (segundos)
    private float getTimeCycle() {
        SkyboxPluginConfig.City selectedCity = config.getCity();
        ZoneId zone = ZoneId.of(cityTimeZones.get(selectedCity));

        if (config.useRealTimeCycle()) {
            ZonedDateTime now = ZonedDateTime.now(zone);
            return (now.getHour() + now.getMinute() / 60f) / 24f;
        } else {
            long millis = System.currentTimeMillis();
            float t = (millis % (config.cycleDuration() * 1000L)) / (float)(config.cycleDuration() * 1000L); // 0..1
            return t;
        }
    }


    // -----------------------
    // Render Skybox // antes de alterar a peste toda das funcao
    // -----------------------
    @Subscribe
    public void onBeforeRender(BeforeRender r) {
        if (client.getGameState() != GameState.LOGGED_IN)
            return;

        SkyboxPluginConfig.City selectedCity = config.getCity();
        double[] coords = cityCoords.get(selectedCity);
        double latitude = coords[0];
        double longitude = coords[1];
        ZoneId zone = ZoneId.of(cityTimeZones.get(selectedCity));
        LocalDate today = LocalDate.now(zone);

        // Sunrise/sunset reais
        ZonedDateTime sunriseTime = SunCalc.getSunrise(today, latitude, longitude, zone);
        ZonedDateTime sunsetTime  = SunCalc.getSunset(today, latitude, longitude, zone);

        float sunriseStart = (float)((sunriseTime.getHour() - 1 + sunriseTime.getMinute()/60.0) / 24.0);
        float sunriseEnd   = (float)((sunriseTime.getHour() + sunriseTime.getMinute()/60.0) / 24.0);
        float sunsetStart  = (float)((sunsetTime.getHour() + sunsetTime.getMinute()/60.0) / 24.0);
        float sunsetEnd    = (float)((sunsetTime.getHour() + 1 + sunsetTime.getMinute()/60.0) / 24.0);

        float t = getTimeCycle(); // 0..1

        Color skyColor;
        if (t < sunriseStart) {
            skyColor = config.getNightColor();
        } else if (t < sunriseEnd) {
            skyColor = interpolateColor(config.getNightColor(), config.getSunriseColor(),
                    (t - sunriseStart) / (sunriseEnd - sunriseStart));
        } else if (t < 0.5f) {
            skyColor = interpolateColor(config.getSunriseColor(), config.getDayColor(),
                    (t - sunriseEnd) / (0.5f - sunriseEnd));
        } else if (t < sunsetStart) {
            skyColor = config.getDayColor();
        } else if (t < sunsetEnd) {
            skyColor = interpolateColor(config.getDayColor(), config.getSunsetColor(),
                    (t - sunsetStart) / (sunsetEnd - sunsetStart));
        } else {
            skyColor = interpolateColor(config.getSunsetColor(), config.getNightColor(),
                    (t - sunsetEnd) / (1.0f - sunsetEnd));
        }

        client.setSkyboxColor(skyColor.getRGB());
    }



    // -----------------------
    // Função auxiliar para renderizacao de skybox
    // -----------------------
    private Color interpolateColor(Color start, Color end, float t) {
        int r = (int)(start.getRed()   * (1 - t) + end.getRed()   * t);
        int g = (int)(start.getGreen() * (1 - t) + end.getGreen() * t);
        int b = (int)(start.getBlue()  * (1 - t) + end.getBlue()  * t);
        return new Color(r, g, b);
    }
    private static final Map<SkyboxPluginConfig.City, double[]> cityCoords = Map.of(
            SkyboxPluginConfig.City.SAO_PAULO, new double[]{-23.5505, -46.6333},
            SkyboxPluginConfig.City.LONDON,    new double[]{51.5074, -0.1278},
            SkyboxPluginConfig.City.NEW_YORK,  new double[]{40.7128, -74.0060},
            SkyboxPluginConfig.City.TOKYO,     new double[]{35.6895, 139.6917},
            SkyboxPluginConfig.City.SYDNEY,    new double[]{-33.8688, 151.2093}
    );

    private static final Map<SkyboxPluginConfig.City, String> cityTimeZones = Map.of(
            SkyboxPluginConfig.City.SAO_PAULO, "America/Sao_Paulo",
            SkyboxPluginConfig.City.LONDON,    "Europe/London",
            SkyboxPluginConfig.City.NEW_YORK,  "America/New_York",
            SkyboxPluginConfig.City.TOKYO,     "Asia/Tokyo",
            SkyboxPluginConfig.City.SYDNEY,    "Australia/Sydney"
    );

    // -----------------------
    // Virtual time calculation
    // -----------------------
    public ZonedDateTime getVirtualTime()
    {
        SkyboxPluginConfig.City selectedCity = config.getCity();
        ZoneId zone = ZoneId.of(cityTimeZones.get(selectedCity));

        if (config.useRealTimeCycle())
        {
            return ZonedDateTime.now(zone);
        }
        else
        {
            long millis = System.currentTimeMillis();
            float t = (millis % (config.cycleDuration() * 1000L)) / (float)(config.cycleDuration() * 1000L); // 0..1
            int totalMinutes = (int)(t * 24 * 60);
            int hour = totalMinutes / 60;
            int minute = totalMinutes % 60;

            return ZonedDateTime.now(zone)
                    .withHour(hour)
                    .withMinute(minute)
                    .withSecond(0)
                    .withNano(0);
        }
    }

}




