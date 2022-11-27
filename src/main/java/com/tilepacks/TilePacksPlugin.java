/*
 * Copyright (c) 2018, TheLonelyDev <https://github.com/TheLonelyDev>
 * Copyright (c) 2021, Adam <Adam@sigterm.info>
 * Copyright (c) 2022, Trevor <https://github.com/TrevorMartz>
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
import com.google.common.collect.ImmutableMap;
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

    //Keeping these as a string mostly because it is a bit simpler. Not sure if it is actually the best idea.
    //Would be nice to add more search tags that don't need to go in the name and maybe categories for other filtering.
    public static final ImmutableMap<String, String> PACKS = new ImmutableMap.Builder<String, String>()
            .put("Abyssal Sire", "[{\"regionId\":11850,\"regionX\":27,\"regionY\":44,\"z\":0,\"color\":\"#FFFFFF00\"},{\"regionId\":11850,\"regionX\":25,\"regionY\":44,\"z\":0,\"color\":\"#FFFFFF00\"},{\"regionId\":11850,\"regionX\":27,\"regionY\":36,\"z\":0,\"color\":\"#FFFFFF00\"},{\"regionId\":11850,\"regionX\":25,\"regionY\":36,\"z\":0,\"color\":\"#FFFFFF00\"},{\"regionId\":11850,\"regionX\":27,\"regionY\":34,\"z\":0,\"color\":\"#FFFFFF00\"},{\"regionId\":11850,\"regionX\":25,\"regionY\":34,\"z\":0,\"color\":\"#FFFFFF00\"}]")
            .put("Akkha Butterfly", "[{\"regionId\":14676,\"regionX\":36,\"regionY\":32,\"z\":1,\"color\":\"#FFFFFFFF\"},{\"regionId\":14676,\"regionX\":36,\"regionY\":39,\"z\":1,\"color\":\"#FFFFFFFF\"},{\"regionId\":14676,\"regionX\":29,\"regionY\":39,\"z\":1,\"color\":\"#FFFFFFFF\"},{\"regionId\":14676,\"regionX\":29,\"regionY\":32,\"z\":1,\"color\":\"#FFFFFFFF\"},{\"regionId\":14676,\"regionX\":29,\"regionY\":31,\"z\":1,\"color\":\"#FFFFFFFF\"},{\"regionId\":14676,\"regionX\":29,\"regionY\":24,\"z\":1,\"color\":\"#FFFFFFFF\"},{\"regionId\":14676,\"regionX\":36,\"regionY\":31,\"z\":1,\"color\":\"#FFFFFFFF\"},{\"regionId\":14676,\"regionX\":36,\"regionY\":24,\"z\":1,\"color\":\"#FFFFFFFF\"},{\"regionId\":14676,\"regionX\":34,\"regionY\":22,\"z\":1,\"color\":\"#968D8D8D\"},{\"regionId\":14676,\"regionX\":31,\"regionY\":22,\"z\":1,\"color\":\"#968D8D8D\"},{\"regionId\":14676,\"regionX\":42,\"regionY\":30,\"z\":1,\"color\":\"#968D8D8D\"},{\"regionId\":14676,\"regionX\":42,\"regionY\":33,\"z\":1,\"color\":\"#968D8D8D\"},{\"regionId\":14676,\"regionX\":34,\"regionY\":41,\"z\":1,\"color\":\"#968D8D8D\"},{\"regionId\":14676,\"regionX\":31,\"regionY\":41,\"z\":1,\"color\":\"#968D8D8D\"},{\"regionId\":14676,\"regionX\":23,\"regionY\":33,\"z\":1,\"color\":\"#968D8D8D\"},{\"regionId\":14676,\"regionX\":23,\"regionY\":30,\"z\":1,\"color\":\"#968D8D8D\"},{\"regionId\":14676,\"regionX\":29,\"regionY\":28,\"z\":1,\"color\":\"#32FFFFFF\",\"label\":\"lol\"},{\"regionId\":14676,\"regionX\":36,\"regionY\":28,\"z\":1,\"color\":\"#32FFFFFF\",\"label\":\"lol\"},{\"regionId\":14676,\"regionX\":36,\"regionY\":35,\"z\":1,\"color\":\"#32FFFFFF\",\"label\":\"lol\"},{\"regionId\":14676,\"regionX\":29,\"regionY\":35,\"z\":1,\"color\":\"#32FFFFFF\",\"label\":\"lol\"},{\"regionId\":14676,\"regionX\":39,\"regionY\":27,\"z\":1,\"color\":\"#ABFFFFFF\"},{\"regionId\":14676,\"regionX\":39,\"regionY\":28,\"z\":1,\"color\":\"#ABFFFFFF\"},{\"regionId\":14676,\"regionX\":33,\"regionY\":28,\"z\":1,\"color\":\"#ABFFFFFF\"},{\"regionId\":14676,\"regionX\":33,\"regionY\":27,\"z\":1,\"color\":\"#ABFFFFFF\"},{\"regionId\":14676,\"regionX\":32,\"regionY\":28,\"z\":1,\"color\":\"#ABFFFFFF\"},{\"regionId\":14676,\"regionX\":32,\"regionY\":27,\"z\":1,\"color\":\"#ABFFFFFF\"},{\"regionId\":14676,\"regionX\":33,\"regionY\":35,\"z\":1,\"color\":\"#ABFFFFFF\"},{\"regionId\":14676,\"regionX\":33,\"regionY\":36,\"z\":1,\"color\":\"#ABFFFFFF\"},{\"regionId\":14676,\"regionX\":32,\"regionY\":36,\"z\":1,\"color\":\"#ABFFFFFF\"},{\"regionId\":14676,\"regionX\":32,\"regionY\":35,\"z\":1,\"color\":\"#ABFFFFFF\"},{\"regionId\":14676,\"regionX\":39,\"regionY\":36,\"z\":1,\"color\":\"#ABFFFFFF\"},{\"regionId\":14676,\"regionX\":39,\"regionY\":35,\"z\":1,\"color\":\"#ABFFFFFF\"},{\"regionId\":14676,\"regionX\":26,\"regionY\":36,\"z\":1,\"color\":\"#ABFFFFFF\"},{\"regionId\":14676,\"regionX\":26,\"regionY\":35,\"z\":1,\"color\":\"#ABFFFFFF\"},{\"regionId\":14676,\"regionX\":26,\"regionY\":28,\"z\":1,\"color\":\"#ABFFFFFF\"},{\"regionId\":14676,\"regionX\":26,\"regionY\":27,\"z\":1,\"color\":\"#ABFFFFFF\"}]")
            .put("Alchemical Hydra", "[{\"regionId\":5536.0,\"regionX\":33.0,\"regionY\":23.0,\"z\":0.0,\"color\":\"#FF24FA3D\"},{\"regionId\":5536.0,\"regionX\":33.0,\"regionY\":27.0,\"z\":0.0,\"color\":\"#FF24FA3D\"},{\"regionId\":5536.0,\"regionX\":30.0,\"regionY\":30.0,\"z\":0.0,\"color\":\"#FF24FA3D\"},{\"regionId\":5536.0,\"regionX\":30.0,\"regionY\":35.0,\"z\":0.0,\"color\":\"#FF24FA3D\"},{\"regionId\":5536.0,\"regionX\":28.0,\"regionY\":37.0,\"z\":0.0,\"color\":\"#FF24FA3D\"},{\"regionId\":5536.0,\"regionX\":27.0,\"regionY\":38.0,\"z\":0.0,\"color\":\"#FF3941FF\"},{\"regionId\":5536.0,\"regionX\":31.0,\"regionY\":38.0,\"z\":0.0,\"color\":\"#FF3941FF\"},{\"regionId\":5536.0,\"regionX\":14.0,\"regionY\":38.0,\"z\":0.0,\"color\":\"#FF3941FF\"},{\"regionId\":5536.0,\"regionX\":14.0,\"regionY\":33.0,\"z\":0.0,\"color\":\"#FF3941FF\"},{\"regionId\":5536.0,\"regionX\":13.0,\"regionY\":32.0,\"z\":0.0,\"color\":\"#FFFF2121\"},{\"regionId\":5536.0,\"regionX\":16.0,\"regionY\":34.0,\"z\":0.0,\"color\":\"#FFFF2121\"},{\"regionId\":5536.0,\"regionX\":19.0,\"regionY\":32.0,\"z\":0.0,\"color\":\"#FFFF2121\"},{\"regionId\":5536.0,\"regionX\":20.0,\"regionY\":31.0,\"z\":0.0,\"color\":\"#FFFF2121\"},{\"regionId\":5536.0,\"regionX\":19.0,\"regionY\":30.0,\"z\":0.0,\"color\":\"#FFFF2121\"},{\"regionId\":5536.0,\"regionX\":17.0,\"regionY\":30.0,\"z\":0.0,\"color\":\"#FFF1FF00\"}]")
            .put("Calisto lure", "[{\"regionId\":13371,\"regionX\":0,\"regionY\":61,\"z\":0,\"color\":\"#FFFAFF00\",\"label\":\"1\"},{\"regionId\":13371,\"regionX\":5,\"regionY\":61,\"z\":0,\"color\":\"#FFFAFF00\",\"label\":\"2\"},{\"regionId\":13371,\"regionX\":9,\"regionY\":33,\"z\":0,\"color\":\"#FFFAFF00\",\"label\":\"4\"},{\"regionId\":13371,\"regionX\":6,\"regionY\":32,\"z\":0,\"color\":\"#FFFAFF00\",\"label\":\"3\"},{\"regionId\":13371,\"regionX\":8,\"regionY\":41,\"z\":0,\"color\":\"#FFFAFF00\",\"label\":\"5\"}]")
            .put("Chambers of Xeric crabs", "[{\"regionId\":13395,\"regionX\":7,\"regionY\":46,\"z\":2,\"color\":\"#FF000000\",\"label\":\"Anchor\"},{\"regionId\":13395,\"regionX\":8,\"regionY\":48,\"z\":2,\"color\":\"#FF000000\",\"label\":\"Anchor\"},{\"regionId\":13395,\"regionX\":14,\"regionY\":47,\"z\":2,\"color\":\"#FFFFFFFF\"},{\"regionId\":13395,\"regionX\":11,\"regionY\":56,\"z\":2,\"color\":\"#FFFF0000\"},{\"regionId\":13395,\"regionX\":11,\"regionY\":46,\"z\":2,\"color\":\"#FF1FA347\"},{\"regionId\":13395,\"regionX\":10,\"regionY\":46,\"z\":2,\"color\":\"#FFFF0000\"},{\"regionId\":13395,\"regionX\":10,\"regionY\":47,\"z\":2,\"color\":\"#FF001DFF\"},{\"regionId\":13139,\"regionX\":14,\"regionY\":40,\"z\":2,\"color\":\"#FF001DFF\"},{\"regionId\":13139,\"regionX\":17,\"regionY\":40,\"z\":2,\"color\":\"#FF1FA347\"},{\"regionId\":13139,\"regionX\":18,\"regionY\":40,\"z\":2,\"color\":\"#FFFFFFFF\"},{\"regionId\":13139,\"regionX\":22,\"regionY\":40,\"z\":2,\"color\":\"#FFFF0000\"},{\"regionId\":13139,\"regionX\":25,\"regionY\":41,\"z\":2,\"color\":\"#FF000000\",\"label\":\"Anchor\"},{\"regionId\":13139,\"regionX\":24,\"regionY\":39,\"z\":2,\"color\":\"#FF000000\",\"label\":\"Anchor\"}]")
            .put("Chambers of Xeric Olm", "[{\"regionId\":12889,\"regionX\":37,\"regionY\":43,\"z\":0,\"color\":{\"value\":-1,\"falpha\":0}},{\"regionId\":12889,\"regionX\":37,\"regionY\":45,\"z\":0,\"color\":{\"value\":-1,\"falpha\":0}},{\"regionId\":12889,\"regionX\":28,\"regionY\":45,\"z\":0,\"color\":{\"value\":-1,\"falpha\":0}},{\"regionId\":12889,\"regionX\":28,\"regionY\":43,\"z\":0,\"color\":{\"value\":-1,\"falpha\":0}},{\"regionId\":12889,\"regionX\":37,\"regionY\":38,\"z\":0,\"color\":{\"value\":-1,\"falpha\":0}},{\"regionId\":12889,\"regionX\":28,\"regionY\":38,\"z\":0,\"color\":{\"value\":-1,\"falpha\":0}},{\"regionId\":12889,\"regionX\":37,\"regionY\":41,\"z\":0,\"color\":{\"value\":-16777216,\"falpha\":0}},{\"regionId\":12889,\"regionX\":37,\"regionY\":50,\"z\":0,\"color\":{\"value\":-1,\"falpha\":0}},{\"regionId\":12889,\"regionX\":28,\"regionY\":47,\"z\":0,\"color\":{\"value\":-16777216,\"falpha\":0}},{\"regionId\":12889,\"regionX\":28,\"regionY\":50,\"z\":0,\"color\":{\"value\":-1,\"falpha\":0}}]")
            .put("Gauntlet Hunllef(both)", "[{\"regionId\":7512,\"regionX\":56,\"regionY\":59,\"z\":1,\"color\":\"#FFF1FF00\"},{\"regionId\":7512,\"regionX\":55,\"regionY\":59,\"z\":1,\"color\":\"#FFF1FF00\"},{\"regionId\":7512,\"regionX\":59,\"regionY\":56,\"z\":1,\"color\":\"#FFF1FF00\"},{\"regionId\":7512,\"regionX\":59,\"regionY\":55,\"z\":1,\"color\":\"#FFF1FF00\"},{\"regionId\":7512,\"regionX\":52,\"regionY\":56,\"z\":1,\"color\":\"#FFF1FF00\"},{\"regionId\":7512,\"regionX\":52,\"regionY\":55,\"z\":1,\"color\":\"#FFF1FF00\"},{\"regionId\":7512,\"regionX\":56,\"regionY\":52,\"z\":1,\"color\":\"#FFF1FF00\"},{\"regionId\":7512,\"regionX\":55,\"regionY\":52,\"z\":1,\"color\":\"#FFF1FF00\"},{\"regionId\":7768,\"regionX\":56,\"regionY\":59,\"z\":1,\"color\":\"#FFF1FF00\"},{\"regionId\":7768,\"regionX\":55,\"regionY\":59,\"z\":1,\"color\":\"#FFF1FF00\"},{\"regionId\":7768,\"regionX\":59,\"regionY\":55,\"z\":1,\"color\":\"#FFF1FF00\"},{\"regionId\":7768,\"regionX\":59,\"regionY\":56,\"z\":1,\"color\":\"#FFF1FF00\"},{\"regionId\":7768,\"regionX\":56,\"regionY\":52,\"z\":1,\"color\":\"#FFF1FF00\"},{\"regionId\":7768,\"regionX\":55,\"regionY\":52,\"z\":1,\"color\":\"#FFF1FF00\"},{\"regionId\":7768,\"regionX\":52,\"regionY\":55,\"z\":1,\"color\":\"#FFF1FF00\"},{\"regionId\":7768,\"regionX\":52,\"regionY\":56,\"z\":1,\"color\":\"#FFF1FF00\"}]")
            .put("General Graador 5:0", "[{\"regionId\":11347,\"regionX\":52,\"regionY\":39,\"z\":2,\"color\":\"#FF00FF00\"},{\"regionId\":11347,\"regionX\":49,\"regionY\":51,\"z\":2,\"color\":\"#FFFF0000\"},{\"regionId\":11347,\"regionX\":56,\"regionY\":54,\"z\":2,\"color\":\"#FFFF0000\"},{\"regionId\":11347,\"regionX\":59,\"regionY\":48,\"z\":2,\"color\":\"#FFFF0000\"},{\"regionId\":11347,\"regionX\":54,\"regionY\":44,\"z\":2,\"color\":\"#FFFF0000\"},{\"regionId\":11347,\"regionX\":51,\"regionY\":40,\"z\":2,\"color\":\"#FFFF0000\"},{\"regionId\":11347,\"regionX\":57,\"regionY\":53,\"z\":2,\"color\":\"#FFFF7F00\"},{\"regionId\":11347,\"regionX\":55,\"regionY\":43,\"z\":2,\"color\":\"#FFFF7F00\"},{\"regionId\":11347,\"regionX\":55,\"regionY\":42,\"z\":2,\"color\":\"#FFFF7F00\"}]")
            .put("General Graador 6:0", "[{\"regionId\":11347,\"regionX\":60,\"regionY\":39,\"z\":2,\"color\":\"#FFF8EA09\"},{\"regionId\":11347,\"regionX\":48,\"regionY\":39,\"z\":2,\"color\":\"#FFF8EA09\"},{\"regionId\":11347,\"regionX\":48,\"regionY\":57,\"z\":2,\"color\":\"#FFF8EA09\"},{\"regionId\":11347,\"regionX\":60,\"regionY\":54,\"z\":2,\"color\":\"#FFF8EA09\"},{\"regionId\":11347,\"regionX\":48,\"regionY\":49,\"z\":2,\"color\":\"#FFF8EA09\"},{\"regionId\":11347,\"regionX\":60,\"regionY\":46,\"z\":2,\"color\":\"#FFF8EA09\"},{\"regionId\":11347,\"regionX\":57,\"regionY\":40,\"z\":2,\"color\":\"#FFF8EA09\",\"label\":\"start\"}]")
            .put("General Graador 9:0", "[{\"regionId\":11347,\"regionX\":57,\"regionY\":55,\"z\":2,\"color\":\"#00FF0000\"},{\"regionId\":11347,\"regionX\":56,\"regionY\":45,\"z\":2,\"color\":\"#00FF0000\"},{\"regionId\":11347,\"regionX\":59,\"regionY\":50,\"z\":2,\"color\":\"#FFFF9000\"},{\"regionId\":11347,\"regionX\":57,\"regionY\":57,\"z\":2,\"color\":\"#FF19A800\",\"label\":\"Start\"},{\"regionId\":11347,\"regionX\":51,\"regionY\":54,\"z\":2,\"color\":\"#FFFF0000\",\"label\":\"1\"},{\"regionId\":11347,\"regionX\":55,\"regionY\":48,\"z\":2,\"color\":\"#FFFF0000\",\"label\":\"2\"},{\"regionId\":11347,\"regionX\":57,\"regionY\":44,\"z\":2,\"color\":\"#FFFF0000\",\"label\":\"3\"},{\"regionId\":11347,\"regionX\":51,\"regionY\":48,\"z\":2,\"color\":\"#FFFF0000\",\"label\":\"4\"},{\"regionId\":11347,\"regionX\":54,\"regionY\":42,\"z\":2,\"color\":\"#FFFF0000\",\"label\":\"5\"},{\"regionId\":11347,\"regionX\":49,\"regionY\":39,\"z\":2,\"color\":\"#FFFF0000\",\"label\":\"6\"},{\"regionId\":11347,\"regionX\":57,\"regionY\":47,\"z\":2,\"color\":\"#FFFF0000\",\"label\":\"7\"},{\"regionId\":11347,\"regionX\":57,\"regionY\":52,\"z\":2,\"color\":\"#FFFF0000\",\"label\":\"8\"},{\"regionId\":11347,\"regionX\":57,\"regionY\":56,\"z\":2,\"color\":\"#FFFF0000\",\"label\":\"9\"}]")
            .put("Giants Foundry", "[{\"regionId\":13491,\"regionX\":36,\"regionY\":36,\"z\":0,\"color\":\"#FFFFC300\",\"label\":\"Heats up\"},{\"regionId\":13491,\"regionX\":39,\"regionY\":41,\"z\":0,\"color\":\"#FFFF0000\",\"label\":\"Cools down\"},{\"regionId\":13491,\"regionX\":37,\"regionY\":29,\"z\":0,\"color\":\"#FF00A13E\",\"label\":\"Cools down\"}]")
            .put("Inferno", "[{\"regionId\":9043,\"regionX\":36,\"regionY\":46,\"z\":0,\"color\":\"#FF93928E\"},{\"regionId\":9043,\"regionX\":42,\"regionY\":46,\"z\":0,\"color\":\"#FF93928E\"},{\"regionId\":9043,\"regionX\":26,\"regionY\":46,\"z\":0,\"color\":\"#FF93928E\"},{\"regionId\":9043,\"regionX\":20,\"regionY\":46,\"z\":0,\"color\":\"#FF93928E\"},{\"regionId\":9043,\"regionX\":40,\"regionY\":46,\"z\":0,\"color\":\"#FFDF3C3C\"},{\"regionId\":9043,\"regionX\":39,\"regionY\":46,\"z\":0,\"color\":\"#FFDF3C3C\"},{\"regionId\":9043,\"regionX\":38,\"regionY\":46,\"z\":0,\"color\":\"#FFDF3C3C\"},{\"regionId\":9043,\"regionX\":31,\"regionY\":46,\"z\":0,\"color\":\"#FFDF3C3C\"},{\"regionId\":9043,\"regionX\":23,\"regionY\":46,\"z\":0,\"color\":\"#FFDF3C3C\"},{\"regionId\":9043,\"regionX\":22,\"regionY\":46,\"z\":0,\"color\":\"#FFDF3C3C\"},{\"regionId\":9043,\"regionX\":24,\"regionY\":46,\"z\":0,\"color\":\"#FFDF3C3C\"},{\"regionId\":9043,\"regionX\":33,\"regionY\":43,\"z\":0,\"color\":\"#FF93928E\"},{\"regionId\":9043,\"regionX\":32,\"regionY\":29,\"z\":0,\"color\":\"#FF93928E\"},{\"regionId\":9043,\"regionX\":26,\"regionY\":34,\"z\":0,\"color\":\"#FF93928E\"}]")
            .put("K'ril Tsutsaroth 6:0", "[{\"regionId\":11603,\"regionX\":39,\"regionY\":15,\"z\":2,\"color\":\"#FF00FF00\"},{\"regionId\":11603,\"regionX\":40,\"regionY\":7,\"z\":2,\"color\":\"#FF00FF00\"},{\"regionId\":11603,\"regionX\":56,\"regionY\":17,\"z\":2,\"color\":\"#FFFF0000\"},{\"regionId\":11603,\"regionX\":40,\"regionY\":17,\"z\":2,\"color\":\"#FFFF0000\"},{\"regionId\":11603,\"regionX\":46,\"regionY\":9,\"z\":2,\"color\":\"#FF00FF00\"},{\"regionId\":11603,\"regionX\":53,\"regionY\":14,\"z\":2,\"color\":\"#FF00FF00\"}]")
            .put("Theatre of Blood Maiden", "[{\"regionId\":12613,\"regionX\":34,\"regionY\":29,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":35,\"regionY\":29,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":34,\"regionY\":28,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":35,\"regionY\":28,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":38,\"regionY\":20,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":42,\"regionY\":20,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":46,\"regionY\":20,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":42,\"regionY\":41,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":38,\"regionY\":41,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":46,\"regionY\":41,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":50,\"regionY\":20,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":50,\"regionY\":41,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":50,\"regionY\":22,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":50,\"regionY\":39,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":41,\"regionY\":29,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":41,\"regionY\":30,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":41,\"regionY\":31,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":41,\"regionY\":32,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":50,\"regionY\":16,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":50,\"regionY\":15,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12613,\"regionX\":50,\"regionY\":14,\"z\":0,\"color\":{\"value\":-2090309528}}]")
            .put("Theatre of Blood Soteseg", "[{\"regionId\":13123,\"regionX\":17,\"regionY\":37,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":13123,\"regionX\":18,\"regionY\":42,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":13123,\"regionX\":13,\"regionY\":43,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":13123,\"regionX\":12,\"regionY\":38,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":13123,\"regionX\":15,\"regionY\":37,\"z\":0,\"color\":{\"value\":-65536}}]")
            .put("Theatre of Blood Verzik", "[{\"regionId\":12611,\"regionX\":26,\"regionY\":30,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":26,\"regionY\":29,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":38,\"regionY\":30,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":38,\"regionY\":29,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":40,\"regionY\":25,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":24,\"regionY\":25,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":40,\"regionY\":22,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":24,\"regionY\":22,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":25,\"regionY\":28,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":39,\"regionY\":28,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":29,\"regionY\":26,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":29,\"regionY\":27,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":29,\"regionY\":25,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":29,\"regionY\":24,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":30,\"regionY\":23,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":31,\"regionY\":23,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":32,\"regionY\":23,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":33,\"regionY\":23,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":34,\"regionY\":23,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":35,\"regionY\":24,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":35,\"regionY\":25,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":35,\"regionY\":26,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":35,\"regionY\":27,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":35,\"regionY\":28,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":34,\"regionY\":29,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":33,\"regionY\":29,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":32,\"regionY\":29,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":31,\"regionY\":29,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":30,\"regionY\":29,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":29,\"regionY\":28,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":40,\"regionY\":18,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":38,\"regionY\":17,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":26,\"regionY\":17,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":24,\"regionY\":18,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":32,\"regionY\":20,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":36,\"regionY\":24,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":28,\"regionY\":24,\"z\":0,\"color\":{\"value\":-2090309528}},{\"regionId\":12611,\"regionX\":32,\"regionY\":28,\"z\":0,\"color\":{\"value\":-2090309528}}]")
            .put("Theatre of Blood Xarpus", "[{\"regionId\":12612,\"regionX\":38,\"regionY\":30,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":39,\"regionY\":31,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":30,\"regionY\":30,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":29,\"regionY\":31,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":30,\"regionY\":40,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":38,\"regionY\":40,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":39,\"regionY\":39,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":39,\"regionY\":38,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":39,\"regionY\":37,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":39,\"regionY\":32,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":39,\"regionY\":33,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":37,\"regionY\":30,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":36,\"regionY\":30,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":31,\"regionY\":30,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":32,\"regionY\":30,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":29,\"regionY\":32,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":29,\"regionY\":33,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":30,\"regionY\":31,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":38,\"regionY\":31,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":38,\"regionY\":39,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":29,\"regionY\":38,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":29,\"regionY\":37,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":30,\"regionY\":39,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":31,\"regionY\":40,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":32,\"regionY\":40,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":37,\"regionY\":40,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":36,\"regionY\":40,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":35,\"regionY\":40,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":34,\"regionY\":40,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":33,\"regionY\":40,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":29,\"regionY\":36,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":29,\"regionY\":35,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":29,\"regionY\":34,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":33,\"regionY\":30,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":34,\"regionY\":30,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":35,\"regionY\":30,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":39,\"regionY\":35,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":39,\"regionY\":36,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":39,\"regionY\":34,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":31,\"regionY\":36,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":31,\"regionY\":34,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":33,\"regionY\":32,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":35,\"regionY\":32,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":37,\"regionY\":34,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":37,\"regionY\":36,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":35,\"regionY\":38,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":33,\"regionY\":38,\"z\":1,\"color\":{\"value\":-2090309528}},{\"regionId\":12612,\"regionX\":29,\"regionY\":39,\"z\":1,\"color\":{\"value\":-2090309528}}]")
            .put("Venenatis","[{\"regionId\":13114,\"regionX\":60,\"regionY\":22,\"z\":0,\"color\":\"#FFFAFF00\"},{\"regionId\":13114,\"regionX\":63,\"regionY\":19,\"z\":0,\"color\":\"#FFFFFF00\"},{\"regionId\":13114,\"regionX\":55,\"regionY\":22,\"z\":0,\"color\":\"#FFFFFF00\"},{\"regionId\":13370,\"regionX\":4,\"regionY\":25,\"z\":0,\"color\":\"#FFFAFF00\"},{\"regionId\":13370,\"regionX\":4,\"regionY\":22,\"z\":0,\"color\":\"#FFFAFF00\"},{\"regionId\":13370,\"regionX\":1,\"regionY\":19,\"z\":0,\"color\":\"#FFFFFF00\"}]")
            .put("Vet'ion", "[{\"regionId\":12602,\"regionX\":61,\"regionY\":53,\"z\":0,\"color\":\"#FFFFF100\",\"label\":\"Lure2\"},{\"regionId\":12858,\"regionX\":3,\"regionY\":52,\"z\":0,\"color\":\"#FFFFFF00\"},{\"regionId\":12858,\"regionX\":5,\"regionY\":52,\"z\":0,\"color\":\"#FFFFFF00\"},{\"regionId\":12858,\"regionX\":16,\"regionY\":57,\"z\":0,\"color\":\"#FFFFFF00\",\"label\":\"Lure\"},{\"regionId\":12858,\"regionX\":3,\"regionY\":47,\"z\":0,\"color\":\"#FFFF0000\"},{\"regionId\":12858,\"regionX\":5,\"regionY\":47,\"z\":0,\"color\":\"#FFFF0000\"},{\"regionId\":12858,\"regionX\":6,\"regionY\":47,\"z\":0,\"color\":\"#FFFF0000\"},{\"regionId\":12858,\"regionX\":7,\"regionY\":47,\"z\":0,\"color\":\"#FFFF0000\"},{\"regionId\":12858,\"regionX\":8,\"regionY\":47,\"z\":0,\"color\":\"#FFFF0000\"},{\"regionId\":12858,\"regionX\":2,\"regionY\":47,\"z\":0,\"color\":\"#FFFF0000\"},{\"regionId\":12858,\"regionX\":1,\"regionY\":47,\"z\":0,\"color\":\"#FFFF0000\"},{\"regionId\":12858,\"regionX\":4,\"regionY\":47,\"z\":0,\"color\":\"#FFFF0000\",\"label\":\"30 LINE\"},{\"regionId\":12858,\"regionX\":0,\"regionY\":47,\"z\":0,\"color\":\"#FFFF0000\"}]")
            .put("Barbarian Assault Defender", "[{\"regionId\":7508.0,\"regionX\":44.0,\"regionY\":34.0,\"z\":0.0,\"color\":{\"value\":-4129024.0,\"falpha\":0.0}},{\"regionId\":7508.0,\"regionX\":39.0,\"regionY\":39.0,\"z\":0.0,\"color\":{\"value\":-4129024.0,\"falpha\":0.0}},{\"regionId\":7508.0,\"regionX\":35.0,\"regionY\":42.0,\"z\":0.0,\"color\":{\"value\":-65526.0,\"falpha\":0.0}},{\"regionId\":7508.0,\"regionX\":32.0,\"regionY\":42.0,\"z\":0.0,\"color\":{\"value\":-1.6121601E7,\"falpha\":0.0}},{\"regionId\":7508.0,\"regionX\":30.0,\"regionY\":46.0,\"z\":0.0,\"color\":{\"value\":-1.6121601E7,\"falpha\":0.0}},{\"regionId\":7509.0,\"regionX\":45.0,\"regionY\":35.0,\"z\":0.0,\"color\":{\"value\":-7864576.0,\"falpha\":0.0}},{\"regionId\":7509.0,\"regionX\":35.0,\"regionY\":43.0,\"z\":0.0,\"color\":{\"value\":-7864576.0,\"falpha\":0.0}},{\"regionId\":7509.0,\"regionX\":38.0,\"regionY\":40.0,\"z\":0.0,\"color\":{\"value\":-65517.0,\"falpha\":0.0}},{\"regionId\":7509.0,\"regionX\":29.0,\"regionY\":46.0,\"z\":0.0,\"color\":{\"value\":-65517.0,\"falpha\":0.0}},{\"regionId\":7509.0,\"regionX\":45.0,\"regionY\":29.0,\"z\":0.0,\"color\":{\"value\":-65517.0,\"falpha\":0.0}},{\"regionId\":7509.0,\"regionX\":32.0,\"regionY\":42.0,\"z\":0.0,\"color\":{\"value\":-1.6121601E7,\"falpha\":0.0}}]")
            .put("Hallowed Sepulchre", "[{\"regionId\":9052,\"regionX\":32,\"regionY\":63,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":40,\"regionY\":3,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":42,\"regionY\":42,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":0,\"regionY\":45,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":6,\"regionY\":52,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":4,\"regionY\":25,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":31,\"regionY\":10,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":31,\"regionY\":18,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":55,\"regionY\":18,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":42,\"regionY\":51,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":42,\"regionY\":60,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":63,\"regionY\":34,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":28,\"regionY\":63,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":59,\"regionY\":45,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":51,\"regionY\":18,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":31,\"regionY\":14,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9053,\"regionX\":4,\"regionY\":36,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9821,\"regionX\":57,\"regionY\":36,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9821,\"regionX\":61,\"regionY\":49,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9821,\"regionX\":61,\"regionY\":53,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9821,\"regionX\":61,\"regionY\":57,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":32,\"regionY\":49,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":59,\"regionY\":18,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":59,\"regionY\":25,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":50,\"regionY\":32,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":40,\"regionY\":63,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":17,\"regionY\":35,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":23,\"regionY\":59,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":1,\"regionY\":30,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":40,\"regionY\":32,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":6,\"regionY\":15,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":12,\"regionY\":43,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":32,\"regionY\":58,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":50,\"regionY\":32,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":32,\"regionY\":53,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":57,\"regionY\":23,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":39,\"regionY\":61,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":44,\"regionY\":61,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":49,\"regionY\":61,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":57,\"regionY\":4,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10077,\"regionX\":44,\"regionY\":32,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":4,\"regionY\":36,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":48,\"regionY\":54,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":53,\"regionY\":16,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":59,\"regionY\":43,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":21,\"regionY\":34,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":4,\"regionY\":58,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":6,\"regionY\":15,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":47,\"regionY\":36,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":56,\"regionY\":54,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":48,\"regionY\":45,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":42,\"regionY\":17,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":32,\"regionY\":17,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":32,\"regionY\":7,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":11,\"regionY\":47,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":11,\"regionY\":48,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":57,\"regionY\":54,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9563,\"regionX\":42,\"regionY\":21,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10074,\"regionX\":24,\"regionY\":63,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":12,\"regionY\":60,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":49,\"regionY\":7,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":45,\"regionY\":7,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":42,\"regionY\":54,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":10,\"regionY\":10,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":15,\"regionY\":6,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":19,\"regionY\":6,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":19,\"regionY\":7,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":15,\"regionY\":7,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":19,\"regionY\":5,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":15,\"regionY\":5,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":45,\"regionY\":6,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":49,\"regionY\":6,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":45,\"regionY\":5,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":49,\"regionY\":5,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":55,\"regionY\":37,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":20,\"regionY\":19,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":7,\"regionY\":32,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":10,\"regionY\":42,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":16,\"regionY\":48,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":19,\"regionY\":19,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":26,\"regionY\":19,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":23,\"regionY\":41,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":18,\"regionY\":27,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":13,\"regionY\":4,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":13,\"regionY\":5,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":32,\"regionY\":54,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":23,\"regionY\":54,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":10075,\"regionX\":6,\"regionY\":15,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":23,\"regionY\":57,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":58,\"regionY\":11,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":58,\"regionY\":17,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":57,\"regionY\":12,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":57,\"regionY\":16,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":56,\"regionY\":17,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":56,\"regionY\":11,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":57,\"regionY\":35,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":56,\"regionY\":34,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":58,\"regionY\":34,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":56,\"regionY\":40,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":57,\"regionY\":39,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":58,\"regionY\":40,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":54,\"regionY\":52,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":55,\"regionY\":52,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":44,\"regionY\":53,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":43,\"regionY\":52,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":44,\"regionY\":51,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":38,\"regionY\":51,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":39,\"regionY\":52,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":38,\"regionY\":53,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":25,\"regionY\":52,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":26,\"regionY\":51,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":26,\"regionY\":53,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":21,\"regionY\":52,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":20,\"regionY\":51,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":20,\"regionY\":53,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":27,\"regionY\":57,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":58,\"regionY\":53,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":6,\"regionY\":53,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":6,\"regionY\":7,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":59,\"regionY\":4,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":59,\"regionY\":50,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":12,\"regionY\":56,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":15,\"regionY\":56,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":23,\"regionY\":56,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":59,\"regionY\":41,\"z\":0,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":19,\"regionY\":57,\"z\":2,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":21,\"regionY\":57,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":40,\"regionY\":57,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":48,\"regionY\":57,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":9051,\"regionX\":43,\"regionY\":56,\"z\":1,\"color\":{\"value\":-256}},{\"regionId\":9052,\"regionX\":32,\"regionY\":63,\"z\":2,\"color\":{\"value\":-256}}]")
            .put("Werewolf Skullball", "[{\"regionId\":14234,\"regionX\":35,\"regionY\":6,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"1. Shoot North\"},{\"regionId\":14234,\"regionX\":36,\"regionY\":15,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"3. Kick North\"},{\"regionId\":14234,\"regionX\":34,\"regionY\":16,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"2. Tap East\"},{\"regionId\":14234,\"regionX\":36,\"regionY\":19,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"4. Shoot North\"},{\"regionId\":14234,\"regionX\":35,\"regionY\":29,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"5. Tap East 2x\"},{\"regionId\":14234,\"regionX\":38,\"regionY\":28,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"6. Shoot North\"},{\"regionId\":14234,\"regionX\":39,\"regionY\":38,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"7. Tap West\"},{\"regionId\":14234,\"regionX\":37,\"regionY\":37,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"8. Shoot North\"},{\"regionId\":14234,\"regionX\":38,\"regionY\":46,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"10. Shoot North\"},{\"regionId\":14234,\"regionX\":36,\"regionY\":47,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"9. Tap East\"},{\"regionId\":14234,\"regionX\":37,\"regionY\":53,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"11. Kick East\"},{\"regionId\":14234,\"regionX\":42,\"regionY\":52,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"12. Tap North 2x\"},{\"regionId\":14234,\"regionX\":41,\"regionY\":55,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"13. Shoot East\"},{\"regionId\":14234,\"regionX\":50,\"regionY\":55,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"14. Kick East\"},{\"regionId\":14234,\"regionX\":55,\"regionY\":56,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"15. Shoot South\"},{\"regionId\":14234,\"regionX\":56,\"regionY\":46,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"16. Tap West\"},{\"regionId\":14234,\"regionX\":54,\"regionY\":47,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"17. Shoot South\"},{\"regionId\":14234,\"regionX\":54,\"regionY\":38,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"18. Shoot South\"},{\"regionId\":14234,\"regionX\":53,\"regionY\":28,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"19. Tap East\"},{\"regionId\":14234,\"regionX\":55,\"regionY\":29,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"20. Shoot South\"},{\"regionId\":14234,\"regionX\":55,\"regionY\":20,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"21. Shoot South\"},{\"regionId\":14234,\"regionX\":56,\"regionY\":10,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"22. Kick West\"},{\"regionId\":14234,\"regionX\":51,\"regionY\":9,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"23. Kick North\"},{\"regionId\":14234,\"regionX\":50,\"regionY\":8,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"24. Tap East\"},{\"regionId\":14234,\"regionX\":53,\"regionY\":8,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"25. Shoot West\"},{\"regionId\":14234,\"regionX\":43,\"regionY\":7,\"z\":0,\"color\":\"#FFFF001D\",\"label\":\"26. Shoot/Kick North\"}]")
            .put("Tarn's Lair Clue Step", "[{\"regionId\":12615.0,\"regionX\":32.0,\"regionY\":42.0,\"z\":0.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":30.0,\"regionY\":33.0,\"z\":0.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":32.0,\"regionY\":53.0,\"z\":0.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":15.0,\"regionY\":39.0,\"z\":0.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":8.0,\"regionY\":38.0,\"z\":1.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":5.0,\"regionY\":21.0,\"z\":1.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":53.0,\"regionY\":54.0,\"z\":0.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":52.0,\"regionY\":52.0,\"z\":1.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":50.0,\"regionY\":52.0,\"z\":1.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":50.0,\"regionY\":54.0,\"z\":1.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":48.0,\"regionY\":54.0,\"z\":1.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":48.0,\"regionY\":56.0,\"z\":1.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":43.0,\"regionY\":54.0,\"z\":1.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":46.0,\"regionY\":56.0,\"z\":1.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":44.0,\"regionY\":56.0,\"z\":1.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":37.0,\"regionY\":56.0,\"z\":1.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":8.0,\"regionY\":27.0,\"z\":2.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":8.0,\"regionY\":30.0,\"z\":2.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":8.0,\"regionY\":32.0,\"z\":2.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":8.0,\"regionY\":27.0,\"z\":1.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":5.0,\"regionY\":12.0,\"z\":2.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}},{\"regionId\":12615.0,\"regionX\":3.0,\"regionY\":10.0,\"z\":0.0,\"color\":{\"value\":-917760.0,\"falpha\":0.0}}]")
            .build();

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

        if (config.hidePlugin()) {
            return;
        }

        clientToolbar.addNavigation(navButton);

        log.debug("Tile Packs Plugin started");
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

    Collection<GroundMarkerPoint> getSavedPoints(int regionId) {
        String json = configManager.getConfiguration(CONFIG_GROUP, REGION_PREFIX + regionId);

        if (Strings.isNullOrEmpty(json)) {
            return Collections.emptyList();
        }

        return gson.fromJson(json, new TypeToken<List<GroundMarkerPoint>>() {
        }.getType());
    }

    List<GroundMarkerPoint> getActivePoints() {
        List<GroundMarkerPoint> markers = new ArrayList<>();
        List<String> enabledPacks = loadEnabledPacks();
        for (Map.Entry<String, String> pack : PACKS.entrySet()) {
            if (enabledPacks.contains(pack.getKey())) {
                markers.addAll(gson.fromJson(
                        pack.getValue(),
                        new TypeToken<List<GroundMarkerPoint>>() {
                        }.getType()));
            }
        }
        return markers;
    }

    List<GroundMarkerPoint> getActivePoints(int regionId) {
        List<GroundMarkerPoint> activePoints = getActivePoints();
        Map<Integer, List<GroundMarkerPoint>> regionGroupedPoints = activePoints.stream()
                .collect(Collectors.groupingBy(GroundMarkerPoint::getRegionId));
        List<GroundMarkerPoint> regionPoints = regionGroupedPoints.get(regionId);
        if (regionPoints == null) {
            return Collections.emptyList();
        }
        return regionPoints;
    }

    void addEnabledPack(String packName) {
        List<String> packs = loadEnabledPacks();
        packs.add(packName);

        String json = gson.toJson(packs);
        configManager.setConfiguration(CONFIG_GROUP, PACKS_PREFIX, json);
    }

    void removeEnabledPack(String packName) {
        List<String> packs = loadEnabledPacks();
        packs.remove(packName);
        if (packs.isEmpty()) {
            configManager.unsetConfiguration(CONFIG_GROUP, PACKS_PREFIX);
            return;
        }

        String json = gson.toJson(packs);
        configManager.setConfiguration(CONFIG_GROUP, PACKS_PREFIX, json);
    }

    List<String> loadEnabledPacks() {
        String json = configManager.getConfiguration(CONFIG_GROUP, PACKS_PREFIX);

        if (Strings.isNullOrEmpty(json)) {
            return new ArrayList<String>();
        }
        return gson.fromJson(json, new TypeToken<List<String>>() {
        }.getType());

    }

    void savePoints(int regionId, Collection<GroundMarkerPoint> points) {
        if (points == null || points.isEmpty()) {
            configManager.unsetConfiguration(CONFIG_GROUP, REGION_PREFIX + regionId);
            return;
        }

        String json = gson.toJson(points);
        configManager.setConfiguration(CONFIG_GROUP, REGION_PREFIX + regionId, json);
    }

    void loadPoints() {
        points.clear();

        int[] regions = client.getMapRegions();

        if (regions == null) {
            return;
        }

        for (int regionId : regions) {
            Collection<GroundMarkerPoint> regionPoints = getSavedPoints(regionId);
            Collection<ColorTileMarker> colorTileMarkers = translateToColorTileMarker(regionPoints);
            points.addAll(colorTileMarkers);
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

    void importGroundMarkers(List<GroundMarkerPoint> importPoints) {
        Map<Integer, List<GroundMarkerPoint>> regionGroupedPoints = importPoints.stream()
                .collect(Collectors.groupingBy(GroundMarkerPoint::getRegionId));

        regionGroupedPoints.forEach((regionId, groupedPoints) ->
        {
            Collection<GroundMarkerPoint> regionPoints = getSavedPoints(regionId);

            List<GroundMarkerPoint> mergedList = new ArrayList<>(regionPoints.size() + groupedPoints.size());
            mergedList.addAll(regionPoints);

            for (GroundMarkerPoint point : groupedPoints) {
                if (!mergedList.contains(point)) {
                    mergedList.add(point);
                }
            }

            savePoints(regionId, mergedList);
        });

        loadPoints();
    }

    void removeGroundMarkers(List<GroundMarkerPoint> removePoints) {
        Map<Integer, List<GroundMarkerPoint>> regionGroupedPoints = removePoints.stream()
                .collect(Collectors.groupingBy(GroundMarkerPoint::getRegionId));


        regionGroupedPoints.forEach((regionId, groupedPoints) ->
        {
            //there is no need to filter out the individual points, we can just re-filter the points it should have and overwrite the save.
            Collection<GroundMarkerPoint> regionPoints = getActivePoints(regionId);
            savePoints(regionId, regionPoints);
        });

        loadPoints();
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
