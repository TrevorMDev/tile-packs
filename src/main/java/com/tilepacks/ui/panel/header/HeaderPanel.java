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

package com.tilepacks.ui.panel.header;

import com.tilepacks.FilterManager;
import com.tilepacks.ui.panel.TilePacksListPanel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * The header that lives at the top of the panel above the list of tile packs
 * Handles the search bar and global filters
 */
public class HeaderPanel extends JPanel {
    private final FilterManager filterManager;
    private final TilePacksListPanel tilePacksList;

    private final IconTextField searchTextField;
    private final JPanel filterPanel = new JPanel();
    private final VisibleHeaderLabel visibleListLabel;
    private final InvisibleHeaderLabel invisibleListLabel;

    public HeaderPanel(FilterManager filterManager, TilePacksListPanel tilePacksList) {
        super();
        this.filterManager = filterManager;
        this.tilePacksList = tilePacksList;

        setLayout(new BorderLayout());

        searchTextField = new IconTextField();
        searchTextField.setIcon(IconTextField.Icon.SEARCH);
        searchTextField.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
        searchTextField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchTextField.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        searchTextField.setMinimumSize(new Dimension(0, 30));
        searchTextField.addKeyListener(new SearchTextFieldKeyListener());
        searchTextField.addClearListener(() -> tilePacksList.createTilePackPanels());
        add(searchTextField, BorderLayout.NORTH);


        filterPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 4, 6));
        filterPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        filterPanel.setBorder(new MatteBorder(1, 0, 0, 0, ColorScheme.DARK_GRAY_COLOR));
        add(filterPanel, BorderLayout.SOUTH);

        visibleListLabel = new VisibleHeaderLabel(filterManager, tilePacksList);
        filterPanel. add(visibleListLabel);
        invisibleListLabel = new InvisibleHeaderLabel(filterManager, tilePacksList);
        filterPanel. add(invisibleListLabel);
    }

    public String getSearchText() {
        return searchTextField.getText();
    }

    class SearchTextFieldKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
            tilePacksList.createTilePackPanels();
        }
    }
}

