package com.tilepacks;

import com.tilepacks.data.GroundMarkerPoint;
import com.tilepacks.data.TilePack;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.chatbox.ChatboxPanelManager;

import javax.inject.Inject;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/** Handles pack scoped scene tile editing. */
public class TilePackEditingManager {
    private final Client client;
    private final TilePackManager tilePackManager;
    private final PointManager pointManager;
    private final TilePacksConfig config;
    private final ChatboxPanelManager chatboxPanelManager;
    private final ClientThread clientThread;

    private volatile Integer editingPackId;

    @Inject
    TilePackEditingManager(Client client, TilePackManager tilePackManager,
                           PointManager pointManager, TilePacksConfig config,
                           ChatboxPanelManager chatboxPanelManager, ClientThread clientThread) {
        this.client = client;
        this.tilePackManager = tilePackManager;
        this.pointManager = pointManager;
        this.config = config;
        this.chatboxPanelManager = chatboxPanelManager;
        this.clientThread = clientThread;
    }

    /** Sets the pack currently being edited */
    public synchronized void editPack(Integer packId) {
        if (packId == null || !tilePackManager.getTilePacks().containsKey(packId)) {
            editingPackId = null;
            return;
        }
        editingPackId = packId;
    }

    public synchronized void stopEditing() {
        editingPackId = null;
    }

    public Integer getEditingPackId() {
        return editingPackId;
    }

    public synchronized void onMenuEntryAdded(MenuEntryAdded event) {
        Integer packId = editingPackId;
        if (!shouldHandleMenuEntry(packId, client.isKeyPressed(KeyCode.KC_SHIFT), event.getMenuEntry().getType())) {
            return;
        }

        int worldViewId = event.getMenuEntry().getWorldViewId();
        WorldView worldView = client.getWorldView(worldViewId);
        if (worldView == null) {
            return;
        }

        Tile selectedTile = worldView.getSelectedSceneTile();
        if (selectedTile == null) {
            return;
        }

        WorldPoint point = WorldPoint.fromLocalInstance(client, selectedTile.getLocalLocation());
        if (point == null) {
            return;
        }

        TilePack pack = tilePackManager.getTilePacks().get(packId);
        if (pack == null) {
            return;
        }

        boolean containsTile = pack.parsedPackTiles.contains(new GroundMarkerPoint(point.getRegionID(), point.getRegionX(),
                point.getRegionY(), point.getPlane(), null, null));
        String packName = pack.packName == null ? "" : pack.packName;
        client.createMenuEntry(-1)
                .setOption((containsTile ? "Remove from '" + packName + "' pack" : "Mark in '" + packName + "' pack") )
                .setWorldViewId(worldViewId)
                .setType(MenuAction.RUNELITE)
                .onClick(menuEvent -> toggleTile(packId, point));

        if (containsTile) {
            client.createMenuEntry(-1)
                    .setOption("Edit label in '" + packName + "' pack")
                    .setWorldViewId(worldViewId)
                    .setType(MenuAction.RUNELITE)
                    .onClick(menuEvent -> promptForLabel(packId, point));
        }
    }

    static boolean shouldHandleMenuEntry(Integer editingPackId, boolean shiftPressed, MenuAction action) {
        return editingPackId != null && shiftPressed
                && (action == MenuAction.WALK || action == MenuAction.SET_HEADING);
    }

    synchronized boolean toggleTile(Integer packId, WorldPoint point) {
        if (!Objects.equals(editingPackId, packId)) {
            return false;
        }

        TilePack pack = tilePackManager.getTilePacks().get(packId);
        if (pack == null) {
            return false;
        }

        GroundMarkerPoint newPoint = new GroundMarkerPoint(point.getRegionID(), point.getRegionX(),
                point.getRegionY(), point.getPlane(), resolveTileColor(pack), null);

        int index = pack.indexOf(point);
        if (index >= 0) {
            pack.parsedPackTiles.remove(index);
        } else {
            pack.parsedPackTiles.add(newPoint);
        }

        saveTiles(packId, pack.parsedPackTiles);
        return true;
    }

    /** Opens the chatbox prompt for a tile's label, pre-filled with the current one. */
    protected void promptForLabel(Integer packId, WorldPoint point) {
        final String currentLabel;
        synchronized (this) {
            if (!Objects.equals(editingPackId, packId)) {
                return;
            }
            TilePack pack = tilePackManager.getTilePacks().get(packId);

            int index = pack.indexOf(point);
            if (index < 0) {
                return;
            }
            currentLabel = pack.parsedPackTiles.get(index).getLabel();
        }

        // Opened outside the lock so the prompt never holds up menu handling
        chatboxPanelManager.openTextInput("Tile label")
                .value(currentLabel == null ? "" : currentLabel)
                .onDone((Consumer<String>) input -> clientThread.invoke(() -> {
                    setTileLabel(packId, point, input);
                }))
                .build();
    }

    /** Sets or clears the label on an existing tile. A blank label clears it. */
    synchronized boolean setTileLabel(Integer packId, WorldPoint point, String label) {
        // The prompt can outlive the state it was opened in: re-check everything
        if (!Objects.equals(editingPackId, packId)) {
            return false;
        }
        TilePack pack = tilePackManager.getTilePacks().get(packId);

        int index = pack.indexOf(point);
        if (index < 0) {
            return false; // tile was removed while the prompt was open
        }

        String normalized = label == null ? null : label.trim();
        if (normalized != null && normalized.isEmpty()) {
            normalized = null;
        }

        GroundMarkerPoint existing = pack.parsedPackTiles.get(index);
        if (Objects.equals(existing.getLabel(), normalized)) {
            return false; // nothing changed, skip the save
        }

        pack.parsedPackTiles.set(index, new GroundMarkerPoint(existing.getRegionId(), existing.getRegionX(),
                existing.getRegionY(), existing.getZ(), existing.getColor(), normalized));

        saveTiles(packId, pack.parsedPackTiles);
        return true;
    }

    /** Shared persistence step for any change to a pack's tiles. */
    private void saveTiles(Integer packId, List<GroundMarkerPoint> points) {
        tilePackManager.updatePackTiles(packId, points);
        pointManager.loadPoints();
    }

    private static GroundMarkerPoint toCoordinate(WorldPoint point) {
        return new GroundMarkerPoint(point.getRegionID(), point.getRegionX(), point.getRegionY(), point.getPlane(), null, null);
    }

    private Color resolveTileColor(TilePack pack) {
        if (pack.defaultColor != null) {
            return pack.defaultColor;
        }
        if (config.overrideColorActive()) {
            return config.overrideColor();
        }
        if (!pack.parsedPackTiles.isEmpty()) {
            Color firstInPack = pack.parsedPackTiles.get(0).getColor();
            if (firstInPack != null) {
                return firstInPack;
            }
        }
        return Color.YELLOW;
    }
}