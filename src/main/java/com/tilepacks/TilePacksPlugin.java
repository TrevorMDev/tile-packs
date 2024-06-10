/*
 * Copyright (c) 2018, TheLonelyDev <https://github.com/TheLonelyDev>
 * Copyright (c) 2021, Adam <Adam@sigterm.info>
 * Copyright (c) 2022, Trevor <https://github.com/TrevorMDev>
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
package com.tilepacks;

import com.google.gson.Gson;
import com.google.inject.Provides;
import com.tilepacks.ui.overlay.GroundMarkerMinimapOverlay;
import com.tilepacks.ui.overlay.GroundMarkerOverlay;
import com.tilepacks.ui.panel.TilePacksPanel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
        name = "Tile Packs"
)
public class TilePacksPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private TilePacksConfig config;
    @Inject
    private ConfigManager configManager;
    @Inject
    private Gson gson;
    @Inject
    private ClientToolbar clientToolbar;
    @Inject
    private OverlayManager overlayManager;

    private GroundMarkerMinimapOverlay minimapOverlay;
    private GroundMarkerOverlay overlay;
    private TilePackManager tilePackManager;
    private PointManager pointManager;
    private TilePacksPanel panel;
    private NavigationButton navButton;

    @Override
    protected void startUp() throws Exception {
        tilePackManager = new TilePackManager(gson, configManager);
        tilePackManager.loadPacks();
        pointManager = new PointManager(tilePackManager, gson, client);
        overlay = new GroundMarkerOverlay(pointManager, client, config);
        minimapOverlay = new GroundMarkerMinimapOverlay(pointManager, client, config);
        overlayManager.add(overlay);
        overlayManager.add(minimapOverlay);
        panel = new TilePacksPanel(tilePackManager, pointManager, gson);
        final BufferedImage icon = ImageUtil.loadImageResource(TilePacksPlugin.class, "tilepacks_icon.png");

        navButton = NavigationButton.builder()
                .tooltip("Tile Packs")
                .icon(icon)
                .priority(4)//how is this determined? Could it be a config?
                .panel(panel)
                .build();

        pointManager.loadPoints();

        log.debug("Tile Packs Plugin started");
        if (config.hidePlugin()) {
            return;
        }

        clientToolbar.addNavigation(navButton);

    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        overlayManager.remove(minimapOverlay);
        clientToolbar.removeNavigation(navButton);
        log.debug("Tile Packs Plugin stopped");
    }

    @Subscribe
    public void onConfigChanged(final ConfigChanged event) {
        if (event.getGroup().equals(TilePacksConfig.GROUP)) {
            switch (event.getKey()) {
                case "hidePlugin":
                    if (config.hidePlugin()) {
                        clientToolbar.removeNavigation(navButton);
                    } else {
                        clientToolbar.addNavigation(navButton);
                    }
                    break;
            }
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        // map region has just been updated
        pointManager.loadPoints();
    }

    @Provides
    TilePacksConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TilePacksConfig.class);
    }
}
