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

import com.tilepacks.TilePackConfigManager;
import com.tilepacks.TilePacksPlugin;
import com.tilepacks.data.TilePackConfig;
import com.tilepacks.data.TilePack;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * UI control that handles the toggling of if a pack is visible or hidden
 */
public class ToggleVisibleLabel extends JLabel {
    private static final ImageIcon VISIBLE_ICON;
    private static final ImageIcon VISIBLE_ICON_HOVER;
    private static final ImageIcon INVISIBLE_ICON;
    private static final ImageIcon INVISIBLE_ICON_HOVER;

    private final TilePackConfigManager tilePackConfigManager;

    private final TilePack tilePack;
    private final TilePackConfig tilePackConfig;
    private final TilePacksListPanel tilePacksList;

    static {
        // Icon is https://www.flaticon.com/free-icon/visibility_3395544
        // Made by https://www.flaticon.com/authors/andrean-prabowo
        final BufferedImage visibleIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "visible_icon.png");
        VISIBLE_ICON = new ImageIcon(visibleIcon);
        VISIBLE_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(visibleIcon, 0.50f));

        // Icon is https://www.flaticon.com/free-icon/visible_4175339
        // Made by https://www.flaticon.com/authors/uicon
        final BufferedImage invisibleIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "invisible_icon.png");
        INVISIBLE_ICON = new ImageIcon(invisibleIcon);
        INVISIBLE_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(invisibleIcon, 0.50f));
    }

    ToggleVisibleLabel(TilePackConfigManager tilePackConfigManager,
                       TilePack tilePack,
                       TilePackConfig tilePackConfig,
                       TilePacksListPanel tilePacksList) {
        super();
        this.tilePackConfigManager = tilePackConfigManager;
        this.tilePack = tilePack;
        this.tilePackConfig = tilePackConfig;
        this.tilePacksList = tilePacksList;

        if(tilePackConfig.visible) {
            setIcon(VISIBLE_ICON);
            setToolTipText("Hide pack from list without deleting");
        } else {
            setIcon(INVISIBLE_ICON);
            setToolTipText("Show tile pack in main list");
        }

        this.addMouseListener(new ToggleVisibleMouseAdapter());
    }

    class ToggleVisibleMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                tilePackConfig.visible = !tilePackConfig.visible;
                if(tilePackConfig.visible) {
                    setIcon(VISIBLE_ICON);
                    setToolTipText("Hide pack from list without deleting");
                } else {
                    setIcon(INVISIBLE_ICON);
                    setToolTipText("Show tile pack in main list");
                }
                tilePackConfigManager.updateTilePackConfig(tilePackConfig);
                tilePacksList.createTilePackPanels();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if(tilePackConfig.visible) {
                setIcon(VISIBLE_ICON_HOVER);
            } else {
                setIcon(INVISIBLE_ICON_HOVER);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if(tilePackConfig.visible) {
                setIcon(VISIBLE_ICON);
            } else {
                setIcon(INVISIBLE_ICON);
            }
        }
    }
}

