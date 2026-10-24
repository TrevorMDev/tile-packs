package com.tilepacks.ui.panel.pack.actions;

import com.tilepacks.TilePacksPlugin;
import com.tilepacks.ui.panel.TilePacksPanel;
import net.runelite.client.util.ImageUtil;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/** UI action that opens a pack in the title editor. */
public class EditPackLabel extends JLabel {
    private static final ImageIcon EDIT_ICON;
    private static final ImageIcon EDIT_ICON_HOVER;

    static {
        BufferedImage editIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "edit.png");
        EDIT_ICON = new ImageIcon(editIcon);
        EDIT_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(editIcon, 0.50f));
    }

    public EditPackLabel(TilePacksPanel rootPanel, Integer packId) {
        setIcon(EDIT_ICON);
        setToolTipText("Edit pack title");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) {
                    rootPanel.editPack(packId);
                }
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                setIcon(EDIT_ICON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                setIcon(EDIT_ICON);
            }
        });
    }
}
