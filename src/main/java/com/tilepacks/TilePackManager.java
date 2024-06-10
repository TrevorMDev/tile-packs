/*
 * Copyright (c) 2024, Trevor <https://github.com/TrevorMDev>
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
import com.tilepacks.data.TilePack;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for all functions that interface with or modify tile packs
 */
@Slf4j
@Value
public class TilePackManager {
    private static final String CONFIG_GROUP = "tilePacks";
    private static final String CUSTOM_ID = "customId";
    private static final String CUSTOM_PACKS = "customPacks";
    private static final String PACKS_PREFIX = "packs";

    @NonFinal
    private Map<Integer, TilePack> packs = new HashMap<Integer, TilePack>();

    private final Gson gson;
    private final ConfigManager configManager;

    @Inject
    TilePackManager(Gson gson, ConfigManager configManager) {
        this.gson = gson;
        this.configManager = configManager;
    }

    //loads the packs from the json file
    public void loadPacks() {
        try (InputStream in = getClass().getResourceAsStream("tilePacks.jsonc"))
        {
            final InputStreamReader data = new InputStreamReader(in, StandardCharsets.UTF_8);
            final Type type = new TypeToken<Map<Integer, TilePack>>() {}.getType();
            Map<Integer, TilePack> parsed = gson.fromJson(data, type);
            //merge in any custom packs
            Map<Integer, TilePack> customPacks = loadCustomPacks();
            parsed.putAll(customPacks);

            packs = parsed;
        } catch(Exception e) {
            log.error("error loading packs from json, this is likely due to a bad json file.", e);
        }
    }

    //loads the custom packs from the config
    private Map<Integer, TilePack> loadCustomPacks() {
        String json = configManager.getConfiguration(CONFIG_GROUP, CUSTOM_PACKS);

        if (Strings.isNullOrEmpty(json)) {
            return new HashMap<Integer, TilePack>();
        }
        return gson.fromJson(json, new TypeToken<Map<Integer, TilePack>>() {
        }.getType());
    }

    //saves a pack id to the saved config of enabled packs
    public void addEnabledPack(Integer packId) {
        List<Integer> packs = loadEnabledPacks();
        packs.add(packId);

        String json = gson.toJson(packs);
        configManager.setConfiguration(CONFIG_GROUP, PACKS_PREFIX, json);
    }

    //removes a pack id from the saved config of enabled packs
    public void removeEnabledPack(Integer packId) {
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
    public List<Integer> loadEnabledPacks() {
        String json = configManager.getConfiguration(CONFIG_GROUP, PACKS_PREFIX);

        if (Strings.isNullOrEmpty(json)) {
            return new ArrayList<>();
        }
        return gson.fromJson(json, new TypeToken<List<Integer>>() {
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

    //saves a custom pack to the users config
    public void addCustomPack(String name, String tiles) {
        Integer customId = loadCustomId() + 1;
        TilePack pack = new TilePack(customId, name, tiles);
        Map<Integer, TilePack> customPacks = loadCustomPacks();
        customPacks.put(customId, pack);

        String json = gson.toJson(customPacks);
        configManager.setConfiguration(CONFIG_GROUP, CUSTOM_PACKS, json);
        configManager.setConfiguration(CONFIG_GROUP, CUSTOM_ID, customId);
    }

    //removes a custom pack from the users config
    public void removeCustomPack(Integer packId) {
        //unsubscribe the pack before removing it.
        removeEnabledPack(packId);
        Map<Integer, TilePack> customPacks = loadCustomPacks();
        customPacks.remove(packId);
        if (customPacks.isEmpty()) {
            configManager.unsetConfiguration(CONFIG_GROUP, CUSTOM_PACKS);
            return;
        }

        String json = gson.toJson(customPacks);
        configManager.setConfiguration(CONFIG_GROUP, CUSTOM_PACKS, json);
    }
}
