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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class TilePacksPanel extends PluginPanel {

    private final TilePacksPlugin plugin;
    private final Gson gson;

    private final JPanel listContainer = new JPanel();

    TilePacksPanel(TilePacksPlugin plugin, Gson gson) {
        super();
        this.plugin = plugin;
        this.gson = gson;
        listContainer.setLayout(new GridLayout(0, 1));
        listContainer.setBackground(Color.black);
//        JsonElement json = gson.fromJson("[{\"regionId\":6972,\"regionX\":48,\"regionY\":52,\"z\":0,\"color\":\"#FFF1FF00\"},{\"regionId\":6972,\"regionX\":33,\"regionY\":31,\"z\":0,\"color\":\"#FFF1FF00\"},{\"regionId\":6972,\"regionX\":33,\"regionY\":35,\"z\":0,\"color\":\"#FFF1FF00\"},{\"regionId\":6972,\"regionX\":33,\"regionY\":33,\"z\":0,\"color\":\"#FFF1FF00\"},{\"regionId\":6972,\"regionX\":48,\"regionY\":43,\"z\":0,\"color\":\"#FFF1FF00\"},{\"regionId\":6972,\"regionX\":48,\"regionY\":53,\"z\":0,\"color\":\"#FFF1FF00\"},{\"regionId\":6972,\"regionX\":48,\"regionY\":51,\"z\":0,\"color\":\"#FFF1FF00\"}]", JsonElement.class);
//        log.info("json", json.toString());
//        String jsonString = json.getAsString();
//        log.info("jsonString", jsonString);
//        List<GroundMarkerPoint> markers = gson.fromJson(jsonString, new TypeToken<List<GroundMarkerPoint>>() {
//        }.getType());
        List<GroundMarkerPoint> markers = new ArrayList<GroundMarkerPoint>();
        markers.add(new GroundMarkerPoint(9776, 10, 16, 0, Color.decode("#F1FF00"), null));
        markers.add(new GroundMarkerPoint(9776, 8, 16, 0, Color.decode("#F1FF00"), null));
        markers.add(new GroundMarkerPoint(9776, 9, 15, 0, Color.decode("#F1FF00"), null));//TODO how do make this easily expandable, and also support opacity from hex codes.


//                = Arrays.asList([{"regionId":9776,"regionX":10,"regionY":16,"z":0,"color":"#FFF1FF00"},{"regionId":9776,"regionX":8,"regionY":16,"z":0,"color":"#FFF1FF00"},{"regionId":9776,"regionX":9,"regionY":15,"z":0,"color":"#FFF1FF00"}])

        log.info("markers");
        log.info(markers.toString());
        JPanel tile = new TilePack(plugin, "Arceuus Runecrafting Shortcuts", markers);
        add(listContainer);
        listContainer.add(tile);
    }
}