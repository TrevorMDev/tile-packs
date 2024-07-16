/*
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

package com.tilepacks.ui.panel;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.tilepacks.FilterManager;
import com.tilepacks.TilePackConfigManager;
import com.tilepacks.PointManager;
import com.tilepacks.TilePackManager;
import com.tilepacks.data.TilePackConfig;
import com.tilepacks.data.TilePack;
import com.tilepacks.ui.panel.header.HeaderPanel;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * UI panel parent, contains all content for the panel.
 */
public class TilePacksListPanel extends PluginPanel {

    private final TilePackManager tilePackManager;
    private final PointManager pointManager;
    private final TilePackConfigManager tilePackConfigManager;
    private final FilterManager filterManager;
    private final Gson gson;

    private final HeaderPanel headerPanel;
    private final JPanel listContainer = new JPanel();

    public TilePacksListPanel(TilePackManager tilePackManager, PointManager pointManager,
                              TilePackConfigManager tilePackConfigManager, FilterManager filterManager,
                              Gson gson) {
        super();
        this.tilePackManager = tilePackManager;
        this.pointManager = pointManager;
        this.tilePackConfigManager = tilePackConfigManager;
        this.filterManager = filterManager;
        this.gson = gson;

        headerPanel = new HeaderPanel(filterManager, this);
        add(headerPanel);

        add(listContainer);
        listContainer.setLayout(new GridLayout(0, 1, 0, 0));

        CustomPackManagerPanel customPackManager = new CustomPackManagerPanel(tilePackManager, pointManager, gson,this);

        add(customPackManager);

        createTilePackPanels();
    }

    public void createTilePackPanels() {
        listContainer.removeAll();
        String search = headerPanel.getSearchText();
        for (Map.Entry<Integer, TilePack> pack : tilePackManager.getTilePacks().entrySet()) {
            TilePack tilePack = pack.getValue();
            TilePackConfig tilePackConfig = tilePackConfigManager.getTilePackConfig(tilePack.id);
            boolean matchesSearch = Strings.isNullOrEmpty(search) || tilePack.packName.toLowerCase().contains(search.toLowerCase());
            boolean matchesFilters = (filterManager.getShowVisible() && tilePackConfig.visible) || (filterManager.getShowInvisible() && !tilePackConfig.visible);

            if (matchesSearch && matchesFilters) {
                JPanel tile = new TilePackPanel(tilePackManager, pointManager, tilePackConfigManager, gson,
                        this, tilePack, tilePackConfig);
                listContainer.add(tile);
            }
        }
        listContainer.revalidate();
        listContainer.repaint();
    }
}