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
public class InvisibleHeaderLabel extends JLabel {
    private static final ImageIcon INVISIBLE_ICON_ACTIVE;
    private static final ImageIcon INVISIBLE_ICON_ACTIVE_HOVER;
    private static final ImageIcon INVISIBLE_ICON;
    private static final ImageIcon INVISIBLE_ICON_HOVER;

    private final FilterManager filterManager;
    private final TilePacksListPanel tilePacksList;

    static {
        // Icon is https://www.flaticon.com/free-icon/visible_4175339
        // Made by https://www.flaticon.com/authors/uicon
        final BufferedImage invisibleIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "invisible_icon.png");
        INVISIBLE_ICON_ACTIVE = new ImageIcon(ImageUtil.recolorImage(invisibleIcon, Color.white));
        INVISIBLE_ICON_ACTIVE_HOVER = new ImageIcon(ImageUtil.recolorImage(invisibleIcon, ColorUtil.colorWithAlpha(Color.white, 128)));
        INVISIBLE_ICON = new ImageIcon(invisibleIcon);
        INVISIBLE_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(invisibleIcon, 0.50f));
    }

    InvisibleHeaderLabel(FilterManager filterManager, TilePacksListPanel tilePacksList) {
        super();
        this.filterManager = filterManager;
        this.tilePacksList = tilePacksList;

        if(filterManager.getShowInvisible()) {
            setIcon(INVISIBLE_ICON_ACTIVE);
        } else {
            setIcon(INVISIBLE_ICON);
        }
        setToolTipText("Show invisible packs");

        this.addMouseListener(new InvisibleHeaderLabelMouseAdapter());
    }

    class InvisibleHeaderLabelMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                if(filterManager.getShowInvisible()) {
                    filterManager.setShowInvisible(false);
                    setIcon(INVISIBLE_ICON_HOVER);
                } else {
                    filterManager.setShowInvisible(true);
                    setIcon(INVISIBLE_ICON_ACTIVE_HOVER);
                }
                tilePacksList.createTilePackPanels();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if(filterManager.getShowInvisible()) {
                setIcon(INVISIBLE_ICON_ACTIVE_HOVER);
            } else {
                setIcon(INVISIBLE_ICON_HOVER);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if(filterManager.getShowInvisible()) {
                setIcon(INVISIBLE_ICON_ACTIVE);
            } else {
                setIcon(INVISIBLE_ICON);
            }
        }
    }
}

