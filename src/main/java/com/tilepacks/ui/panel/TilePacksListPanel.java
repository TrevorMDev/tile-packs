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
import com.tilepacks.TilePackConfigManager;
import com.tilepacks.PointManager;
import com.tilepacks.TilePackManager;
import com.tilepacks.data.TilePackConfig;
import com.tilepacks.data.TilePack;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;

/**
 * UI panel parent, contains all content for the panel.
 */
public class TilePacksListPanel extends PluginPanel {

    private final TilePackManager tilePackManager;
    private final PointManager pointManager;
    private final TilePackConfigManager tilePackConfigManager;
    private final Gson gson;

    private final IconTextField searchBar;
    private final JPanel listContainer = new JPanel();

    public TilePacksListPanel(TilePackManager tilePackManager, PointManager pointManager,
                              TilePackConfigManager tilePackConfigManager, Gson gson) {
        super();
        this.tilePackManager = tilePackManager;
        this.pointManager = pointManager;
        this.tilePackConfigManager = tilePackConfigManager;
        this.gson = gson;

        searchBar = new IconTextField();
        searchBar.setIcon(IconTextField.Icon.SEARCH);
        searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        searchBar.setMinimumSize(new Dimension(0, 30));
        searchBar.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                createTilePackPanels();
            }
        });
        searchBar.addClearListener(() -> createTilePackPanels());
        add(searchBar);

        add(listContainer);
        listContainer.setLayout(new GridLayout(0, 1, 0, 0));

        CustomPackManagerPanel customPackManager = new CustomPackManagerPanel(tilePackManager, gson, this);

        add(customPackManager);

        createTilePackPanels();
    }

    public void createTilePackPanels() {
        listContainer.removeAll();
        String search = searchBar.getText();
        for (Map.Entry<Integer, TilePack> pack : tilePackManager.getTilePacks().entrySet()) {
            TilePack tilePack = pack.getValue();
            TilePackConfig tilePackConfig = tilePackConfigManager.getTilePackConfig(tilePack.id);
            boolean matchesSearch = Strings.isNullOrEmpty(search) || tilePack.packName.toLowerCase().contains(search.toLowerCase());

            if (matchesSearch && tilePackConfig.visible) {
                JPanel tile = new TilePackPanel(tilePackManager, pointManager, tilePackConfigManager, gson,
                        this, tilePack, tilePackConfig);
                listContainer.add(tile);
            }
        }
        listContainer.revalidate();
        listContainer.repaint();
    }
}