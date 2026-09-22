package com.tilepacks;

import com.google.gson.Gson;
import com.tilepacks.data.GroundMarkerPoint;
import com.tilepacks.data.TilePack;
import net.runelite.api.Client;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PointManagerTest
{
	@Test
	public void getActivePointsCachesPointsByRegion() throws Exception
	{
		TilePackManager tilePackManager = mock(TilePackManager.class);
		TilePack pack = new TilePack(1, "test", "["
				+ "{\"regionId\":1,\"regionX\":2,\"regionY\":3,\"z\":0},"
				+ "{\"regionId\":1,\"regionX\":4,\"regionY\":5,\"z\":0},"
				+ "{\"regionId\":2,\"regionX\":6,\"regionY\":7,\"z\":1}"
				+ "]");
		when(tilePackManager.getEnabledPacks()).thenReturn(Collections.singletonList(1));
		when(tilePackManager.getTilePacks()).thenReturn(Collections.singletonMap(1, pack));

		PointManager pointManager = new PointManager(tilePackManager, new Gson(), null);
		Method getActivePointsForRegion = PointManager.class.getDeclaredMethod("getActivePoints", int.class);
		getActivePointsForRegion.setAccessible(true);
		List<GroundMarkerPoint> first = (List<GroundMarkerPoint>) getActivePointsForRegion.invoke(pointManager, 1);
		List<GroundMarkerPoint> second = (List<GroundMarkerPoint>) getActivePointsForRegion.invoke(pointManager, 1);

		assertEquals(2, first.size());
		assertSame(first, second);
		assertEquals(1, ((List<GroundMarkerPoint>) getActivePointsForRegion.invoke(pointManager, 2)).size());
	}

	@Test
	public void loadPointsInvalidatesActivePointsCache() throws Exception
	{
		TilePackManager tilePackManager = mock(TilePackManager.class);
		TilePack pack = new TilePack(1, "test", "["
				+ "{\"regionId\":1,\"regionX\":2,\"regionY\":3,\"z\":0}"
				+ "]");
		when(tilePackManager.getEnabledPacks()).thenReturn(Collections.singletonList(1));
		when(tilePackManager.getTilePacks()).thenReturn(Collections.singletonMap(1, pack));
		Client client = mock(Client.class);
		when(client.getTopLevelWorldView()).thenReturn(null);

		PointManager pointManager = new PointManager(tilePackManager, new Gson(), client);
		Method getActivePointsForRegion = PointManager.class.getDeclaredMethod("getActivePoints", int.class);
		getActivePointsForRegion.setAccessible(true);

		List<GroundMarkerPoint> first = (List<GroundMarkerPoint>) getActivePointsForRegion.invoke(pointManager, 1);
		pointManager.loadPoints();
		List<GroundMarkerPoint> second = (List<GroundMarkerPoint>) getActivePointsForRegion.invoke(pointManager, 1);

		assertEquals(1, first.size());
		assertEquals(1, second.size());
		assertNotSame(first, second);
	}
}