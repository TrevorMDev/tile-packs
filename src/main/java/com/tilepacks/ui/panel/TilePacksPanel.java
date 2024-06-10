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
import com.tilepacks.PointManager;
import com.tilepacks.data.TilePack;
import com.tilepacks.TilePackManager;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.Map;

@Slf4j
public class TilePacksPanel extends PluginPanel {

    private final TilePackManager tilePackManager;
    private final PointManager pointManager;
    private final Gson gson;

    private final IconTextField searchBar;
    private final JPanel listContainer = new JPanel();

    public TilePacksPanel(TilePackManager tilePackManager, PointManager pointManager, Gson gson) {
        super();
        this.tilePackManager = tilePackManager;
        this.pointManager = pointManager;
        this.gson = gson;

        this.searchBar = new IconTextField();
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
                loadPacks();
            }
        });
        searchBar.addClearListener(() -> loadPacks());
        add(searchBar);

        add(listContainer);
        listContainer.setLayout(new GridLayout(0, 1, 0, 0));

        CustomPackManager customPackManager = new CustomPackManager(tilePackManager, gson, this);

        add(customPackManager);

        loadPacks();
    }

    public void loadPacks() {
        listContainer.removeAll();
        String search = searchBar.getText();
        List<Integer> enabledPacks = tilePackManager.loadEnabledPacks();
        for (Map.Entry<Integer, TilePack> pack : tilePackManager.getPacks().entrySet()) {
            //TODO add search keys to the TilePack so you can search on more than the name.
            if (Strings.isNullOrEmpty(search) || pack.getValue().packName.toLowerCase().contains(search.toLowerCase())) {
                JPanel tile = new PackPanel(tilePackManager, pointManager, gson, this, pack.getValue(), enabledPacks.contains(pack.getKey()));
                listContainer.add(tile);
            }
        }
        listContainer.revalidate();
        listContainer.repaint();
    }
}