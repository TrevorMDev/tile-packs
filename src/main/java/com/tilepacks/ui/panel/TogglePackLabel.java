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

package com.tilepacks.ui.panel;

import com.tilepacks.PointManager;
import com.tilepacks.TilePackManager;
import com.tilepacks.TilePacksPlugin;
import com.tilepacks.data.TilePack;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * UI control that handles the enabling and disabling of the TilePack
 */
public class TogglePackLabel extends JLabel {
    private static final ImageIcon ADD_ICON;
    private static final ImageIcon ADD_ICON_HOVER;
    private static final ImageIcon REMOVE_ICON;
    private static final ImageIcon REMOVE_ICON_HOVER;

    private final TilePackManager tilePackManager;
    private final PointManager pointManager;
    private final TilePack tilePack;

    static {
        // Icon is https://www.flaticon.com/free-icon/plus_1828819
        // Made by https://www.flaticon.com/authors/pixel-perfect
        final BufferedImage addIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "add_icon.png");
        ADD_ICON = new ImageIcon(addIcon);
        ADD_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));
        // Icon is https://www.flaticon.com/free-icon/minus_1143362
        // Made by https://www.flaticon.com/authors/utari-nuraeni
        final BufferedImage removeIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "remove_icon.png");
        REMOVE_ICON = new ImageIcon(removeIcon);
        REMOVE_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(removeIcon, 0.50f));
    }

    TogglePackLabel(TilePackManager tilePackManager, PointManager pointManager, TilePack tilePack) {
        super();
        this.tilePackManager = tilePackManager;
        this.pointManager = pointManager;
        this.tilePack = tilePack;

        if(tilePackManager.isPackEnabled(tilePack.id)) {
            setIcon(REMOVE_ICON);
            setToolTipText("Remove tiles");
        } else {
            setIcon(ADD_ICON);
            setToolTipText("Add tiles");
        }
        addMouseListener(new HelpLinkMouseAdapter());
    }

    class HelpLinkMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                if(tilePackManager.isPackEnabled(tilePack.id)) {
                    //remove click
                    tilePackManager.removeEnabledPack(tilePack.id);
                    setIcon(ADD_ICON_HOVER);
                    setToolTipText("Add tiles");
                } else {
                    //add click
                    tilePackManager.addEnabledPack(tilePack.id);
                    setIcon(REMOVE_ICON_HOVER);
                    setToolTipText("Remove tiles");
                }
                pointManager.loadPoints();
                revalidate();
                repaint();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if(tilePackManager.isPackEnabled(tilePack.id)) {
                setIcon(REMOVE_ICON_HOVER);
            } else {
                setIcon(ADD_ICON_HOVER);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if(tilePackManager.isPackEnabled(tilePack.id)) {
                setIcon(REMOVE_ICON);
            } else {
                setIcon(ADD_ICON);
            }
        }
    }
}

