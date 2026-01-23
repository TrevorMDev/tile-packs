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

import com.tilepacks.TilePacksPlugin;
import com.tilepacks.data.TilePack;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * UI control that handles the opening of help links
 */
public class HelpLinkLabel extends JLabel {
    private static final ImageIcon HELP_ICON;
    private static final ImageIcon HELP_ICON_HOVER;
    private static final ImageIcon YOUTUBE_ICON;
    private static final ImageIcon YOUTUBE_ICON_HOVER;
    private static final ImageIcon DISCORD_ICON;
    private static final ImageIcon DISCORD_ICON_HOVER;

    private final TilePack tilePack;

    static {
        // Icon is https://www.flaticon.com/free-icon/resize_3388930
        // Made by https://www.flaticon.com/authors/pixel-perfect
        final BufferedImage helpIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "help_icon.png");
        HELP_ICON = new ImageIcon(helpIcon);
        HELP_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(helpIcon, 0.50f));

        final BufferedImage youtubeIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "youtube.png");
        YOUTUBE_ICON = new ImageIcon(youtubeIcon);
        YOUTUBE_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(youtubeIcon, 0.50f));

        final BufferedImage discordIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "discord.png");
        DISCORD_ICON = new ImageIcon(discordIcon);
        DISCORD_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(discordIcon, 0.50f));
    }

    HelpLinkLabel(TilePack tilePack) {
        super();
        this.tilePack = tilePack;

        setIcon(determineIcon(false));
        setToolTipText("Click to open source of pack in browser");
        addMouseListener(new HelpLinkMouseAdapter());
    }

    private ImageIcon determineIcon(boolean hovering) {
        if(this.tilePack.link.contains("youtube.com") || this.tilePack.link.contains("youtu.be")) {
            return hovering ? YOUTUBE_ICON_HOVER : YOUTUBE_ICON;
        } else if(this.tilePack.link.contains("discord.gg")) {
            return hovering ? DISCORD_ICON_HOVER : DISCORD_ICON;
        }
        return hovering ? HELP_ICON_HOVER : HELP_ICON;
    }

    class HelpLinkMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                LinkBrowser.browse(tilePack.link);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            setIcon(determineIcon(true));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            setIcon(determineIcon(false));
        }
    }
}

