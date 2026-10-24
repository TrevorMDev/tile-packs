package com.tilepacks.ui.panel.editor.actions;

import com.tilepacks.TilePacksPlugin;
import net.runelite.client.util.ImageUtil;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/** UI control that returns from the pack editor to the list. */
public class BackLabel extends JLabel {
    private static final ImageIcon BACK_ICON;
    private static final ImageIcon BACK_ICON_HOVER;

    static {
        BufferedImage backIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "back_icon.png");
        BACK_ICON = new ImageIcon(backIcon);
        BACK_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(backIcon, 0.50f));
    }

    public BackLabel(Runnable onBack) {
        setIcon(BACK_ICON);
        setToolTipText("Back to list view");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) {
                    onBack.run();
                }
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                setIcon(BACK_ICON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                setIcon(BACK_ICON);
            }
        });
    }
}
