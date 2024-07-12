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
import com.tilepacks.TilePacksPlugin;
import com.tilepacks.ui.panel.TilePacksListPanel;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * UI control that handles the filtering of the list as visible, invisible or both.
 */
public class VisibleHeaderLabel extends JLabel {
    private static final ImageIcon VISIBLE_ICON_ACTIVE;
    private static final ImageIcon VISIBLE_ICON_ACTIVE_HOVER;
    private static final ImageIcon VISIBLE_ICON;
    private static final ImageIcon VISIBLE_ICON_HOVER;

    private final FilterManager filterManager;
    private final TilePacksListPanel tilePacksList;

    static {
        // Icon is https://www.flaticon.com/free-icon/visibility_3395544
        // Made by https://www.flaticon.com/authors/andrean-prabowo
        final BufferedImage visibleIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "visible_icon.png");
        VISIBLE_ICON_ACTIVE = new ImageIcon(ImageUtil.recolorImage(visibleIcon, Color.white));
        VISIBLE_ICON_ACTIVE_HOVER = new ImageIcon(ImageUtil.recolorImage(visibleIcon, ColorUtil.colorWithAlpha(Color.white, 128)));
        VISIBLE_ICON = new ImageIcon(visibleIcon);
        VISIBLE_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(visibleIcon, 0.50f));
    }

    VisibleHeaderLabel(FilterManager filterManager, TilePacksListPanel tilePacksList) {
        super();
        this.filterManager = filterManager;
        this.tilePacksList = tilePacksList;

        if(filterManager.getShowVisible()) {
            setIcon(VISIBLE_ICON_ACTIVE);
        } else {
            setIcon(VISIBLE_ICON);
        }
        setToolTipText("Show visible tile packs");

        this.addMouseListener(new VisibleHeaderLabelMouseAdapter());
    }

    class VisibleHeaderLabelMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                if(filterManager.getShowVisible()) {
                    filterManager.setShowVisible(false);
                    setIcon(VISIBLE_ICON_HOVER);
                } else {
                    filterManager.setShowVisible(true);
                    setIcon(VISIBLE_ICON_ACTIVE_HOVER);
                }
                tilePacksList.createTilePackPanels();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if(filterManager.getShowVisible()) {
                setIcon(VISIBLE_ICON_ACTIVE_HOVER);
            } else {
                setIcon(VISIBLE_ICON_HOVER);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if(filterManager.getShowVisible()) {
                setIcon(VISIBLE_ICON_ACTIVE);
            } else {
                setIcon(VISIBLE_ICON);
            }
        }
    }
}

