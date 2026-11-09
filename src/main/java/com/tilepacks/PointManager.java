package com.tilepacks;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.tilepacks.data.ColorTileMarker;
import com.tilepacks.data.GroundMarkerPoint;
import com.tilepacks.data.TilePack;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.WorldEntity;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;

import javax.inject.Inject;
import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is responsible for translating the tile packs into drawable points
 */
@Slf4j
@Value
public class PointManager {

    @Getter
    @NonFinal
    private volatile ImmutableListMultimap<WorldView, ColorTileMarker> points = ImmutableListMultimap.of();

    @NonFinal
    private Map<Integer, List<GroundMarkerPoint>> activePointsByRegion;

    private final TilePackManager tilePackManager;
    private final Client client;

    @Inject
    PointManager(TilePackManager tilePackManager, Client client) {
        this.tilePackManager = tilePackManager;
        this.client = client;
    }

    //loads the points from the packs for the players active regions
    public synchronized void loadPoints() {
        activePointsByRegion = null;
        ListMultimap<WorldView, ColorTileMarker> updatedPoints = ArrayListMultimap.create();

        WorldView worldView = client.getTopLevelWorldView();
        if (worldView == null)
        {
            points = ImmutableListMultimap.copyOf(updatedPoints);
            return;
        }

        addWorldViewPoints(updatedPoints, worldView);

        for (WorldEntity we : worldView.worldEntities())
        {
            addWorldViewPoints(updatedPoints, we.getWorldView());
        }

        points = ImmutableListMultimap.copyOf(updatedPoints);
    }

    public synchronized void loadWorldViewPoints(WorldView worldView)
    {
        ListMultimap<WorldView, ColorTileMarker> updatedPoints = ArrayListMultimap.create(points);
        updatedPoints.removeAll(worldView);
        addWorldViewPoints(updatedPoints, worldView);
        points = ImmutableListMultimap.copyOf(updatedPoints);
    }

    private void addWorldViewPoints(ListMultimap<WorldView, ColorTileMarker> updatedPoints, WorldView worldView)
    {
        int[] regions = worldView.getMapRegions();
        if (regions == null)
        {
            return;
        }

        for (int regionId : regions)
        {
            Collection<GroundMarkerPoint> regionPoints = getActivePoints(regionId);
            Collection<ColorTileMarker> colorTileMarkers = translateToColorTileMarker(worldView, regionPoints);
            updatedPoints.putAll(worldView, colorTileMarkers);
        }
    }

    //gets the active points for a region, loading and indexing all enabled packs once.
    synchronized List<GroundMarkerPoint> getActivePoints(int regionId) {
        if (activePointsByRegion == null)
        {
            activePointsByRegion = new HashMap<>();
            Set<Integer> enabledPacks = new HashSet<>(tilePackManager.getEnabledPacks());
            for (Map.Entry<Integer, TilePack> pack : tilePackManager.getTilePacks().entrySet()) {
                if (!enabledPacks.contains(pack.getKey())) {
                    continue;
                }

                List<GroundMarkerPoint> markers = pack.getValue().parsedPackTiles;
                if (markers == null) {
                    log.warn("Skipping tile pack {} because its tile data is invalid", pack.getKey());
                    continue;
                }
                for (GroundMarkerPoint marker : markers)
                {
                    Color markerColor = marker.getColor();
                    if (markerColor == null) {
                        markerColor = pack.getValue().defaultColor == null
                                ? Color.YELLOW
                                : pack.getValue().defaultColor;
                    }
                    GroundMarkerPoint resolvedMarker = new GroundMarkerPoint(
                            marker.getRegionId(), marker.getRegionX(), marker.getRegionY(), marker.getZ(),
                            markerColor, marker.getLabel());
                    activePointsByRegion
                            .computeIfAbsent(resolvedMarker.getRegionId(), key -> new ArrayList<>())
                            .add(resolvedMarker);
                }
            }

        }

        List<GroundMarkerPoint> regionPoints = activePointsByRegion.get(regionId);
        if (regionPoints == null) {
            return Collections.emptyList();
        }
        return regionPoints;
    }

    private Collection<ColorTileMarker> translateToColorTileMarker(WorldView wv, Collection<GroundMarkerPoint> points)
    {
        if (points.isEmpty())
        {
            return Collections.emptyList();
        }

        return points.stream()
                .map(point -> new ColorTileMarker(
                        WorldPoint.fromRegion(point.getRegionId(), point.getRegionX(), point.getRegionY(), point.getZ()),
                        point.getColor(), point.getLabel()))
                .flatMap(colorTile ->
                {
                    final Collection<WorldPoint> localWorldPoints = WorldPoint.toLocalInstance(wv, colorTile.getWorldPoint());
                    return localWorldPoints.stream().map(wp -> new ColorTileMarker(wp, colorTile.getColor(), colorTile.getLabel()));
                })
                .collect(Collectors.toList());
    }
}