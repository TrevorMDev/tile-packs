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
 * UI control that handles the deletion of custom packs
 */
public class DeleteCustomPackLabel extends JLabel {
    private static final ImageIcon DELETE_ICON;
    private static final ImageIcon DELETE_ICON_HOVER;

    private final TilePackManager tilePackManager;
    private final PointManager pointManager;
    private final TilePack tilePack;
    private final TilePacksListPanel tilePacksList;

    static {
        final BufferedImage deleteIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "delete_icon.png");
        DELETE_ICON = new ImageIcon(deleteIcon);
        DELETE_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(deleteIcon, 0.50f));
    }

    DeleteCustomPackLabel(TilePackManager tilePackManager, PointManager pointManager, TilePack tilePack, TilePacksListPanel tilePacksList) {
        super();
        this.tilePackManager = tilePackManager;
        this.pointManager = pointManager;
        this.tilePack = tilePack;
        this.tilePacksList = tilePacksList;

        this.setIcon(DELETE_ICON);
        this.setToolTipText("Delete this custom pack, this is permanent");
        this.addMouseListener(new DeleteCustomPackMouseAdapter(this));
    }

    class DeleteCustomPackMouseAdapter extends MouseAdapter {
        private final DeleteCustomPackLabel deleteCustomPackLabel;

        DeleteCustomPackMouseAdapter(DeleteCustomPackLabel deleteCustomPackLabel) {
            this.deleteCustomPackLabel = deleteCustomPackLabel;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                final int result = JOptionPane.showOptionDialog(deleteCustomPackLabel,
                        "Are you sure you want to delete this pack?",
                        "Delete Pack?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, new String[]{"Yes", "No"}, "No");

                if (result == JOptionPane.YES_OPTION) {
                    tilePackManager.removeCustomPack(tilePack.id);
                    pointManager.loadPoints();
                    tilePacksList.createTilePackPanels();
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            setIcon(DELETE_ICON_HOVER);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            setIcon(DELETE_ICON);
        }
    }
}

