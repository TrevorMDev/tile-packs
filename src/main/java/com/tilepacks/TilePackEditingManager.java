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
    private final Map<Integer, PackTileCache> tileCaches = new java.util.concurrent.ConcurrentHashMap<>();
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
        getTileCache(packId);
    }

    /** Leaves edit mode without discarding the parsed cache for this profile. */
    public synchronized void stopEditing() {
        editingPackId = null;
    }

    /** Drops profile-specific edit state and parsed data after a profile change. */
    public synchronized void clear() {
        editingPackId = null;
        tileCaches.clear();
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

        PackTileCache cache = getTileCache(packId);
        if (cache == null) {
            return;
        }

        TilePack pack = tilePackManager.getTilePacks().get(packId);
        if (pack == null) {
            return;
        }

        boolean containsTile = cache.contains(point);
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

        PackTileCache cache = getTileCache(packId);
        TilePack pack = tilePackManager.getTilePacks().get(packId);
        if (cache == null || pack == null) {
            return false;
        }

        TileCoordinate coordinate = toCoordinate(point);

        if (cache.coordinates.remove(coordinate)) {
            cache.points.removeIf(coordinate::matches);
        } else {
            GroundMarkerPoint tile = new GroundMarkerPoint(coordinate.regionId, coordinate.regionX,
                    coordinate.regionY, coordinate.plane, resolveTileColor(pack), null);
            cache.points.add(tile);
            cache.coordinates.add(coordinate);
        }

        saveTiles(packId, cache);
        return true;
    }

    /** Opens the chatbox prompt for a tile's label, pre-filled with the current one. */
    protected void promptForLabel(Integer packId, WorldPoint point) {
        final String currentLabel;
        synchronized (this) {
            if (!Objects.equals(editingPackId, packId)) {
                return;
            }
            PackTileCache cache = getTileCache(packId);
            if (cache == null) {
                return;
            }
            int index = cache.indexOf(toCoordinate(point));
            if (index < 0) {
                return;
            }
            currentLabel = cache.points.get(index).getLabel();
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

        PackTileCache cache = getTileCache(packId);
        if (cache == null) {
            return false;
        }

        int index = cache.indexOf(toCoordinate(point));
        if (index < 0) {
            return false; // tile was removed while the prompt was open
        }

        String normalized = label == null ? null : label.trim();
        if (normalized != null && normalized.isEmpty()) {
            normalized = null;
        }

        GroundMarkerPoint existing = cache.points.get(index);
        if (Objects.equals(existing.getLabel(), normalized)) {
            return false; // nothing changed, skip the save
        }

        cache.points.set(index, new GroundMarkerPoint(existing.getRegionId(), existing.getRegionX(),
                existing.getRegionY(), existing.getZ(), existing.getColor(), normalized));

        saveTiles(packId, cache);
        return true;
    }

    /** Shared persistence step for any change to a pack's tiles. */
    private void saveTiles(Integer packId, PackTileCache cache) {
        tilePackManager.updatePackTiles(packId, cache.points);
        pointManager.loadPoints();
    }

    private static TileCoordinate toCoordinate(WorldPoint point) {
        return new TileCoordinate(point.getRegionID(), point.getRegionX(), point.getRegionY(), point.getPlane());
    }

    private Color resolveTileColor(TilePack pack) {
        if (pack.defaultColor != null) {
            return pack.defaultColor;
        }
        if (config.overrideColorActive()) {
            return config.overrideColor();
        }
        return Color.YELLOW;
    }

    /** Builds a retained edit cache from the pack's parsed tiles, not once per menu/click event. */
    private PackTileCache getTileCache(Integer packId) {
        TilePack pack = tilePackManager.getTilePacks().get(packId);
        if (pack == null || pack.parsedPackTiles == null) {
            return null;
        }

        PackTileCache cache = tileCaches.get(packId);
        if (cache != null) {
            return cache;
        }

        cache = new PackTileCache(pack.parsedPackTiles);
        tileCaches.put(packId, cache);
        return cache;
    }

    private static final class PackTileCache {
        private final List<GroundMarkerPoint> points;
        private final Set<TileCoordinate> coordinates = new HashSet<>();

        private PackTileCache(List<GroundMarkerPoint> points) {
            this.points = new ArrayList<>(points);
            for (GroundMarkerPoint point : points) {
                coordinates.add(new TileCoordinate(point.getRegionId(), point.getRegionX(),
                        point.getRegionY(), point.getZ()));
            }
        }

        private boolean contains(WorldPoint point) {
            return coordinates.contains(new TileCoordinate(point.getRegionID(), point.getRegionX(),
                    point.getRegionY(), point.getPlane()));
        }

        private int indexOf(TileCoordinate coordinate) {
            for (int i = 0; i < points.size(); i++) {
                if (coordinate.matches(points.get(i))) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static final class TileCoordinate {
        private final int regionId;
        private final int regionX;
        private final int regionY;
        private final int plane;

        private TileCoordinate(int regionId, int regionX, int regionY, int plane) {
            this.regionId = regionId;
            this.regionX = regionX;
            this.regionY = regionY;
            this.plane = plane;
        }

        private boolean matches(GroundMarkerPoint point) {
            return point.getRegionId() == regionId && point.getRegionX() == regionX
                    && point.getRegionY() == regionY && point.getZ() == plane;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TileCoordinate)) {
                return false;
            }
            TileCoordinate that = (TileCoordinate) other;
            return regionId == that.regionId && regionX == that.regionX
                    && regionY == that.regionY && plane == that.plane;
        }

        @Override
        public int hashCode() {
            return Objects.hash(regionId, regionX, regionY, plane);
        }
    }
}