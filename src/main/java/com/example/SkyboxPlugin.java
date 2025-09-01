package com.example;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.Color;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

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

    @Provides
    SkyboxPluginConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(SkyboxPluginConfig.class);
    }

    @Subscribe
    public void onBeforeRender(BeforeRender r)
    {
        if (client.getGameState() != GameState.LOGGED_IN)
            return;

        Color dayColor = config.getDayColor();
        Color nightColor = config.getNightColor();

        // Ciclo de 2 minutos (120000ms) — ajuste o valor para mudar a velocidade
        //float t = (float) ((System.currentTimeMillis() % 120000) / 120000.0);
        // Ciclo de 20 segundos
        float t = (float) ((System.currentTimeMillis() % 20000) / 20000.0);

        // Interpolação linear das cores
        int rCol = (int) (dayColor.getRed()   * t + nightColor.getRed()   * (1 - t));
        int gCol = (int) (dayColor.getGreen() * t + nightColor.getGreen() * (1 - t));
        int bCol = (int) (dayColor.getBlue()  * t + nightColor.getBlue()  * (1 - t));

        client.setSkyboxColor(new Color(rCol, gCol, bCol).getRGB());
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
        {
            client.setSkyboxColor(0); // reseta o céu ao abrir a tela de login
        }
    }

    @Override
    public void shutDown()
    {
        client.setSkyboxColor(0); // reseta o céu ao desligar o plugin
    }
}
