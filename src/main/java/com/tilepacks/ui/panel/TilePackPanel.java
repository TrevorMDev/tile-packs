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

import com.google.gson.Gson;
import com.tilepacks.PointManager;
import com.tilepacks.TilePackManager;
import com.tilepacks.TilePacksPlugin;
import com.tilepacks.data.TilePack;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
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
public class TilePackPanel extends JPanel {

    private static final int ROW_WIDTH = PluginPanel.PANEL_WIDTH - 10;
    private static final int ROW_HEIGHT = 30;
    private static final int RIGHT_PANEL_WIDTH = 40;

    private static final ImageIcon ADD_ICON;
    private static final ImageIcon ADD_ICON_HOVER;
    private static final ImageIcon REMOVE_ICON;
    private static final ImageIcon REMOVE_ICON_HOVER;
    private static final ImageIcon HELP_ICON;
    private static final ImageIcon HELP_ICON_HOVER;
    private static final ImageIcon DELETE_ICON;
    private static final ImageIcon DELETE_ICON_HOVER;

    private final TilePackManager tilePackManager;
    private final PointManager pointManager;
    private final Gson gson;
    private final TilePacksListPanel panel;

    private final JPanel topRow = new JPanel();
    private final JPanel bottomRow = new JPanel();
    private final JPanel controlPanel = new JPanel();
    private final JLabel packName;
    private final JLabel addPack;
    private final JLabel removePack;
    private JLabel helpLink;
    private JLabel deleteCustomPack;

    static {
        final BufferedImage addIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "add_icon.png");
        ADD_ICON = new ImageIcon(addIcon);
        ADD_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));
        final BufferedImage removeIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "remove_icon.png");
        REMOVE_ICON = new ImageIcon(removeIcon);
        REMOVE_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(removeIcon, 0.50f));
        final BufferedImage helpIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "help_icon.png");
        HELP_ICON = new ImageIcon(helpIcon);
        HELP_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(helpIcon, 0.50f));
        final BufferedImage deleteIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "delete_icon.png");
        DELETE_ICON = new ImageIcon(deleteIcon);
        DELETE_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(deleteIcon, 0.50f));
    }

    TilePackPanel(TilePackManager tilePackManager, PointManager pointManager, Gson gson, TilePacksListPanel panel, TilePack pack, boolean enabled) {
        super();
        this.tilePackManager = tilePackManager;
        this.pointManager = pointManager;
        this.gson = gson;
        this.panel = panel;

        log.debug("Loading pack - {}", pack.packName);

        this.setLayout(new BorderLayout());
        this.setBackground(ColorScheme.BRAND_ORANGE);
        this.setBorder(new EmptyBorder(2, 2, 2, 2));

        topRow.setLayout(new BorderLayout());
        topRow.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
        topRow.setPreferredSize(new Dimension(ROW_WIDTH, ROW_HEIGHT));
        topRow.setBorder(new EmptyBorder(4, 4, 4, 4));
        add(topRow, BorderLayout.NORTH);

        bottomRow.setLayout(new BorderLayout());
        bottomRow.setBackground(ColorScheme.DARK_GRAY_COLOR);
        bottomRow.setPreferredSize(new Dimension(ROW_WIDTH, ROW_HEIGHT));
        bottomRow.setBorder(new EmptyBorder(2, 4, 2, 4));
        add(bottomRow, BorderLayout.SOUTH);

        packName = new JLabel(pack.packName);
        packName.setFont(FontManager.getRunescapeFont());
        topRow.add(packName, BorderLayout.WEST);

        controlPanel.setLayout(new BorderLayout());
        controlPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        controlPanel.setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, ROW_HEIGHT));
        bottomRow.add(controlPanel, BorderLayout.EAST);

        addPack = new JLabel();
        addPack.setIcon(ADD_ICON);
        addPack.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    tilePackManager.addEnabledPack(pack.id);
                    pointManager.loadPoints();
                    removePack.setIcon(REMOVE_ICON_HOVER);
                    controlPanel.add(removePack, BorderLayout.EAST);
                    controlPanel.remove(addPack);
                    controlPanel.revalidate();
                    controlPanel.repaint();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                addPack.setIcon(ADD_ICON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                addPack.setIcon(ADD_ICON);
            }
        });

        removePack = new JLabel();
        removePack.setIcon(REMOVE_ICON);
        removePack.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    tilePackManager.removeEnabledPack(pack.id);
                    pointManager.loadPoints();
                    addPack.setIcon(ADD_ICON_HOVER);
                    controlPanel.add(addPack, BorderLayout.EAST);
                    controlPanel.remove(removePack);
                    controlPanel.revalidate();
                    controlPanel.repaint();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                removePack.setIcon(REMOVE_ICON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                removePack.setIcon(REMOVE_ICON);
            }
        });

        if (pack.link != null && pack.link != "") {
            helpLink = new JLabel();
            helpLink.setIcon(HELP_ICON);
            helpLink.setToolTipText("Click to open source of pack in browser");
            helpLink.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        LinkBrowser.browse(pack.link);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    helpLink.setIcon(HELP_ICON_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    helpLink.setIcon(HELP_ICON);
                }
            });

            controlPanel.add(helpLink, BorderLayout.WEST);
        }

        if (enabled) {
            controlPanel.add(removePack, BorderLayout.EAST);
        } else {
            controlPanel.add(addPack, BorderLayout.EAST);
        }

        //anything over 10k is a custom pack
        if (pack.id >= 10000) {
            deleteCustomPack = new JLabel();
            deleteCustomPack.setIcon(DELETE_ICON);
            deleteCustomPack.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        final int result = JOptionPane.showOptionDialog(topRow,
                                "Are you sure you want to delete this pack?",
                                "Delete Pack?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                                null, new String[]{"Yes", "No"}, "No");

                        if (result == JOptionPane.YES_OPTION) {
                            tilePackManager.removeCustomPack(pack.id);
                            tilePackManager.loadPacks();
                            pointManager.loadPoints();
                            panel.loadPacks();
                            controlPanel.revalidate();
                            controlPanel.repaint();
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    deleteCustomPack.setIcon(DELETE_ICON_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    deleteCustomPack.setIcon(DELETE_ICON);
                }
            });
            controlPanel.add(deleteCustomPack, BorderLayout.WEST);
        }
    }
}