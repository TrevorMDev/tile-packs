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
import com.tilepacks.data.CustomConfig;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for all functions that interface with or modify the custom configs of a TilePack.
 * A custom config is settings for a tile pack, not custom added packs.
 * Both built in and custom packs have a config.
 */
@Slf4j
@Value
public class CustomConfigManager {
    private static final String CONFIG_GROUP = "tilePacks";
    private static final String CUSTOM_CONFIG = "customConfigs";

    @NonFinal
    private Map<Integer, CustomConfig> customConfigs = new HashMap<Integer, CustomConfig>();

    private final Gson gson;
    private final ConfigManager configManager;

    @Inject
    CustomConfigManager(Gson gson, ConfigManager configManager) {
        this.gson = gson;
        this.configManager = configManager;
        this.loadCustomConfigs();
    }

    //loads the custom pack configs from the settings file
    private void loadCustomConfigs() {
        String json = configManager.getConfiguration(CONFIG_GROUP, CUSTOM_CONFIG);

        if (Strings.isNullOrEmpty(json)) {
            return;
        }
        customConfigs = gson.fromJson(json, new TypeToken<Map<Integer, CustomConfig>>() {
        }.getType());
    }

    //replaces a config with the latest version, then saves the custom config list.
    public void updateCustomConfig(CustomConfig customConfig) {
        customConfigs.put(customConfig.packId, customConfig);

        String json = gson.toJson(customConfigs);
        configManager.setConfiguration(CONFIG_GROUP, CUSTOM_CONFIG, json);
    }

    //returns the custom config of a pack, or a default config if it doesn't exist.
    public CustomConfig fetchCustomConfig(Integer packId) {
        CustomConfig customConfig = customConfigs.get(packId);
        if(customConfig != null) {
            return customConfig;
        }

        return new CustomConfig(packId, true);
    }
}
