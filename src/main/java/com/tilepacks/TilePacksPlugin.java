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

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.groundmarkers.GroundMarkerPlugin;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
        name = "Tile Packs"
)
@PluginDependency(GroundMarkerPlugin.class)
public class TilePacksPlugin extends Plugin {
    private static final String CONFIG_GROUP = "tilePacks";
    private static final String REGION_PREFIX = "region_";
    private static final String PACKS_PREFIX = "packs";

    public static Map<Integer, TilePack> packs = new HashMap<Integer, TilePack>();

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
    @Inject
    private GroundMarkerOverlay overlay;

    @Getter(AccessLevel.PACKAGE)
    private final List<ColorTileMarker> points = new ArrayList<>();
    private TilePacksPanel panel;
    private NavigationButton navButton;

    @Override
    protected void startUp() throws Exception {
        loadPacks();
        overlayManager.add(overlay);
        panel = new TilePacksPanel(this, gson);
        final BufferedImage icon = ImageUtil.loadImageResource(TilePacksPlugin.class, "tilepacks_icon.png");

        navButton = NavigationButton.builder()
                .tooltip("Tile Packs")
                .icon(icon)
                .priority(4)//how is this determined? Could it be a config?
                .panel(panel)
                .build();


        loadPoints();

        log.debug("Tile Packs Plugin started");
        if (config.hidePlugin()) {
            return;
        }

        clientToolbar.addNavigation(navButton);

    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
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

    //this function deletes legacy configs for a region. This plugin used to store all the tiles per region in the config, but now it just grabs them from the packs themselves.
    void deleteLegacyConfig(int regionId) {
        configManager.unsetConfiguration(CONFIG_GROUP, REGION_PREFIX + regionId);
    }

    //gets all the active points for all enabled packs.
    List<GroundMarkerPoint> getActivePoints() {
        List<GroundMarkerPoint> markers = new ArrayList<>();
        List<Integer> enabledPacks = loadEnabledPacks();
        for (Map.Entry<Integer, TilePack> pack : packs.entrySet()) {
            if (enabledPacks.contains(pack.getKey())) {
                markers.addAll(gson.fromJson(
                        pack.getValue().packTiles,
                        new TypeToken<List<GroundMarkerPoint>>() {
                        }.getType()));
            }
        }
        return markers;
    }

    //gets all the active points, filtered for a region
    List<GroundMarkerPoint> getActivePoints(int regionId) {
        deleteLegacyConfig(regionId);
        List<GroundMarkerPoint> activePoints = getActivePoints();
        Map<Integer, List<GroundMarkerPoint>> regionGroupedPoints = activePoints.stream()
                .collect(Collectors.groupingBy(GroundMarkerPoint::getRegionId));
        List<GroundMarkerPoint> regionPoints = regionGroupedPoints.get(regionId);
        if (regionPoints == null) {
            return Collections.emptyList();
        }
        return regionPoints;
    }

    //saves a pack id to the saved config of enabled packs
    void addEnabledPack(Integer packId) {
        List<Integer> packs = loadEnabledPacks();
        packs.add(packId);

        String json = gson.toJson(packs);
        configManager.setConfiguration(CONFIG_GROUP, PACKS_PREFIX, json);
    }

    //removes a pack id from the saved config of enabled packs
    void removeEnabledPack(Integer packId) {
        List<Integer> packs = loadEnabledPacks();
        packs.remove(packId);
        if (packs.isEmpty()) {
            configManager.unsetConfiguration(CONFIG_GROUP, PACKS_PREFIX);
            return;
        }

        String json = gson.toJson(packs);
        configManager.setConfiguration(CONFIG_GROUP, PACKS_PREFIX, json);
    }

    //gets a list of all enabled pack ids
    List<Integer> loadEnabledPacks() {
        String json = configManager.getConfiguration(CONFIG_GROUP, PACKS_PREFIX);

        if (Strings.isNullOrEmpty(json)) {
            return new ArrayList<>();
        }
        return gson.fromJson(json, new TypeToken<List<Integer>>() {
        }.getType());

    }

    //loads the points from the packs for the players active regions
    void loadPoints() {
        points.clear();

        int[] regions = client.getMapRegions();

        if (regions == null) {
            return;
        }

        for (int regionId : regions) {
            Collection<GroundMarkerPoint> regionPoints = getActivePoints(regionId);
            Collection<ColorTileMarker> colorTileMarkers = translateToColorTileMarker(regionPoints);
            points.addAll(colorTileMarkers);
        }
    }

    //loads the packs from the json file
    void loadPacks() {
        try (InputStream in = getClass().getResourceAsStream("tilePacks.jsonc"))
        {
            final InputStreamReader data = new InputStreamReader(in, StandardCharsets.UTF_8);
            final Type type = new TypeToken<Map<Integer, TilePack>>() {}.getType();
            Map<Integer, TilePack> parsed = gson.fromJson(data, type);
            packs = parsed;
        } catch(Exception e) {
            log.error("error loading packs from json, this is likely due to a bad json file.", e);
        }
    }

    private Collection<ColorTileMarker> translateToColorTileMarker(Collection<GroundMarkerPoint> points) {
        if (points.isEmpty()) {
            return Collections.emptyList();
        }

        return points.stream()
                .map(point -> new ColorTileMarker(
                        WorldPoint.fromRegion(point.getRegionId(), point.getRegionX(), point.getRegionY(), point.getZ()),
                        point.getColor(), point.getLabel()))
                .flatMap(colorTile ->
                {
                    final Collection<WorldPoint> localWorldPoints = WorldPoint.toLocalInstance(client, colorTile.getWorldPoint());
                    return localWorldPoints.stream().map(wp -> new ColorTileMarker(wp, colorTile.getColor(), colorTile.getLabel()));
                })
                .collect(Collectors.toList());
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        // map region has just been updated
        loadPoints();
    }

    @Provides
    TilePacksConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TilePacksConfig.class);
    }
}
