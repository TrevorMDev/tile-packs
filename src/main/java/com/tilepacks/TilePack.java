/*
 * Copyright (c) 2021, Adam <Adam@sigterm.info>
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

import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class TilePack extends JPanel
{

    private static final int ROW_WIDTH = 200;
    private static final int ROW_HEIGHT = 60;

    private static final ImageIcon ADD_ICON;
    private static final ImageIcon ADD_ICON_HOVER;
    private static final ImageIcon REMOVE_ICON;
    private static final ImageIcon REMOVE_ICON_HOVER;

    private final TilePacksPlugin plugin;

    private final JPanel rowContainer = new JPanel();
    private JLabel packName;
    private JLabel addPack;
    private JLabel removePack;
    private  List<GroundMarkerPoint> points;

    static
    {
        final BufferedImage addIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "add_icon.png");
        ADD_ICON = new ImageIcon(addIcon);
        final BufferedImage addIconHover = ImageUtil.loadImageResource(TilePacksPlugin.class, "add_icon_hover.png");
        ADD_ICON_HOVER = new ImageIcon(addIconHover);
        final BufferedImage removeIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "remove_icon.png");
        REMOVE_ICON = new ImageIcon(removeIcon);
        final BufferedImage removeIconHover = ImageUtil.loadImageResource(TilePacksPlugin.class, "remove_icon_hover.png");
        REMOVE_ICON_HOVER = new ImageIcon(removeIconHover);
    }

    TilePack(TilePacksPlugin plugin, String name, List<GroundMarkerPoint> points)
    {
        super();
        this.plugin = plugin;
        this.points = points;
        rowContainer.setPreferredSize(new Dimension(ROW_WIDTH, ROW_HEIGHT));
        add(rowContainer);

        packName = new JLabel(name);
        packName.setFont(FontManager.getRunescapeFont());
        rowContainer.add(packName);

        addPack = new JLabel();
        addPack.setIcon(ADD_ICON);
        rowContainer.add(addPack);
        addPack.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    log.info("received click, Adding Tile Pack", points);
                    log.info(points.toString());
                    plugin.importGroundMarkers(points);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                addPack.setIcon(ADD_ICON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                addPack.setIcon(ADD_ICON);
            }
        });

        removePack = new JLabel();
        removePack.setIcon(REMOVE_ICON);
        rowContainer.add(removePack);
        removePack.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    log.info("received click, removing Tile Pack", points);
                    log.info(points.toString());
                    plugin.removeGroundMarkers(points);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                removePack.setIcon(REMOVE_ICON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                removePack.setIcon(REMOVE_ICON);
            }
        });
    }
}