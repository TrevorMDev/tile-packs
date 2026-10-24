package com.tilepacks.ui.panel.editor;

import com.tilepacks.TilePackManager;
import com.tilepacks.data.TilePack;
import com.tilepacks.ui.panel.editor.actions.BackLabel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/** Minimal title editor for a tile pack. */
public class TilePackEditorPanel extends PluginPanel {
    private final TilePackManager tilePackManager;
    private final JTextField titleField = new JTextField();
    private final Timer saveTimer;

    private Integer packId;
    private boolean updatingField;
    private volatile boolean titleSavePending;

    public TilePackEditorPanel(TilePackManager tilePackManager, Runnable onBack) {
        this.tilePackManager = tilePackManager;
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
        add(content, BorderLayout.CENTER);

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
        updatingField = true;
        try {
            titleField.setText(pack.packName == null ? "" : pack.packName);
        } finally {
            updatingField = false;
        }
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
