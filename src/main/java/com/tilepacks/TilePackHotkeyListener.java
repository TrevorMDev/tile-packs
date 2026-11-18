package com.tilepacks;

import com.tilepacks.data.TilePack;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.input.KeyListener;

import javax.swing.SwingUtilities;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/** Toggles a pack's enabled state when its hotkey is pressed. */
public class TilePackHotkeyListener implements KeyListener {
    private final TilePackManager tilePackManager;
    private final PointManager pointManager;
    private final ClientThread clientThread;
    private final Runnable onToggled;

    // Key codes currently held, so holding a key (auto repeat) toggles once
    private final Set<Integer> heldKeys = new HashSet<>();

    /**
     * @param onToggled called after a pack was toggled so the UI can refresh; may be called from any thread
     */
    TilePackHotkeyListener(TilePackManager tilePackManager, PointManager pointManager,
                           ClientThread clientThread, Runnable onToggled) {
        this.tilePackManager = tilePackManager;
        this.pointManager = pointManager;
        this.clientThread = clientThread;
        this.onToggled = onToggled;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!heldKeys.add(e.getKeyCode())) {
            return;
        }

        boolean toggled = false;
        for (TilePack pack : tilePackManager.getTilePacks().values()) {
            if (pack.hotkey != null && pack.hotkey.matches(e)) {
                toggle(pack.id);
                toggled = true;
            }
        }

        if (toggled) {
            clientThread.invoke(pointManager::loadPoints);
            SwingUtilities.invokeLater(onToggled);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        heldKeys.remove(e.getKeyCode());
    }

    private void toggle(Integer packId) {
        if (tilePackManager.isPackEnabled(packId)) {
            tilePackManager.removeEnabledPack(packId);
        } else {
            tilePackManager.addEnabledPack(packId);
        }
    }
}
