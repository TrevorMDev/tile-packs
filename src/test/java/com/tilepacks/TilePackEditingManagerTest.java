package com.tilepacks;

import com.tilepacks.data.GroundMarkerPoint;
import com.tilepacks.data.TilePack;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import org.junit.Test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TilePackEditingManagerTest extends TilePacksTest {
    @Test
    public void menuEntryGateRequiresOpenEditorShiftAndWalkAction() {
        assertTrue(TilePackEditingManager.shouldHandleMenuEntry(10000, true, MenuAction.WALK));
        assertTrue(TilePackEditingManager.shouldHandleMenuEntry(1, true, MenuAction.SET_HEADING));
        assertFalse(TilePackEditingManager.shouldHandleMenuEntry(null, true, MenuAction.WALK));
        assertFalse(TilePackEditingManager.shouldHandleMenuEntry(10000, false, MenuAction.WALK));
        assertFalse(TilePackEditingManager.shouldHandleMenuEntry(10000, true, MenuAction.RUNELITE));
    }

    @Test
    public void editCacheAddsAndRemovesTilesWithSynchronousPackSaves() {
        ConfigManager configManager = mock(ConfigManager.class);
        Map<String, String> values = new HashMap<>();
        stubConfiguration(configManager, values);
        TilePackManager tilePackManager = new TilePackManager(gson, configManager);
        tilePackManager.addCustomPack("Tiles", "[]");
        tilePackManager.updatePackDefaultColor(10000, Color.BLUE);
        PointManager pointManager = mock(PointManager.class);
        TilePackEditingManager editingManager = new TilePackEditingManager(
                mock(Client.class), gson, tilePackManager, pointManager, new TestConfig(Color.YELLOW));
        editingManager.editPack(10000);

        WorldPoint point = new WorldPoint(3200, 3229, 2);
        assertTrue(editingManager.toggleTile(10000, point));
        TilePack afterAdd = gson.fromJson(values.get("pack_10000"), TilePack.class);
        List<GroundMarkerPoint> added = parsePoints(afterAdd.packTiles);
        assertEquals(1, added.size());
        assertEquals(12850, added.get(0).getRegionId());
        assertEquals(0, added.get(0).getRegionX());
        assertEquals(29, added.get(0).getRegionY());
        assertEquals(2, added.get(0).getZ());
        assertEquals(Color.BLUE, added.get(0).getColor());
        assertNull(added.get(0).getLabel());
        assertEquals(gson.toJson(added), gson.fromJson(values.get("pack_10000"), TilePack.class).packTiles);

        assertTrue(editingManager.toggleTile(10000, point));
        TilePack afterRemove = gson.fromJson(values.get("pack_10000"), TilePack.class);
        assertEquals("[]", afterRemove.packTiles);
        verify(pointManager, times(2)).loadPoints();
    }

    @Test
    public void removalUsesCoordinateIndexAndRemovesDuplicateRecords() {
        ConfigManager configManager = mock(ConfigManager.class);
        Map<String, String> values = new HashMap<>();
        List<GroundMarkerPoint> duplicatePoints = new ArrayList<>();
        duplicatePoints.add(new GroundMarkerPoint(12850, 0, 29, 2, Color.RED, null));
        duplicatePoints.add(new GroundMarkerPoint(12850, 0, 29, 2, Color.GREEN, "duplicate"));
        values.put("pack_10000", gson.toJson(new TilePack(10000, "Duplicate", gson.toJson(duplicatePoints))));
        stubConfiguration(configManager, values);
        TilePackManager tilePackManager = new TilePackManager(gson, configManager);
        TilePackEditingManager editingManager = new TilePackEditingManager(
                mock(Client.class), gson, tilePackManager, mock(PointManager.class), new TestConfig(Color.YELLOW));
        editingManager.editPack(10000);

        assertTrue(editingManager.toggleTile(10000, new WorldPoint(3200, 3229, 2)));
        assertEquals("[]", gson.fromJson(values.get("pack_10000"), TilePack.class).packTiles);
    }

    @Test
    public void tileEditRequiresTheOpenEditorTarget() {
        ConfigManager configManager = mock(ConfigManager.class);
        Map<String, String> values = new HashMap<>();
        stubConfiguration(configManager, values);
        TilePackManager tilePackManager = new TilePackManager(gson, configManager);
        tilePackManager.addCustomPack("Tiles", "[]");
        TilePackEditingManager editingManager = new TilePackEditingManager(
                mock(Client.class), gson, tilePackManager, mock(PointManager.class), new TestConfig(Color.YELLOW));
        WorldPoint point = new WorldPoint(3200, 3229, 2);

        assertFalse(editingManager.toggleTile(10000, point));
        editingManager.editPack(10000);
        editingManager.stopEditing();
        assertFalse(editingManager.toggleTile(10000, point));
        assertEquals("[]", gson.fromJson(values.get("pack_10000"), TilePack.class).packTiles);
    }

    private List<GroundMarkerPoint> parsePoints(String json) {
        return gson.fromJson(json, new com.google.common.reflect.TypeToken<List<GroundMarkerPoint>>() { }.getType());
    }

    private void stubConfiguration(ConfigManager configManager, Map<String, String> values) {
        when(configManager.getConfiguration(anyString(), anyString()))
                .thenAnswer(invocation -> values.get(invocation.getArgument(1)));
        when(configManager.getConfigurationKeys(anyString()))
                .thenAnswer(invocation -> {
                    List<String> keys = new ArrayList<>();
                    for (String key : values.keySet()) {
                        if (key.startsWith("pack_")) {
                            keys.add("tilePacks." + key);
                        }
                    }
                    return keys;
                });
        doAnswer(invocation -> {
            String key = invocation.getArgument(1, String.class);
            values.put(key, invocation.getArgument(2, String.class));
            return null;
        }).when(configManager).setConfiguration(anyString(), anyString(), anyString());
        doAnswer(invocation -> {
            String key = invocation.getArgument(1, String.class);
            values.put(key, String.valueOf(invocation.getArgument(2, Integer.class)));
            return null;
        }).when(configManager).setConfiguration(anyString(), anyString(), anyInt());
        doAnswer(invocation -> {
            values.remove(invocation.getArgument(1, String.class));
            return null;
        }).when(configManager).unsetConfiguration(anyString(), anyString());
    }

    private static class TestConfig implements TilePacksConfig {
        private final Color overrideColor;

        private TestConfig(Color overrideColor) {
            this.overrideColor = overrideColor;
        }

        @Override
        public Color overrideColor() {
            return overrideColor;
        }

        @Override
        public boolean overrideColorActive() {
            return false;
        }
    }
}
