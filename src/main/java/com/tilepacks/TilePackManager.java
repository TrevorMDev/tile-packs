package com.tilepacks;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.tilepacks.data.GroundMarkerPoint;
import com.tilepacks.data.TilePack;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import java.awt.Color;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This class is responsible for all functions that interface with or modify tile packs
 */
@Slf4j
@Value
public class TilePackManager {
    private static final String CONFIG_GROUP = "tilePacks";
    private static final String CUSTOM_ID = "customId";
    private static final String PACK_PREFIX = "pack_";
    private static final String PACKS_PREFIX = "packs";
    private static final Type TILE_LIST_TYPE = new TypeToken<List<GroundMarkerPoint>>() {
    }.getType();

    @NonFinal
    private Map<Integer, TilePack> tilePacks = new HashMap<Integer, TilePack>();
    @NonFinal
    private List<Integer> enabledPacks = new ArrayList<Integer>();
    //These are the default packs that the user has edited, which makes a clone of the default.
    @NonFinal
    private Set<Integer> fullOverridePackIds = new HashSet<Integer>();

    private final Gson gson;
    private final ConfigManager configManager;

    @Inject
    TilePackManager(Gson gson, ConfigManager configManager) {
        this.gson = gson;
        this.configManager = configManager;

        this.loadEnabledPacks();
        this.loadPacks();
    }

    //loads the packs from the json file
    private void loadPacks() {
        try (InputStream in = getClass().getResourceAsStream("tilePacks.jsonc")) {
            final InputStreamReader data = new InputStreamReader(in, StandardCharsets.UTF_8);
            final Type type = new TypeToken<Map<Integer, TilePack>>() {
            }.getType();
            Map<Integer, TilePack> parsed = gson.fromJson(data, type);
            for (TilePack savedPack : loadSavedPacks().values()) {
                TilePack resourcePack = parsed.get(savedPack.id);
                if (isFullPackRecord(savedPack)) {
                    parsed.put(savedPack.id, savedPack);
                    fullOverridePackIds.add(savedPack.id);
                } else if (resourcePack != null) {
                    resourcePack.visible = savedPack.visible;
                }
            }

            for (TilePack pack : parsed.values()) {
                pack.parsedPackTiles = parseTiles(pack.packTiles);
            }

            tilePacks = parsed;
        } catch (Exception e) {
            log.error("error loading packs from json, this is likely due to a bad json file.", e);
        }
    }

    private Map<Integer, TilePack> loadSavedPacks() {
        Map<Integer, TilePack> savedPacks = new HashMap<Integer, TilePack>();
        String wholePrefix = ConfigManager.getWholeKey(CONFIG_GROUP, null, PACK_PREFIX);
        for (String key : configManager.getConfigurationKeys(wholePrefix)) {
            String packId = key.substring(wholePrefix.length());
            try {
                Integer id = Integer.valueOf(packId);
                String json = configManager.getConfiguration(CONFIG_GROUP, PACK_PREFIX + id);
                TilePack pack = gson.fromJson(json, TilePack.class);
                if (pack != null) {
                    savedPacks.put(id, pack);
                }
            } catch (RuntimeException e) {
                log.warn("Unable to load tile pack config {}", key, e);
            }
        }
        return savedPacks;
    }

    /**
     * Parses a pack's tile JSON. Null entries are dropped.
     * @return an unmodifiable list, or null when the JSON is missing or unreadable
     */
    public List<GroundMarkerPoint> parseTiles(String packTiles) {
        if (packTiles == null) {
            return null;
        }

        try {
            List<GroundMarkerPoint> points = gson.fromJson(packTiles, TILE_LIST_TYPE);
            if (points == null) {
                return null;
            }
            List<GroundMarkerPoint> parsed = new ArrayList<>(points);
            parsed.removeIf(Objects::isNull);
            return parsed;
        } catch (RuntimeException e) {
            log.warn("Unable to parse tile data", e);
            return null;
        }
    }

    private void savePack(TilePack pack) {
        configManager.setConfiguration(CONFIG_GROUP, PACK_PREFIX + pack.id, gson.toJson(pack));
    }

    //saves a pack id to the saved config of enabled packs
    public void addEnabledPack(Integer packId) {
        enabledPacks.add(packId);

        String json = gson.toJson(enabledPacks);
        configManager.setConfiguration(CONFIG_GROUP, PACKS_PREFIX, json);
    }

    //removes a pack id from the saved config of enabled packs
    public void removeEnabledPack(Integer packId) {
        enabledPacks.remove(packId);
        if (enabledPacks.isEmpty()) {
            configManager.unsetConfiguration(CONFIG_GROUP, PACKS_PREFIX);
            return;
        }

        String json = gson.toJson(enabledPacks);
        configManager.setConfiguration(CONFIG_GROUP, PACKS_PREFIX, json);
    }

    //gets a list of all enabled pack ids
    private void loadEnabledPacks() {
        String json = configManager.getConfiguration(CONFIG_GROUP, PACKS_PREFIX);

        log.debug("json {}", json);
        if (Strings.isNullOrEmpty(json)) {
            return;
        }
        enabledPacks = gson.fromJson(json, new TypeToken<List<Integer>>() {
        }.getType());
    }

    //gets the custom id the user is currently on
    //each pack the user adds needs a unique index, so we need to manage that
    public Integer loadCustomId() {
        String json = configManager.getConfiguration(CONFIG_GROUP, CUSTOM_ID);

        if (Strings.isNullOrEmpty(json)) {
            //default to 9999 because we add 1, and I want it to start at an even 10k.
            return 9999;
        }
        return gson.fromJson(json, new TypeToken<Integer>() {
        }.getType());
    }

    /**
     * saves a custom pack to the users config
     * @return the ID of the new pack
     */
    public Integer addCustomPack(String name, String tiles) {
        Integer customId = loadCustomId() + 1;
        TilePack pack = new TilePack(customId, name, tiles);
        pack.parsedPackTiles = parseTiles(tiles);
        savePack(pack);
        configManager.setConfiguration(CONFIG_GROUP, CUSTOM_ID, customId);

        addEnabledPack(pack.id);
        tilePacks.put(pack.id, pack);
        return pack.id;
    }

    //removes a custom pack from the users config
    public void removeCustomPack(Integer packId) {
        //unsubscribe the pack before removing it.
        removeEnabledPack(packId);
        configManager.unsetConfiguration(CONFIG_GROUP, PACK_PREFIX + packId);
        tilePacks.remove(packId);
    }

    public void updatePack(TilePack pack) {
        if (pack.id >= 10000 || fullOverridePackIds.contains(pack.id)) {
            savePack(pack);
        } else {
            savePack(new TilePack(pack.id, pack.visible));
        }
    }

    /**
     * Returns the manager-owned pack and creates a full saved override the first time a bundled
     * pack is opened for editing.
     */
    public TilePack preparePackForEditing(Integer packId) {
        TilePack pack = tilePacks.get(packId);
        if (pack == null || packId >= 10000 || fullOverridePackIds.contains(packId)) {
            return pack;
        }

        TilePack override = new TilePack();
        override.id = pack.id;
        override.packName = pack.packName;
        override.link = pack.link;
        override.packTiles = pack.packTiles;
        override.parsedPackTiles = pack.parsedPackTiles;
        override.defaultColor = pack.defaultColor;
        override.visible = pack.visible;

        savePack(override);
        tilePacks.put(packId, override);
        fullOverridePackIds.add(packId);
        return override;
    }

    /** Updates only the title of the manager-owned pack and saves its complete record. */
    public void updatePackTitle(Integer packId, String title) {
        TilePack pack = preparePackForEditing(packId);
        if (pack == null) {
            return;
        }

        pack.packName = title;
        savePack(pack);
        fullOverridePackIds.add(packId);
    }

    /** Saves the per-pack default color */
    public void updatePackDefaultColor(Integer packId, Color color) {
        TilePack pack = preparePackForEditing(packId);
        if (pack == null) {
            return;
        }
        pack.defaultColor = color;
        savePack(pack);
        fullOverridePackIds.add(packId);
    }

    /** Saves tile JSON */
    public void updatePackTiles(Integer packId, List<GroundMarkerPoint> points) {
        TilePack pack = preparePackForEditing(packId);
        if (pack == null) {
            return;
        }

        List<GroundMarkerPoint> snapshot = new ArrayList<>(points);
        pack.packTiles = gson.toJson(snapshot, TILE_LIST_TYPE);
        pack.parsedPackTiles = snapshot;
        savePack(pack);
        fullOverridePackIds.add(packId);
    }

    private boolean isFullPackRecord(TilePack pack) {
        return pack.packName != null || pack.link != null || pack.packTiles != null || pack.defaultColor != null;
    }

    public boolean isPackEnabled(Integer packId) {
        return enabledPacks.contains(packId);
    }
}