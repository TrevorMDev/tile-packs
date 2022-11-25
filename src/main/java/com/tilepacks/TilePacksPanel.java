/*
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class TilePacksPanel extends PluginPanel {

    private final TilePacksPlugin plugin;
    private final Gson gson;

    private static final ImmutableMap<String, String> MARKERS = new ImmutableMap.Builder<String, String>()
            .put("Test pack 1", "[{\"regionId\":9776,\"regionX\":10,\"regionY\":16,\"z\":0,\"color\":\"#FFF1FF00\"},{\"regionId\":9776,\"regionX\":8,\"regionY\":16,\"z\":0,\"color\":\"#FFF1FF00\"},{\"regionId\":9776,\"regionX\":9,\"regionY\":15,\"z\":0,\"color\":\"#FFF1FF00\"}]")
            .put("Test pack 2", "[{\"regionId\":9776,\"regionX\":10,\"regionY\":18,\"z\":0,\"color\":\"#FFF1FF00\"},{\"regionId\":9776,\"regionX\":8,\"regionY\":18,\"z\":0,\"color\":\"#FFF1FF00\"},{\"regionId\":9776,\"regionX\":9,\"regionY\":17,\"z\":0,\"color\":\"#FFF1FF00\"}]")
            .build();

    private final JPanel listContainer = new JPanel();

    TilePacksPanel(TilePacksPlugin plugin, Gson gson) {
        super();
        this.plugin = plugin;
        this.gson = gson;
        add(listContainer);
        listContainer.setLayout(new GridLayout(0, 1));
        listContainer.setBackground(Color.black);

        loadPacks();
    }

    private void loadPacks()
    {
        for (Map.Entry<String,String> entry : MARKERS.entrySet()) {
            log.info("key {} value {}", entry.getKey(), entry.getValue());
            List<GroundMarkerPoint> markers = gson.fromJson(
                    entry.getValue(),
                    new TypeToken<List<GroundMarkerPoint>>(){}.getType());
            log.info("markers {}", markers.toString());
            JPanel tile2 = new TilePack(plugin,  entry.getKey(), markers);
            listContainer.add(tile2);
        }
    }
}