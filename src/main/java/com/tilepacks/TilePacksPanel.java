package com.tilepacks;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

class TilePacksPanel extends PluginPanel
{

    private final JPanel listContainer = new JPanel();
    TilePacksPanel()
    {
        super();
        listContainer.setLayout(new GridLayout(0, 1));
        add(listContainer);
    }
}