package com.tilepacks.ui.panel.pack.actions;

import com.tilepacks.TilePackManager;
import com.tilepacks.TilePacksPlugin;
import com.tilepacks.data.TilePack;
import net.runelite.client.config.Keybind;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

import javax.annotation.Nullable;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * UI control that sets the hotkey that toggles a pack on and off.
 * <p>
 * With no hotkey it shows a grey keyboard icon. Once set, the hotkey text (e.g. "Alt+Q") is shown
 * in its place and is clickable the same way.
 * <p>
 * There is no room for save/cancel buttons, so editing is inline:
 * <ul>
 *   <li>Left click: start listening (icon turns orange)</li>
 *   <li>Press a key or combo: it is saved when the keys are released</li>
 *   <li>Escape, or clicking elsewhere: cancel, nothing changes</li>
 *   <li>Backspace/Delete while listening, or right click: clear the hotkey</li>
 * </ul>
 */
public class HotkeyLabel extends JLabel {
    private static final int CONTROL_SIZE = 20;
    // Keeps long combos from pushing the other controls onto a second row; the tooltip has the full text
    private static final int MAX_TEXT_WIDTH = 66;
    // The RuneScape font's glyphs sit high in their line box, so push the text down a little to look
    // vertically centered next to the icons. Tune this if the font or row changes.
    private static final int TEXT_Y_OFFSET = 2;

    private static final Color TEXT_COLOR = ColorScheme.LIGHT_GRAY_COLOR;
    private static final Color TEXT_HOVER_COLOR = ColorUtil.colorWithAlpha(ColorScheme.LIGHT_GRAY_COLOR, 128);

    private static final ImageIcon UNSET_ICON;
    private static final ImageIcon UNSET_ICON_HOVER;
    private static final ImageIcon LISTENING_ICON;

    private final TilePackManager tilePackManager;
    private final Integer packId;

    private boolean listening;
    // Latest key state seen while listening; committed on release so combos like Ctrl+F1 work.
    private Keybind pending;

    static {
        final BufferedImage hotkeyIcon = ImageUtil.loadImageResource(TilePacksPlugin.class, "hotkey_icon.png");
        UNSET_ICON = new ImageIcon(hotkeyIcon);
        UNSET_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(hotkeyIcon, 0.50f));
        LISTENING_ICON = new ImageIcon(ImageUtil.recolorImage(hotkeyIcon, ColorScheme.BRAND_ORANGE));
    }

    public HotkeyLabel(TilePackManager tilePackManager, Integer packId) {
        super();
        this.tilePackManager = tilePackManager;
        this.packId = packId;

        setFont(FontManager.getRunescapeFont());
        // The hotkey text is never markup
        putClientProperty("html.disable", Boolean.TRUE);
        // JLabels are not focusable by default, which is needed to receive key events
        setFocusable(true);
        addMouseListener(new HotkeyMouseAdapter());
        addKeyListener(new HotkeyKeyListener());
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                stopListening();
            }
        });

        refresh();
    }

    @Override
    public Dimension getPreferredSize() {
        // Icon mode matches the other 20x20 controls; text mode is as wide as the text, up to a cap
        if (getIcon() != null) {
            return new Dimension(CONTROL_SIZE, CONTROL_SIZE);
        }
        return new Dimension(Math.min(super.getPreferredSize().width, MAX_TEXT_WIDTH), CONTROL_SIZE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (getIcon() != null) {
            super.paintComponent(g);
            return;
        }
        Graphics shifted = g.create();
        try {
            shifted.translate(0, TEXT_Y_OFFSET);
            super.paintComponent(shifted);
        } finally {
            shifted.dispose();
        }
    }

    @Nullable
    private Keybind currentHotkey() {
        // Read from the manager each time: it can swap the pack instance when a bundled pack is first edited
        TilePack pack = tilePackManager.getTilePacks().get(packId);
        return pack == null ? null : pack.hotkey;
    }

    private void refresh() {
        refresh(false);
    }

    private void refresh(boolean hovering) {
        Keybind hotkey = currentHotkey();
        if (listening) {
            setText(null);
            setIcon(LISTENING_ICON);
            setToolTipText("Press a key or combo. Esc cancels, Backspace clears");
        } else if (hotkey == null) {
            setText(null);
            setIcon(hovering ? UNSET_ICON_HOVER : UNSET_ICON);
            setToolTipText("Click to set a hotkey that toggles this pack");
        } else {
            setIcon(null);
            setText(hotkey.toString());
            setForeground(hovering ? TEXT_HOVER_COLOR : TEXT_COLOR);
            setToolTipText("Hotkey: " + hotkey + " (click to change, right click to clear)");
        }
        // The preferred size changes with the content
        revalidate();
        repaint();
    }

    private void startListening() {
        listening = true;
        pending = null;
        refresh();
        requestFocusInWindow();
    }

    private void stopListening() {
        if (!listening) {
            return;
        }
        listening = false;
        pending = null;
        refresh();
    }

    private void saveHotkey(@Nullable Keybind hotkey) {
        tilePackManager.updatePackHotkey(packId, hotkey);
        stopListening();
        refresh();
    }

    class HotkeyMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (listening) {
                    stopListening();
                } else {
                    startListening();
                }
            } else if (SwingUtilities.isRightMouseButton(e)) {
                saveHotkey(null);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (!listening) {
                refresh(true);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (!listening) {
                refresh(false);
            }
        }
    }

    class HotkeyKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!listening) {
                return;
            }
            e.consume();

            int code = e.getKeyCode();
            if (code == KeyEvent.VK_ESCAPE) {
                stopListening();
            } else if (code == KeyEvent.VK_BACK_SPACE || code == KeyEvent.VK_DELETE) {
                saveHotkey(null);
            } else {
                pending = new Keybind(e);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (listening && pending != null) {
                e.consume();
                saveHotkey(pending);
            }
        }
    }
}
