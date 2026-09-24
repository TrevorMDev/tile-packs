/*
 * Copyright (c) 2024, Trevor <https://github.com/TrevorMDev>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tilepacks;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is responsible for translating the tile packs into drawable points
 */
@Slf4j
@Value
public class PointManager {

    @Getter
    private ListMultimap<WorldView, ColorTileMarker> points = ArrayListMultimap.create();

    @NonFinal
    private Map<Integer, List<GroundMarkerPoint>> activePointsByRegion;

    private final TilePackManager tilePackManager;
    private final Gson gson;
    private final Client client;

    @Inject
    PointManager(TilePackManager tilePackManager, Gson gson, Client client) {
        this.tilePackManager = tilePackManager;
        this.gson = gson;
        this.client = client;
    }

    //loads the points from the packs for the players active regions
    public void loadPoints() {
        points.clear();
        activePointsByRegion = null;

        WorldView worldView = client.getTopLevelWorldView();
        if (worldView == null)
        {
            return;
        }

        loadWorldViewPoints(worldView);

        for (WorldEntity we : worldView.worldEntities())
        {
            loadWorldViewPoints(we.getWorldView());
        }
    }

    public void loadWorldViewPoints(WorldView worldView)
    {
        points.removeAll(worldView);

        int[] regions = worldView.getMapRegions();
        if (regions == null)
        {
            return;
        }

        for (int regionId : regions)
        {
            Collection<GroundMarkerPoint> regionPoints = getActivePoints(regionId);
            Collection<ColorTileMarker> colorTileMarkers = translateToColorTileMarker(worldView, regionPoints);
            points.putAll(worldView, colorTileMarkers);
        }
    }

    //gets the active points for a region, loading and indexing all enabled packs once.
    List<GroundMarkerPoint> getActivePoints(int regionId) {
        if (activePointsByRegion == null)
        {
            activePointsByRegion = new HashMap<>();
            Set<Integer> enabledPacks = new HashSet<>(tilePackManager.getEnabledPacks());
            for (Map.Entry<Integer, TilePack> pack : tilePackManager.getTilePacks().entrySet()) {
                if (!enabledPacks.contains(pack.getKey())) {
                    continue;
                }

                List<GroundMarkerPoint> markers = gson.fromJson(
                        pack.getValue().packTiles,
                        new TypeToken<List<GroundMarkerPoint>>() {
                        }.getType());
                for (GroundMarkerPoint marker : markers)
                {
                    activePointsByRegion
                            .computeIfAbsent(marker.getRegionId(), key -> new ArrayList<>())
                            .add(marker);
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
