package com.tilepacks.ui.panel.pack.actions;

import com.tilepacks.TilePackManager;
import com.tilepacks.TilePacksPlugin;
import com.tilepacks.ui.panel.TilePacksPanel;
import net.runelite.client.util.ImageUtil;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/** UI action that opens a pack in the editor. Only available while the pack is enabled. */
public class EditPackLabel extends JLabel {
    private static final ImageIcon EDIT_ICON;
    private static final ImageIcon EDIT_ICON_HOVER;

    static {
        BufferedImage editIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "edit.png");
        EDIT_ICON = new ImageIcon(editIcon);
        EDIT_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(editIcon, 0.50f));
    }

    private final TilePackManager tilePackManager;
    private final Integer packId;

    public EditPackLabel(TilePackManager tilePackManager, TilePacksPanel rootPanel, Integer packId) {
        this.tilePackManager = tilePackManager;
        this.packId = packId;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (isEditable() && SwingUtilities.isLeftMouseButton(event)) {
                    rootPanel.editPack(packId);
                }
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                if (isEditable()) {
                    setIcon(EDIT_ICON_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                if (isEditable()) {
                    setIcon(EDIT_ICON);
                }
            }
        });

        refresh();
    }

    /** Re-reads whether the pack is enabled and shows or hides the icon to match. */
    public void refresh() {
        if (isEditable()) {
            setIcon(EDIT_ICON);
            setToolTipText("Edit pack title");
        } else {
            // Blank, but the label keeps its preferred size so the other icons don't shift
            setIcon(null);
            setToolTipText(null);
        }
    }

    private boolean isEditable() {
        return tilePackManager.isPackEnabled(packId);
    }
}