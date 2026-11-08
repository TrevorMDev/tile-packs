package com.tilepacks.ui.panel.custom;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.tilepacks.PointManager;
import com.tilepacks.TilePackImportValidator;
import com.tilepacks.TilePackManager;
import com.tilepacks.data.GroundMarkerPoint;
import com.tilepacks.ui.panel.TilePacksListPanel;
import com.tilepacks.ui.panel.TilePacksPanel;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Objects;

/**
 * UI that handles the addition of new tile packs
 */
public class CustomPackManagerPanel extends PluginPanel {

    private final TilePackManager tilePackManager;
    private final PointManager pointManager;
    private final Gson gson;
    private final TilePacksPanel rootPanel;
    private final TilePacksListPanel tilePacksList;

    private final JButton importPackButton;
    private final JButton createEmptyPackButton;
    private final JPanel discordRow;
    private final JLabel discordShare;
    private final JLabel discordShareButton;

    public CustomPackManagerPanel(TilePackManager tilePackManager, PointManager pointManager,
                                  Gson gson, TilePacksPanel rootPanel, TilePacksListPanel tilePacksList) {
        super();
        this.tilePackManager = tilePackManager;
        this.pointManager = pointManager;
        this.gson = gson;
        this.rootPanel = rootPanel;
        this.tilePacksList = tilePacksList;

        importPackButton = new JButton("Import Pack with Tiles");
        importPackButton.setHorizontalAlignment(JLabel.CENTER);
        importPackButton.setFocusable(false);
        importPackButton.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 10, 30));
        importPackButton.addActionListener(e -> showPackDialog(true));
        add(importPackButton);

        createEmptyPackButton = new JButton("Create Empty Pack");
        createEmptyPackButton.setHorizontalAlignment(JLabel.CENTER);
        createEmptyPackButton.setFocusable(false);
        createEmptyPackButton.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 10, 30));
        createEmptyPackButton.addActionListener(e -> showPackDialog(false));
        add(createEmptyPackButton);

        discordRow = new JPanel();
        discordRow.setLayout(new BorderLayout());
        discordRow.setBorder(new EmptyBorder(6, 4, 1, 4));
        add(discordRow, BorderLayout.SOUTH);

        discordShare = new JLabel("Got cool tiles? Share in discord!");
        discordRow.add(discordShare, BorderLayout.WEST);

        discordShareButton = new DiscordLinkLabel();
        discordRow.add(discordShareButton, BorderLayout.EAST);
    }

    /**
     * Shows a dialog asking for a pack name (and tiles, when importing).
     * Validation happens when submitting. Errors are shown inline and the dialog stays open.
     */
    private void showPackDialog(boolean withTiles) {
        final Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dialog = new JDialog(owner, withTiles ? "Import Pack" : "Create Empty Pack",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        final JTextField nameField = new JTextField(24);
        final JTextArea tilesArea = new JTextArea(4, 24);
        tilesArea.setLineWrap(true);
        final JCheckBox editModeBox = new JCheckBox("Enter edit mode?");
        final JLabel errorLabel = new JLabel(" "); // blank but reserves the row so the layout doesn't jump
        errorLabel.setForeground(Color.RED);

        final JButton okButton = new JButton("OK");
        final JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            final String name = nameField.getText().trim();
            final String tiles = tilesArea.getText().trim();

            final String error = validate(name, tiles, withTiles);
            if (error != null) {
                errorLabel.setText(error);
                return;
            }

            dialog.dispose();
            createPack(name, withTiles ? tiles : "[]", editModeBox.isSelected());
        });
        cancelButton.addActionListener(e -> dialog.dispose());

        // Layout
        final JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(10, 10, 4, 10));

        final JLabel nameLabel = new JLabel("Pack Name");
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        nameField.setAlignmentX(LEFT_ALIGNMENT);
        form.add(nameLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(nameField);

        if (withTiles) {
            final JLabel tilesLabel = new JLabel("Pack Tiles");
            tilesLabel.setAlignmentX(LEFT_ALIGNMENT);
            final JScrollPane tilesScroll = new JScrollPane(tilesArea);
            tilesScroll.setAlignmentX(LEFT_ALIGNMENT);
            form.add(Box.createVerticalStrut(10));
            form.add(tilesLabel);
            form.add(Box.createVerticalStrut(4));
            form.add(tilesScroll);
        }

        editModeBox.setAlignmentX(LEFT_ALIGNMENT);
        form.add(Box.createVerticalStrut(10));
        form.add(editModeBox);

        errorLabel.setAlignmentX(LEFT_ALIGNMENT);
        form.add(Box.createVerticalStrut(6));
        form.add(errorLabel);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(okButton);
        buttons.add(cancelButton);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);

        // Enter = OK (except inside the tiles box, where it types a newline), Escape = Cancel
        dialog.getRootPane().setDefaultButton(okButton);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                nameField.requestFocusInWindow();
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** Returns an error message, or null if the input is valid. */
    private String validate(String name, String tiles, boolean withTiles) {
        if (name.isEmpty()) {
            return "Must add a pack name";
        }
        if (withTiles) {
            if (tiles.isEmpty()) {
                return "Must add tiles";
            }
            if (!areTilesValid(tiles)) {
                return "Error parsing tiles, check the formatting";
            }
        }
        return null;
    }

    private void createPack(String name, String tilesJson, boolean enterEditMode) {
        final Integer packId = tilePackManager.addCustomPack(name, tilesJson);
        pointManager.loadPoints();
        tilePacksList.createTilePackPanels();

        // Switch to the editor last, once the pack exists and the list has been rebuilt
        if (enterEditMode && packId != null) {
            rootPanel.editPack(packId);
        }
    }

    private boolean areTilesValid(String tilesJson) {
        // We save the raw string, so parsing here is only to check that it is valid.
        final List<GroundMarkerPoint> importPoints;
        try {
            importPoints = gson.fromJson(tilesJson, new TypeToken<List<GroundMarkerPoint>>() {
            }.getType());
        } catch (Exception ex) {
            return false;
        }

        return importPoints != null
                && !importPoints.isEmpty()
                && importPoints.stream().noneMatch(Objects::isNull)
                && TilePackImportValidator.isValid(tilesJson, gson);
    }
}