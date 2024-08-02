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
import com.tilepacks.TilePackConfigManager;
import com.tilepacks.TilePackManager;
import com.tilepacks.data.TilePackConfig;
import com.tilepacks.data.TilePack;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

@Slf4j
/**
 * UI container for the TilePack and its controls
 * One exists for each TilePack
 *
 * Icons are from https://www.flaticon.com/
 * All icons are 20x20
 * Grey icons are #858585
 * Green icons are #4CAF50
 * Red icons are #E31C1C
 */
public class TilePackPanel extends JPanel {
    private static final int CONTROL_SIZE = 20;

    private final TilePackManager tilePackManager;
    private final PointManager pointManager;
    private final TilePackConfigManager tilePackConfigManager;
    private final ChatMessageManager chatMessageManager;
    private final Gson gson;
    private final TilePacksListPanel tilePacksList;

    private final TilePack tilePack;
    private final TilePackConfig tilePackConfig;

    private final JPanel topRow = new JPanel();
    private final JPanel controlPanel = new JPanel();
    private final JLabel packName;
    private final JLabel deleteCustomPack;
    private final JLabel toggleVisible;
    private final JLabel copyToClipboard;
    private final JLabel helpLink;
    private final JLabel togglePack;

    TilePackPanel(TilePackManager tilePackManager,
                  PointManager pointManager,
                  TilePackConfigManager tilePackConfigManager,
                  ChatMessageManager chatMessageManager,
                  Gson gson,
                  TilePacksListPanel tilePacksList,
                  TilePack tilePack,
                  TilePackConfig tilePackConfig
    ) {
        super();
        this.tilePackManager = tilePackManager;
        this.pointManager = pointManager;
        this.tilePackConfigManager = tilePackConfigManager;
        this.chatMessageManager = chatMessageManager;
        this.gson = gson;
        this.tilePacksList = tilePacksList;
        this.tilePack = tilePack;
        this.tilePackConfig = tilePackConfig;

        log.debug("Loading pack - {}", tilePack.packName);

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(3, 2, 3, 2));

        topRow.setLayout(new BorderLayout());
        topRow.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        topRow.setBorder(new EmptyBorder(2, 4, 1, 4));
        add(topRow, BorderLayout.NORTH);

        packName = new JLabel();
        // Disabling html prevents accidental leaking of IP via a shared tile pack
        packName.putClientProperty("html.disable", Boolean.TRUE);
        packName.setText(tilePack.packName);
        packName.setFont(FontManager.getRunescapeFont());
        topRow.add(packName, BorderLayout.WEST);

        controlPanel.setLayout(new FlowLayout(FlowLayout.TRAILING, 4, 6));
        controlPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        controlPanel.setBorder(new MatteBorder(1, 0, 0, 0, ColorScheme.DARK_GRAY_COLOR));
        add(controlPanel, BorderLayout.SOUTH);

        //anything over 10k is a custom pack
        if (tilePack.id >= 10000) {
            deleteCustomPack = new DeleteCustomPackLabel(tilePackManager, pointManager, tilePack, tilePacksList);
        } else {
            deleteCustomPack = new JLabel();
        }
        deleteCustomPack.setPreferredSize(new Dimension(CONTROL_SIZE, CONTROL_SIZE));
        controlPanel.add(deleteCustomPack);

        toggleVisible = new ToggleVisibleLabel(tilePackConfigManager, tilePack, tilePackConfig, tilePacksList);
        toggleVisible.setPreferredSize(new Dimension(CONTROL_SIZE, CONTROL_SIZE));
        controlPanel.add(toggleVisible);

        copyToClipboard = new CopyToClipboardLabel(chatMessageManager, gson, tilePack);
        copyToClipboard.setPreferredSize(new Dimension(CONTROL_SIZE, CONTROL_SIZE));
        controlPanel.add(copyToClipboard);

        if (!Strings.isNullOrEmpty(tilePack.link)) {
            helpLink = new HelpLinkLabel(tilePack);
        } else {
            helpLink = new JLabel();
        }
        helpLink.setPreferredSize(new Dimension(CONTROL_SIZE, CONTROL_SIZE));
        controlPanel.add(helpLink);

        togglePack = new TogglePackLabel(tilePackManager, pointManager, tilePack);
        togglePack.setPreferredSize(new Dimension(CONTROL_SIZE, CONTROL_SIZE));
        controlPanel.add(togglePack);
    }
}
