/*
 * Copyright (c) 2021, Adam <Adam@sigterm.info>
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
import com.tilepacks.TilePackManager;
import com.tilepacks.TilePacksPlugin;
import com.tilepacks.data.TilePack;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

@Slf4j
/**
 * UI container for the TilePack and its controls
 * One exists for each TilePack
 */
public class TilePackPanel extends JPanel {
    private static final int ROW_WIDTH = PluginPanel.PANEL_WIDTH - 10;
    private static final int ROW_HEIGHT = 30;
    private static final int CONTROL_SIZE = 20;

    private final TilePackManager tilePackManager;
    private final PointManager pointManager;
    private final Gson gson;
    private final TilePacksListPanel tilePacksList;

    private final JPanel topRow = new JPanel();
    private final JPanel controlPanel = new JPanel();
    private final JLabel packName;
    // These placeholder labels give us a grid of 10 control icons
    // Without a fixed number of icons, the controls resize and move around.
    // Because built in and custom packs have different options
    private final JLabel placeholder1;
    private final JLabel placeholder2;
    private final JLabel placeholder3;
    private final JLabel placeholder4;
    private final JLabel placeholder5;
    private final JLabel placeholder6;
    private final JLabel placeholder7;
    private final JLabel deleteCustomPack;
    private final JLabel helpLink;
    private final JLabel togglePack;

    TilePackPanel(TilePackManager tilePackManager, PointManager pointManager, Gson gson, TilePacksListPanel tilePacksList, TilePack tilePack) {
        super();
        this.tilePackManager = tilePackManager;
        this.pointManager = pointManager;
        this.gson = gson;
        this.tilePacksList = tilePacksList;

        log.debug("Loading pack - {}", tilePack.packName);

        this.setLayout(new BorderLayout());
        this.setBackground(ColorScheme.BRAND_ORANGE);
        this.setBorder(new EmptyBorder(2, 2, 2, 2));

        topRow.setLayout(new BorderLayout());
        topRow.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
        topRow.setPreferredSize(new Dimension(ROW_WIDTH, ROW_HEIGHT));
        topRow.setBorder(new EmptyBorder(4, 4, 4, 4));
        add(topRow, BorderLayout.NORTH);

        packName = new JLabel(tilePack.packName);
        packName.setFont(FontManager.getRunescapeFont());
        topRow.add(packName, BorderLayout.WEST);

        controlPanel.setLayout(new GridLayout(1,10));
        controlPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        controlPanel.setPreferredSize(new Dimension(ROW_WIDTH, ROW_HEIGHT));
        controlPanel.setBorder(new EmptyBorder(2, 4, 2, 4));
        add(controlPanel, BorderLayout.SOUTH);

        placeholder1 = new JLabel();
        controlPanel.add(placeholder1);
        placeholder2 = new JLabel();
        controlPanel.add(placeholder2);
        placeholder3 = new JLabel();
        controlPanel.add(placeholder3);
        placeholder4 = new JLabel();
        controlPanel.add(placeholder4);
        placeholder5 = new JLabel();
        controlPanel.add(placeholder5);
        placeholder6 = new JLabel();
        controlPanel.add(placeholder6);
        placeholder7 = new JLabel();
        controlPanel.add(placeholder7);

        //anything over 10k is a custom pack
        if (tilePack.id >= 10000) {
            deleteCustomPack = new DeleteCustomPackLabel(tilePackManager, pointManager, tilePack, tilePacksList);
        } else {
            deleteCustomPack = new JLabel();
        }
        controlPanel.add(deleteCustomPack);

        if (!Strings.isNullOrEmpty(tilePack.link)) {
            helpLink = new HelpLinkLabel(tilePack);
        } else {
            helpLink = new JLabel();
        }
        controlPanel.add(helpLink);

        togglePack = new TogglePackLabel(tilePackManager, pointManager, tilePack);
        controlPanel.add(togglePack);
    }
}