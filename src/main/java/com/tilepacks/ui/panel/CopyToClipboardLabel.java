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

import com.google.gson.Gson;
import com.tilepacks.TilePacksPlugin;
import com.tilepacks.data.TilePack;
import net.runelite.api.ChatMessageType;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * UI control that handles copying the tiles of a pack to the clipboard
 */
public class CopyToClipboardLabel extends JLabel {
    private static final ImageIcon COPY_ICON;
    private static final ImageIcon COPY_ICON_HOVER;

    private final ChatMessageManager chatMessageManager;
    private final Gson gson;
    private final TilePack tilePack;

    static {
        // Icon is https://www.flaticon.com/free-icon/close_1828665
        // Made by https://www.flaticon.com/authors/pixel-perfect
        final BufferedImage copyIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "copy_icon.png");
        COPY_ICON = new ImageIcon(copyIcon);
        COPY_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(copyIcon, 0.50f));
    }

    CopyToClipboardLabel(ChatMessageManager chatMessageManager, Gson gson, TilePack tilePack) {
        super();
        this.chatMessageManager = chatMessageManager;
        this.gson = gson;
        this.tilePack = tilePack;

        setIcon(COPY_ICON);
        setToolTipText("Copy tiles of pack to clipboard");
        addMouseListener(new CopyToClipboardMouseAdapter());
    }

    class CopyToClipboardMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                final String copy = tilePack.packTiles;

                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(new StringSelection(copy), null);
                chatMessageManager.queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(tilePack.packName + " tiles copied to clipboard")
                        .build());
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            setIcon(COPY_ICON_HOVER);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            setIcon(COPY_ICON);
        }
    }
}

