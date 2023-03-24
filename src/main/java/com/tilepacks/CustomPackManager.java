/*
 * Copyright (c) 2023, Trevor <https://github.com/TrevorMDev>
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
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

@Slf4j
public class CustomPackManager extends PluginPanel {

    private final TilePacksPlugin plugin;
    private final Gson gson;
    private final TilePacksPanel panel;

    private final JLabel inputLabel;
    private final FlatTextField nameInput;
    private final JLabel tilesLabel;
    private final FlatTextField tilesInput;
    private final JButton addPackButton;

    CustomPackManager(TilePacksPlugin plugin, Gson gson, TilePacksPanel panel) {
        super();
        this.plugin = plugin;
        this.gson = gson;
        this.panel = panel;

        this.inputLabel = new JLabel("Custom Pack Name");
        add(inputLabel);

        this.nameInput = new FlatTextField();
        nameInput.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
        nameInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        nameInput.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        nameInput.setMinimumSize(new Dimension(0, 30));
        add(nameInput);

        this.tilesLabel = new JLabel("Custom Pack Tiles");
        add(tilesLabel);

        this.tilesInput = new FlatTextField();
        tilesInput.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
        tilesInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        tilesInput.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        tilesInput.setMinimumSize(new Dimension(0, 30));
        add(tilesInput);

        addPackButton = new JButton();
        addPackButton.setText("Add Pack");
        addPackButton.setHorizontalAlignment(JLabel.CENTER);
        addPackButton.setFocusable(false);
        addPackButton.setPreferredSize((new Dimension(PluginPanel.PANEL_WIDTH - 10, 30)));
        addPackButton.addActionListener(e ->
        {
            if (nameInput.getText().isEmpty()) {
                JOptionPane.showMessageDialog(inputLabel, "Must add a pack name");
                return;
            }
            if (tilesInput.getText().isEmpty()) {
                JOptionPane.showMessageDialog(inputLabel, "Must add tiles");
            }
            //check the format of the import points. We actually save the string, so this formatting is just to check it is valid.
            List<GroundMarkerPoint> importPoints;
            try {
                importPoints = gson.fromJson(tilesInput.getText(), new TypeToken<List<GroundMarkerPoint>>() {
                }.getType());
            } catch (JsonSyntaxException ex) {
                JOptionPane.showMessageDialog(inputLabel, "Error parsing tiles, check the formatting");
                return;
            }
            if (importPoints.isEmpty()) {
                JOptionPane.showMessageDialog(inputLabel, "Error parsing tiles, check the formatting");
                return;
            }
            plugin.addCustomPack(nameInput.getText(), tilesInput.getText());
            plugin.loadPacks();
            panel.loadPacks();
        });
        add(addPackButton);
    }
}
