package com.tilepacks.ui.panel.editor;

import com.tilepacks.TilePackManager;
import com.tilepacks.TilePacksConfig;
import com.tilepacks.data.TilePack;
import com.tilepacks.ui.panel.editor.actions.BackLabel;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

/** Minimal title editor for a tile pack. */
public class TilePackEditorPanel extends PluginPanel {
    private final TilePackManager tilePackManager;
    private final Client client;
    private final ColorPickerManager colorPickerManager;
    private final TilePacksConfig config;
    private final JTextField titleField = new JTextField();
    private final JButton defaultColorButton = new JButton();
    private final Timer saveTimer;

    private Integer packId;
    private boolean updatingField;
    private volatile boolean titleSavePending;

    public TilePackEditorPanel(TilePackManager tilePackManager, Client client,
                               ColorPickerManager colorPickerManager, TilePacksConfig config,
                               Runnable onBack) {
        this.tilePackManager = tilePackManager;
        this.client = client;
        this.colorPickerManager = colorPickerManager;
        this.config = config;
        setLayout(new BorderLayout());

        BackLabel backButton = new BackLabel(() -> {
            flushPendingTitle();
            onBack.run();
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 0, 0));
        header.add(backButton, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(12, 0, 8, 0));
        content.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JLabel titleLabel = new JLabel("Pack Name");
        titleLabel.setFont(FontManager.getRunescapeFont());
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        titleField.setColumns(24);
        Dimension titleFieldSize = new Dimension(Integer.MAX_VALUE, titleField.getPreferredSize().height + 4);
        titleField.setPreferredSize(new Dimension(titleField.getPreferredSize().width, titleFieldSize.height));
        titleField.setMaximumSize(titleFieldSize);
        titleField.setAlignmentX(LEFT_ALIGNMENT);
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(titleField);
        JLabel colorLabel = new JLabel("Default Tile Color");
        colorLabel.setFont(FontManager.getRunescapeFont());
        colorLabel.setAlignmentX(LEFT_ALIGNMENT);
        defaultColorButton.setAlignmentX(LEFT_ALIGNMENT);
        defaultColorButton.setToolTipText("Choose the default color for new tiles in this pack");
        defaultColorButton.addActionListener(event -> openColorPicker());
        content.add(Box.createVerticalStrut(12));
        content.add(colorLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(defaultColorButton);
        add(content, BorderLayout.CENTER);
        //temporary while this is in development, as it will not all be released together.
        content.add(Box.createVerticalStrut(24));
        JLabel comingSoon = new JLabel("More editing options coming soon");
        comingSoon.setFont(FontManager.getRunescapeFont());
        comingSoon.setAlignmentX(LEFT_ALIGNMENT);
        content.add(comingSoon);

        saveTimer = new Timer(200, event -> savePendingTitle());
        saveTimer.setRepeats(false);
        titleField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                titleChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                titleChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                titleChanged();
            }
        });
    }

    public void setPack(TilePack pack) {
        flushPendingTitle();
        packId = pack.id;
        updateColorSwatch(resolveDefaultColor(pack));
        updatingField = true;
        try {
            titleField.setText(pack.packName == null ? "" : pack.packName);
        } finally {
            updatingField = false;
        }
    }

    public Integer getPackId() {
        return packId;
    }

    public void clearPack() {
        cancelPendingTitle();
        packId = null;
    }

    private Color resolveDefaultColor(TilePack pack) {
        if (pack.defaultColor != null) {
            return pack.defaultColor;
        }
        if (config.overrideColorActive()) {
            return config.overrideColor();
        }
        return Color.YELLOW;
    }

    private void updateColorSwatch(Color color) {
        defaultColorButton.setBackground(color);
        defaultColorButton.setOpaque(true);
        defaultColorButton.setText(String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
        defaultColorButton.setForeground(color.getRed() + color.getGreen() + color.getBlue() < 384
                ? Color.WHITE : Color.BLACK);
    }

    private void openColorPicker() {
        if (packId == null) {
            return;
        }

        Integer selectedPackId = packId;
        TilePack pack = tilePackManager.getTilePacks().get(selectedPackId);
        if (pack == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            RuneliteColorPicker colorPicker = colorPickerManager.create(
                    client, resolveDefaultColor(pack), "New Tile Color", false);
            colorPicker.setOnClose(color -> {
                if (color == null || !selectedPackId.equals(packId)) {
                    return;
                }
                TilePack currentPack = tilePackManager.getTilePacks().get(selectedPackId);
                if (currentPack == null) {
                    return;
                }
                tilePackManager.updatePackDefaultColor(selectedPackId, color);
                updateColorSwatch(color);
            });
            colorPicker.setVisible(true);
        });
    }

    private void titleChanged() {
        if (!updatingField && packId != null) {
            titleSavePending = true;
            saveTimer.restart();
        }
    }

    private void savePendingTitle() {
        if (titleSavePending && packId != null) {
            titleSavePending = false;
            tilePackManager.updatePackTitle(packId, titleField.getText());
        }
    }

    /** Stops the debounce timer and synchronously saves the current field value if needed. */
    public void flushPendingTitle() {
        saveTimer.stop();
        savePendingTitle();
    }

    /** Cancels a debounced edit that must not be written to a newly active profile. */
    public void cancelPendingTitle() {
        titleSavePending = false;
        saveTimer.stop();
    }
}
