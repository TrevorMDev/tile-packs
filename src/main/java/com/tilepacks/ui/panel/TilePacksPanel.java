package com.tilepacks.ui.panel;

import com.google.gson.Gson;
import com.tilepacks.FilterManager;
import com.tilepacks.PointManager;
import com.tilepacks.TilePackManager;
import com.tilepacks.data.TilePack;
import com.tilepacks.ui.panel.editor.TilePackEditorPanel;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

/** Root navigation panel for the pack list and the pack editor. */
public class TilePacksPanel extends PluginPanel {
    private final TilePackManager tilePackManager;
    private final TilePacksListPanel listPanel;
    private final TilePackEditorPanel editorPanel;
    private JPanel activePanel;

    public TilePacksPanel(TilePackManager tilePackManager,
                          PointManager pointManager,
                          FilterManager filterManager,
                          ChatMessageManager chatMessageManager,
                          Gson gson) {
                this.tilePackManager = tilePackManager;
                setLayout(new BorderLayout());

        listPanel = new TilePacksListPanel(tilePackManager, pointManager, filterManager,
                chatMessageManager, gson, this);
        editorPanel = new TilePackEditorPanel(tilePackManager, this::showList);

        showPanel(listPanel);
    }

    /** Prepares the manager-owned record before displaying it for editing. */
    public void editPack(Integer packId) {
        flushPendingTitle();
        TilePack pack = tilePackManager.preparePackForEditing(packId);
        if (pack == null) {
            return;
        }

        editorPanel.setPack(pack);
        showPanel(editorPanel);
    }

    private void showList() {
        flushPendingTitle();
        listPanel.createTilePackPanels();
        showPanel(listPanel);
    }

    private void showPanel(JPanel panel) {
        if (activePanel == panel) {
            return;
        }
        removeAll();
        activePanel = panel;
        add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /** Stops the debounce timer and immediately saves any outstanding title edit. */
    public void flushPendingTitle() {
        if (editorPanel != null) {
            editorPanel.flushPendingTitle();
        }
    }

    /** Discards a pending title edit when the active RuneLite profile changes. */
    public void cancelPendingTitleEdit() {
        cancelPendingTitle();

        Runnable showList = () -> {
            listPanel.createTilePackPanels();
            showPanel(listPanel);
        };
        if (SwingUtilities.isEventDispatchThread()) {
            showList.run();
        } else {
            SwingUtilities.invokeLater(showList);
        }
    }

    /** Discards a pending title edit without changing the current view. */
    public void cancelPendingTitle() {
        if (editorPanel != null) {
            editorPanel.cancelPendingTitle();
        }
    }

    @Override
    public void removeNotify() {
        cancelPendingTitle();
        super.removeNotify();
    }
}
