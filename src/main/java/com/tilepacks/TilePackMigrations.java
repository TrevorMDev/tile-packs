package com.tilepacks;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.tilepacks.data.TilePack;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class TilePackMigrations {
    private static final String CONFIG_GROUP = "tilePacks";
    private static final String PACK_PREFIX = "pack_";
    private static final String CUSTOM_PACKS = "customPacks";
    private static final String TILE_PACK_CONFIGS = "tilePackConfigs";
    private static final String TILE_PACKS_VERSION = "tilePacksVersion";
    // More for reference than actual use.
    private static final int CURRENT_VERSION = 2;

    private final Gson gson;
    private final ConfigManager configManager;

    @Inject
    TilePackMigrations(Gson gson, ConfigManager configManager) {
        this.gson = gson;
        this.configManager = configManager;
    }

    void migrateToLatest() {
        int version = loadVersion();

        if (version < 2) {
            migrateV1ToV2();
            saveVersion(2);
        }
    }

    private int loadVersion() {
        String version = configManager.getConfiguration(CONFIG_GROUP, TILE_PACKS_VERSION);
        return Strings.isNullOrEmpty(version) ? 1 : Integer.parseInt(version);
    }

    private void saveVersion(int version) {
        configManager.setConfiguration(CONFIG_GROUP, TILE_PACKS_VERSION, version);
    }

    private void migrateV1ToV2() {
        String customPacksJson = configManager.getConfiguration(CONFIG_GROUP, CUSTOM_PACKS);
        String tilePackConfigsJson = configManager.getConfiguration(CONFIG_GROUP, TILE_PACK_CONFIGS);

        if (Strings.isNullOrEmpty(customPacksJson) && Strings.isNullOrEmpty(tilePackConfigsJson)) {
            return;
        }

        Map<Integer, LegacyTilePack> customPacks = parseMap(customPacksJson,
                new TypeToken<Map<Integer, LegacyTilePack>>() { }.getType());
        Map<Integer, LegacyTilePackConfig> tilePackConfigs = parseMap(tilePackConfigsJson,
                new TypeToken<Map<Integer, LegacyTilePackConfig>>() { }.getType());

        Set<Integer> ids = new HashSet<>();
        ids.addAll(customPacks.keySet());
        ids.addAll(tilePackConfigs.keySet());

        for (Integer id : ids) {
            LegacyTilePack customPack = customPacks.get(id);
            LegacyTilePackConfig config = tilePackConfigs.get(id);
            boolean visible = config == null || config.visible;

            TilePack pack;
            if (customPack == null) {
                pack = new TilePack(id, visible);
            } else {
                pack = new TilePack(id, customPack.packName, customPack.link, customPack.packTiles);
                pack.visible = visible;
            }

            configManager.setConfiguration(CONFIG_GROUP, PACK_PREFIX + id, gson.toJson(pack));
        }

        verifySavedPacks(ids);

        configManager.unsetConfiguration(CONFIG_GROUP, CUSTOM_PACKS);
        configManager.unsetConfiguration(CONFIG_GROUP, TILE_PACK_CONFIGS);
    }

    private void verifySavedPacks(Set<Integer> ids) {
        for (Integer id : ids) {
            String json = configManager.getConfiguration(CONFIG_GROUP, PACK_PREFIX + id);
            if (Strings.isNullOrEmpty(json)) {
                throw new IllegalStateException("Saved tile pack config is missing for id " + id);
            }

            TilePack pack = gson.fromJson(json, TilePack.class);
            if (pack == null || !id.equals(pack.id)) {
                throw new IllegalStateException("Saved tile pack config is invalid for id " + id);
            }
        }
    }

    private <T> Map<Integer, T> parseMap(String json, Type type) {
        if (Strings.isNullOrEmpty(json)) {
            return new HashMap<>();
        }
        Map<Integer, T> parsed = gson.fromJson(json, type);
        return parsed == null ? new HashMap<>() : parsed;
    }

    private static class LegacyTilePack {
        private Integer id;
        private String packName;
        private String link;
        private String packTiles;
    }

    private static class LegacyTilePackConfig {
        private Integer packId;
        private boolean visible;
    }
}