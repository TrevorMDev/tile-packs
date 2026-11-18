package com.tilepacks;

import com.tilepacks.data.TilePack;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.Keybind;
import org.junit.Test;

import javax.swing.JPanel;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TilePackHotkeyListenerTest {
    private static final int PACK_ID = 10000;

    private final TilePackManager tilePackManager = mock(TilePackManager.class);
    private final PointManager pointManager = mock(PointManager.class);
    private final ClientThread clientThread = mock(ClientThread.class);
    private final TilePackHotkeyListener listener =
            new TilePackHotkeyListener(tilePackManager, pointManager, clientThread, () -> { });

    private void givenPackWithHotkey(Keybind hotkey) {
        TilePack pack = new TilePack(PACK_ID, "Pack", "[]");
        pack.hotkey = hotkey;
        Map<Integer, TilePack> packs = new HashMap<>();
        packs.put(PACK_ID, pack);
        when(tilePackManager.getTilePacks()).thenReturn(packs);
    }

    // Keybind.matches compares getExtendedKeyCode(), which is only populated on native events
    // and is 0 on hand-built ones, so report it the way a real key event would.
    private static KeyEvent keyEvent(int id, int keyCode, int modifiers) {
        return new KeyEvent(new JPanel(), id, 0L, modifiers, keyCode, KeyEvent.CHAR_UNDEFINED) {
            @Override
            public int getExtendedKeyCode() {
                return keyCode;
            }
        };
    }

    private static KeyEvent press(int keyCode, int modifiers) {
        return keyEvent(KeyEvent.KEY_PRESSED, keyCode, modifiers);
    }

    private static KeyEvent release(int keyCode) {
        return keyEvent(KeyEvent.KEY_RELEASED, keyCode, 0);
    }

    @Test
    public void matchingKeyEnablesADisabledPackAndReloadsPoints() {
        givenPackWithHotkey(new Keybind(KeyEvent.VK_F1, 0));
        when(tilePackManager.isPackEnabled(PACK_ID)).thenReturn(false);

        listener.keyPressed(press(KeyEvent.VK_F1, 0));

        verify(tilePackManager).addEnabledPack(PACK_ID);
        verify(tilePackManager, never()).removeEnabledPack(any());
        verify(clientThread).invoke(any(Runnable.class));
    }

    @Test
    public void matchingKeyDisablesAnEnabledPack() {
        givenPackWithHotkey(new Keybind(KeyEvent.VK_F1, 0));
        when(tilePackManager.isPackEnabled(PACK_ID)).thenReturn(true);

        listener.keyPressed(press(KeyEvent.VK_F1, 0));

        verify(tilePackManager).removeEnabledPack(PACK_ID);
        verify(tilePackManager, never()).addEnabledPack(any());
    }

    @Test
    public void holdingTheKeyTogglesOnlyOnce() {
        givenPackWithHotkey(new Keybind(KeyEvent.VK_F1, 0));

        listener.keyPressed(press(KeyEvent.VK_F1, 0));
        listener.keyPressed(press(KeyEvent.VK_F1, 0));
        listener.keyPressed(press(KeyEvent.VK_F1, 0));
        verify(tilePackManager, times(1)).addEnabledPack(PACK_ID);

        listener.keyReleased(release(KeyEvent.VK_F1));
        listener.keyPressed(press(KeyEvent.VK_F1, 0));
        verify(tilePackManager, times(2)).addEnabledPack(PACK_ID);
    }

    @Test
    public void modifiersMustMatch() {
        givenPackWithHotkey(new Keybind(KeyEvent.VK_F1, InputEvent.CTRL_DOWN_MASK));

        listener.keyPressed(press(KeyEvent.VK_F1, 0));

        verify(tilePackManager, never()).addEnabledPack(any());
        verify(clientThread, never()).invoke(any(Runnable.class));
    }

    @Test
    public void packsWithoutAHotkeyAreIgnored() {
        givenPackWithHotkey(null);

        listener.keyPressed(press(KeyEvent.VK_F1, 0));

        verify(tilePackManager, never()).addEnabledPack(any());
        verify(tilePackManager, never()).removeEnabledPack(any());
    }
}
