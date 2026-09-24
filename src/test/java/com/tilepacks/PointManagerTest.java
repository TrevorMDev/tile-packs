package com.tilepacks;

import com.tilepacks.data.GroundMarkerPoint;
import com.tilepacks.data.TilePack;
import net.runelite.api.Client;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PointManagerTest extends TilePacksTest
{
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