package com.tilepacks;

import com.google.common.collect.Multimap;
import com.tilepacks.data.ColorTileMarker;
import com.tilepacks.data.GroundMarkerPoint;
import com.tilepacks.data.TilePack;
import net.runelite.api.Client;
import net.runelite.api.WorldView;
import org.junit.Test;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PointManagerTest extends TilePacksTest
{
	@Test
	public void publishedPointsSnapshotIsImmutable()
	{
		PointManager pointManager = new PointManager(mock(TilePackManager.class), gson, null);
		Multimap<WorldView, ColorTileMarker> snapshot = pointManager.getPoints();

		try
		{
			snapshot.clear();
			fail("Published marker snapshots must be immutable");
		}
		catch (UnsupportedOperationException expected)
		{
			// Expected: overlays must not be able to mutate a published snapshot.
		}
	}

	@Test
	public void getActivePointsCachesPointsByRegion()
	{
		TilePackManager tilePackManager = mock(TilePackManager.class);
		TilePack pack = new TilePack(1, "test", "["
				+ "{\"regionId\":1,\"regionX\":2,\"regionY\":3,\"z\":0},"
				+ "{\"regionId\":1,\"regionX\":4,\"regionY\":5,\"z\":0},"
				+ "{\"regionId\":2,\"regionX\":6,\"regionY\":7,\"z\":1}"
				+ "]");
		when(tilePackManager.getEnabledPacks()).thenReturn(Collections.singletonList(1));
		when(tilePackManager.getTilePacks()).thenReturn(Collections.singletonMap(1, pack));

		PointManager pointManager = new PointManager(tilePackManager, gson, null);
		List<GroundMarkerPoint> first = pointManager.getActivePoints(1);
		List<GroundMarkerPoint> second = pointManager.getActivePoints(1);

		assertEquals(2, first.size());
		assertSame(first, second);
		assertEquals(1, pointManager.getActivePoints(2).size());
	}

	@Test
	public void resolvesTileColorBeforePackDefaultAndYellowFallback()
	{
		TilePackManager tilePackManager = mock(TilePackManager.class);
		TilePack pack = new TilePack(1, "colors", gson.toJson(java.util.Arrays.asList(
				new GroundMarkerPoint(1, 2, 3, 0, null, "pack default"),
				new GroundMarkerPoint(1, 4, 5, 0, Color.BLUE, "tile color"))));
		pack.defaultColor = Color.RED;
		when(tilePackManager.getEnabledPacks()).thenReturn(Collections.singletonList(1));
		when(tilePackManager.getTilePacks()).thenReturn(Collections.singletonMap(1, pack));

		PointManager pointManager = new PointManager(tilePackManager, gson, null);
		List<GroundMarkerPoint> points = pointManager.getActivePoints(1);

		assertEquals(Color.RED, points.get(0).getColor());
		assertEquals(Color.BLUE, points.get(1).getColor());
	}

	@Test
	public void resolvesMissingTileAndPackColorToYellow()
	{
		TilePackManager tilePackManager = mock(TilePackManager.class);
		TilePack pack = new TilePack(1, "legacy", "[{\"regionId\":1,\"regionX\":2,\"regionY\":3,\"z\":0}]");
		when(tilePackManager.getEnabledPacks()).thenReturn(Collections.singletonList(1));
		when(tilePackManager.getTilePacks()).thenReturn(Collections.singletonMap(1, pack));

		PointManager pointManager = new PointManager(tilePackManager, gson, null);

		assertEquals(Color.YELLOW, pointManager.getActivePoints(1).get(0).getColor());
	}

	@Test
	public void emptyPackProducesNoActivePoints()
	{
		TilePackManager tilePackManager = mock(TilePackManager.class);
		TilePack pack = new TilePack(10000, "empty", "[]");
		when(tilePackManager.getEnabledPacks()).thenReturn(Collections.singletonList(10000));
		when(tilePackManager.getTilePacks()).thenReturn(Collections.singletonMap(10000, pack));

		PointManager pointManager = new PointManager(tilePackManager, gson, null);

		assertEquals(Collections.emptyList(), pointManager.getActivePoints(1));
	}

	@Test
	public void skipsNullEntriesAndMalformedPackJsonWhenBuildingActivePoints()
	{
		TilePackManager tilePackManager = mock(TilePackManager.class);
		TilePack validPack = new TilePack(1, "valid", gson.toJson(java.util.Arrays.asList(
				null,
				new GroundMarkerPoint(1, 2, 3, 0, Color.GREEN, null))));
		TilePack malformedPack = new TilePack(2, "malformed", "not-json");
		when(tilePackManager.getEnabledPacks()).thenReturn(java.util.Arrays.asList(1, 2));
		when(tilePackManager.getTilePacks()).thenReturn(java.util.Map.of(1, validPack, 2, malformedPack));

		PointManager pointManager = new PointManager(tilePackManager, gson, null);

		assertEquals(1, pointManager.getActivePoints(1).size());
		assertEquals(Color.GREEN, pointManager.getActivePoints(1).get(0).getColor());
	}

	@Test
	public void loadPointsInvalidatesActivePointsCache()
	{
		TilePackManager tilePackManager = mock(TilePackManager.class);
		TilePack pack = new TilePack(1, "test", "["
				+ "{\"regionId\":1,\"regionX\":2,\"regionY\":3,\"z\":0}"
				+ "]");
		when(tilePackManager.getEnabledPacks()).thenReturn(Collections.singletonList(1));
		when(tilePackManager.getTilePacks()).thenReturn(Collections.singletonMap(1, pack));
		Client client = mock(Client.class);
		when(client.getTopLevelWorldView()).thenReturn(null);

		PointManager pointManager = new PointManager(tilePackManager, gson, client);
		List<GroundMarkerPoint> first = pointManager.getActivePoints(1);
		pointManager.loadPoints();
		List<GroundMarkerPoint> second = pointManager.getActivePoints(1);

		assertEquals(1, first.size());
		assertEquals(1, second.size());
		assertNotSame(first, second);
	}
}