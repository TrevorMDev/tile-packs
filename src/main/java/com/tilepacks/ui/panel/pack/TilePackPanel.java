package com.tilepacks.ui.panel.pack;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.tilepacks.PointManager;
import com.tilepacks.TilePackManager;
import com.tilepacks.data.TilePack;
import com.tilepacks.ui.panel.TilePacksListPanel;
import com.tilepacks.ui.panel.TilePacksPanel;
import com.tilepacks.ui.panel.pack.actions.CopyToClipboardLabel;
import com.tilepacks.ui.panel.pack.actions.DeleteCustomPackLabel;
import com.tilepacks.ui.panel.pack.actions.EditPackLabel;
import com.tilepacks.ui.panel.pack.actions.HelpLinkLabel;
import com.tilepacks.ui.panel.pack.actions.TogglePackLabel;
import com.tilepacks.ui.panel.pack.actions.ToggleVisibleLabel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

@Slf4j
/**
 * UI container for the TilePack and its controls
 * One exists for each TilePack
 *
 * Icons are from https://www.flaticon.com/
 * All icons are 20x20
 * Grey icons are #858585
 * Green icons are #4CAF50
 * Red icons are #E31C1C
 */
public class TilePackPanel extends JPanel {
    private static final int CONTROL_SIZE = 20;

    private final TilePackManager tilePackManager;
    private final PointManager pointManager;
    private final ChatMessageManager chatMessageManager;
    private final Gson gson;
    private final TilePacksListPanel tilePacksList;
    private final TilePacksPanel rootPanel;

    private final TilePack tilePack;

    private final JPanel topRow = new JPanel();
    private final JPanel controlPanel = new JPanel();
    private final JLabel packName;
    private final JLabel deleteCustomPack;
    private final JLabel editPack;
    private final JLabel toggleVisible;
    private final JLabel copyToClipboard;
    private final JLabel helpLink;
    private final JLabel togglePack;

    public TilePackPanel(TilePackManager tilePackManager,
                  PointManager pointManager,
                  ChatMessageManager chatMessageManager,
                  Gson gson,
                  TilePacksListPanel tilePacksList,
                  TilePacksPanel rootPanel,
                  TilePack tilePack
    ) {
        super();
        this.tilePackManager = tilePackManager;
        this.pointManager = pointManager;
        this.chatMessageManager = chatMessageManager;
        this.gson = gson;
        this.tilePacksList = tilePacksList;
        this.rootPanel = rootPanel;
        this.tilePack = tilePack;

        log.debug("Loading pack - {}", tilePack.packName);

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(3, 2, 3, 2));

        topRow.setLayout(new BorderLayout());
        topRow.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        topRow.setBorder(new EmptyBorder(2, 4, 1, 4));
        add(topRow, BorderLayout.NORTH);

        packName = new JLabel();
        // Disabling html prevents accidental leaking of IP via a shared tile pack
        packName.putClientProperty("html.disable", Boolean.TRUE);
        packName.setText(tilePack.packName);
        packName.setFont(FontManager.getRunescapeFont());
        topRow.add(packName, BorderLayout.WEST);

        controlPanel.setLayout(new FlowLayout(FlowLayout.TRAILING, 4, 6));
        controlPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        controlPanel.setBorder(new MatteBorder(1, 0, 0, 0, ColorScheme.DARK_GRAY_COLOR));
        add(controlPanel, BorderLayout.SOUTH);

        editPack = new EditPackLabel(rootPanel, tilePack.id);
        editPack.setPreferredSize(new Dimension(CONTROL_SIZE, CONTROL_SIZE));
        controlPanel.add(editPack);

        //anything over 10k is a custom pack
        if (tilePack.id >= 10000) {
            deleteCustomPack = new DeleteCustomPackLabel(tilePackManager, pointManager, tilePack, tilePacksList);
        } else {
            deleteCustomPack = new JLabel();
        }
        deleteCustomPack.setPreferredSize(new Dimension(CONTROL_SIZE, CONTROL_SIZE));
        controlPanel.add(deleteCustomPack);

        toggleVisible = new ToggleVisibleLabel(tilePackManager, tilePack, tilePacksList);
        toggleVisible.setPreferredSize(new Dimension(CONTROL_SIZE, CONTROL_SIZE));
        controlPanel.add(toggleVisible);

        copyToClipboard = new CopyToClipboardLabel(chatMessageManager, gson, tilePack);
        copyToClipboard.setPreferredSize(new Dimension(CONTROL_SIZE, CONTROL_SIZE));
        controlPanel.add(copyToClipboard);

        if (!Strings.isNullOrEmpty(tilePack.link)) {
            helpLink = new HelpLinkLabel(tilePack);
        } else {
            helpLink = new JLabel();
        }
        helpLink.setPreferredSize(new Dimension(CONTROL_SIZE, CONTROL_SIZE));
        controlPanel.add(helpLink);

        togglePack = new TogglePackLabel(tilePackManager, pointManager, tilePack);
        togglePack.setPreferredSize(new Dimension(CONTROL_SIZE, CONTROL_SIZE));
        controlPanel.add(togglePack);
    }
}
