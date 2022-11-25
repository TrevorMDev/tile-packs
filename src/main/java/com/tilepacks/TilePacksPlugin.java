/*
 * Copyright (c) 2018, TheLonelyDev <https://github.com/TheLonelyDev>
 * Copyright (c) 2021, Adam <Adam@sigterm.info>
 * Copyright (c) 2022, Trevor <https://github.com/TrevorMartz>
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

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.groundmarkers.GroundMarkerPlugin;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Tile Packs"
)
@PluginDependency(GroundMarkerPlugin.class)
public class TilePacksPlugin extends Plugin
{
	private static final String CONFIG_GROUP = "tilePacks";
	private static final String REGION_PREFIX = "region_";
	@Inject
	private Client client;
	@Inject
	private TilePacksConfig config;
	@Inject
	private ConfigManager configManager;
	@Inject
	private Gson gson;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private GroundMarkerOverlay overlay;

	@Getter(AccessLevel.PACKAGE)
	private final List<ColorTileMarker> points = new ArrayList<>();
	private TilePacksPanel panel;
	private NavigationButton navButton;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Tile Packs Plugin starting");
		overlayManager.add(overlay);
		panel = new TilePacksPanel(this, gson);
		final BufferedImage icon = ImageUtil.loadImageResource(TilePacksPlugin.class, "tilepacks_icon.png");

		navButton = NavigationButton.builder()
				.tooltip("Tile Packs")
				.icon(icon)
				.priority(4)//how is this determined? Could it be a config?
				.panel(panel)
				.build();


		if(config.hidePlugin()) {
			log.info("tile packs hidden, skipping adding");
			return;
		}

		clientToolbar.addNavigation(navButton);

		loadPoints();
		log.info("Tile Packs Plugin started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Tile Packs Plugin stopping");
		overlayManager.remove(overlay);
		clientToolbar.removeNavigation(navButton);
		log.info("Tile Packs Plugin stopped");
	}

	@Subscribe
	public void onConfigChanged(final ConfigChanged event)
	{
		if (event.getGroup().equals(TilePacksConfig.GROUP))
		{
			switch (event.getKey())
			{
				case "hidePlugin":
					if (config.hidePlugin())
					{
						clientToolbar.removeNavigation(navButton);
					}
					else
					{
						clientToolbar.addNavigation(navButton);
					}
					break;
			}
		}
	}

	Collection<GroundMarkerPoint> getPoints(int regionId)
	{
		String json = configManager.getConfiguration(CONFIG_GROUP, REGION_PREFIX + regionId);

		log.info("json {}", json);
		if (Strings.isNullOrEmpty(json))
		{
			return Collections.emptyList();
		}
		return gson.fromJson(json, new TypeToken<List<GroundMarkerPoint>>(){}.getType());
	}

	void savePoints(int regionId, Collection<GroundMarkerPoint> points)
	{
		if (points == null || points.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, REGION_PREFIX + regionId);
			return;
		}

		String json = gson.toJson(points);
		configManager.setConfiguration(CONFIG_GROUP, REGION_PREFIX + regionId, json);
	}

	void loadPoints()
	{
		points.clear();

		int[] regions = client.getMapRegions();

		if (regions == null)
		{
			return;
		}

		for (int regionId : regions)
		{
			// load points for region
			log.info("Loading points for region {}", regionId);
			Collection<GroundMarkerPoint> regionPoints = getPoints(regionId);
			log.info("regionPoints {}", regionPoints.toString());
			Collection<ColorTileMarker> colorTileMarkers = translateToColorTileMarker(regionPoints);
			log.info("colorTileMarkers {}", colorTileMarkers.toString());
			points.addAll(colorTileMarkers);
		}
	}

	private Collection<ColorTileMarker> translateToColorTileMarker(Collection<GroundMarkerPoint> points)
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
					final Collection<WorldPoint> localWorldPoints = WorldPoint.toLocalInstance(client, colorTile.getWorldPoint());
					return localWorldPoints.stream().map(wp -> new ColorTileMarker(wp, colorTile.getColor(), colorTile.getLabel()));
				})
				.collect(Collectors.toList());
	}

	void importGroundMarkers(List<GroundMarkerPoint> importPoints)
	{
		// regions being imported may not be loaded on client,
		// so need to import each bunch directly into the config
		// first, collate the list of unique region ids in the import
		Map<Integer, List<GroundMarkerPoint>> regionGroupedPoints = importPoints.stream()
				.collect(Collectors.groupingBy(GroundMarkerPoint::getRegionId));

		// now import each region into the config
		regionGroupedPoints.forEach((regionId, groupedPoints) ->
		{
			// combine imported points with existing region points
			log.info("Importing {} points to region {}", groupedPoints.size(), regionId);
			Collection<GroundMarkerPoint> regionPoints = getPoints(regionId);

			List<GroundMarkerPoint> mergedList = new ArrayList<>(regionPoints.size() + groupedPoints.size());
			// add existing points
			mergedList.addAll(regionPoints);

			// add new points
			for (GroundMarkerPoint point : groupedPoints)
			{
				// filter out duplicates
				if (!mergedList.contains(point))
				{
					mergedList.add(point);
				}
			}

			savePoints(regionId, mergedList);
		});

		// reload points from config
		log.info("Reloading points after import");
		loadPoints();
	}

	void removeGroundMarkers(List<GroundMarkerPoint> removePoints)
	{
		// regions being removed may not be loaded on client,
		// so need to import each bunch directly into the config
		// first, collate the list of unique region ids in the import
		Map<Integer, List<GroundMarkerPoint>> regionGroupedPoints = removePoints.stream()
				.collect(Collectors.groupingBy(GroundMarkerPoint::getRegionId));

		// now import each region into the config
		regionGroupedPoints.forEach((regionId, groupedPoints) ->
		{
			// combine imported points with existing region points
			log.info("removing {} points from region {}", groupedPoints.size(), regionId);
			Collection<GroundMarkerPoint> regionPoints = getPoints(regionId);

			// remove specified points
			for (GroundMarkerPoint point : groupedPoints)
			{
				// filter out duplicates
				if (regionPoints.contains(point))
				{
					regionPoints.remove(point);
				}
			}
			log.info("region after removal {}", regionPoints.toString());

			savePoints(regionId, regionPoints);
		});

		// reload points from config
		log.info("Reloading points after removal");
		loadPoints();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		// map region has just been updated
		loadPoints();
	}


	@Provides
	TilePacksConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TilePacksConfig.class);
	}
}
