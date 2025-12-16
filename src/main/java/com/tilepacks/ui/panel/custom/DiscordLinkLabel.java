package com.tilepacks.ui.panel.custom;

import com.tilepacks.TilePacksPlugin;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * UI Label that links to the Tile Packs discord
 */
public class DiscordLinkLabel extends JLabel {
    private static final ImageIcon DISCORD_ICON;
    private static final ImageIcon DISCORD_ICON_HOVER;

    static {
        final BufferedImage discordIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "discord.png");
        DISCORD_ICON = new ImageIcon(discordIcon);
        DISCORD_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(discordIcon, 0.50f));
    }

    DiscordLinkLabel() {
        super();
        setIcon(DISCORD_ICON);
        setToolTipText("Click to join the Tile Packs discord");
        addMouseListener(new HelpLinkMouseAdapter());
    }


    class HelpLinkMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                LinkBrowser.browse("https://discord.gg/4EQRWxY3Wb");
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            setIcon(DISCORD_ICON_HOVER);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            setIcon(DISCORD_ICON);
        }
    }
}

