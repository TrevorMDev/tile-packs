package com.tilepacks;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.tilepacks.data.GroundMarkerPoint;
import com.tilepacks.data.TilePack;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuEntryAdded;

import javax.inject.Inject;
import java.awt.Color;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Handles pack scoped scene tile editing. */
@Slf4j
public class TilePackEditingManager {
    private static final Type TILE_LIST_TYPE = new TypeToken<List<GroundMarkerPoint>>() { }.getType();

    private final Client client;
    private final Gson gson;
    private final TilePackManager tilePackManager;
    private final PointManager pointManager;
    private final TilePacksConfig config;
    private final Map<Integer, PackTileCache> tileCaches = new java.util.concurrent.ConcurrentHashMap<>();

    private volatile Integer editingPackId;

    @Inject
    TilePackEditingManager(Client client, Gson gson, TilePackManager tilePackManager,
                           PointManager pointManager, TilePacksConfig config) {
        this.client = client;
        this.gson = gson;
        this.tilePackManager = tilePackManager;
        this.pointManager = pointManager;
        this.config = config;
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

        int regionId = point.getRegionID();
        int regionX = point.getRegionX();
        int regionY = point.getRegionY();
        int plane = point.getPlane();
        TileCoordinate coordinate = new TileCoordinate(regionId, regionX, regionY, plane);

        if (cache.coordinates.remove(coordinate)) {
            cache.points.removeIf(coordinate::matches);
        } else {
            GroundMarkerPoint tile = new GroundMarkerPoint(regionId, regionX, regionY, plane,
                    resolveTileColor(pack), null);
            cache.points.add(tile);
            cache.coordinates.add(coordinate);
        }

        String updatedTiles = gson.toJson(cache.points);
        tilePackManager.updatePackTiles(packId, updatedTiles);
        pointManager.loadPoints();
        return true;
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

    /** Parses a pack's tile JSON once into a retained cache, not once per menu/click event. */
    private PackTileCache getTileCache(Integer packId) {
        TilePack pack = tilePackManager.getTilePacks().get(packId);
        if (pack == null || pack.packTiles == null) {
            return null;
        }

        PackTileCache cache = tileCaches.get(packId);
        if (cache != null) {
            return cache;
        }

        try {
            List<GroundMarkerPoint> points = gson.fromJson(pack.packTiles, TILE_LIST_TYPE);
            if (points == null) {
                return null;
            }
            points.removeIf(Objects::isNull);
            cache = new PackTileCache(points);
            tileCaches.put(packId, cache);
            return cache;
        } catch (RuntimeException e) {
            log.warn("Unable to load tile data for editing pack {}", packId, e);
            return null;
        }
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
