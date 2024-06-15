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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.tilepacks.data.ColorTileMarker;
import com.tilepacks.data.GroundMarkerPoint;
import com.tilepacks.data.TilePack;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
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

    private List<ColorTileMarker> points = new ArrayList<>();

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

        int[] regions = client.getMapRegions();

        if (regions == null) {
            return;
        }

        for (int regionId : regions) {
            Collection<GroundMarkerPoint> regionPoints = getActivePoints(regionId);
            Collection<ColorTileMarker> colorTileMarkers = translateToColorTileMarker(regionPoints);
            points.addAll(colorTileMarkers);
        }
        log.debug("active points - {}",points);
    }

    //gets all the active points for all enabled packs.
    private List<GroundMarkerPoint> getActivePoints() {
        List<GroundMarkerPoint> markers = new ArrayList<>();
        List<Integer> enabledPacks = tilePackManager.getEnabledPacks();
        for (Map.Entry<Integer, TilePack> pack : tilePackManager.getTilePacks().entrySet()) {
            if (enabledPacks.contains(pack.getKey())) {
                markers.addAll(gson.fromJson(
                        pack.getValue().packTiles,
                        new TypeToken<List<GroundMarkerPoint>>() {
                        }.getType()));
            }
        }
        return markers;
    }

    //gets all the active points, filtered for a region
    private List<GroundMarkerPoint> getActivePoints(int regionId) {
        List<GroundMarkerPoint> activePoints = getActivePoints();
        Map<Integer, List<GroundMarkerPoint>> regionGroupedPoints = activePoints.stream()
                .collect(Collectors.groupingBy(GroundMarkerPoint::getRegionId));
        List<GroundMarkerPoint> regionPoints = regionGroupedPoints.get(regionId);
        if (regionPoints == null) {
            return Collections.emptyList();
        }
        return regionPoints;
    }


    private Collection<ColorTileMarker> translateToColorTileMarker(Collection<GroundMarkerPoint> points) {
        if (points.isEmpty()) {
            return Collections.emptyList();
        }

        return points.stream()
                .map(point -> new ColorTileMarker(
                        WorldPoint.fromRegion(point.getRegionId(), point.getRegionX(), point.getRegionY(), point.getZ()),
                        point.getColor(), point.getLabel()))
                .flatMap(colorTile ->
                {
                    final Collection<WorldPoint> localWorldPoints = WorldPoint.toLocalInstance(client, colorTile.getWorldPoint());
                    return localWorldPoints.stream().map(wp -> new ColorTileMarker(wp, colorTile.getColor(), colorTile.getLabel()));
                })
                .collect(Collectors.toList());
    }
}
