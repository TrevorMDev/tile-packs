package com.tilepacks;

import com.tilepacks.data.GroundMarkerPoint;
import com.tilepacks.data.TilePack;
import net.runelite.client.config.ConfigManager;
import org.junit.Test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TilePackManagerTest extends TilePacksTest {

    @Test
    public void customPackSaveUpdateAndDeleteUseIndividualKey() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        stubConfiguration(configManager, values);
        TilePackManager manager = new TilePackManager(gson, configManager);

        manager.addCustomPack("Custom", "[]");

        TilePack savedPack = gson.fromJson(values.get("pack_10000"), TilePack.class);
        assertEquals("Custom", savedPack.packName);
        assertTrue(savedPack.visible);
        assertEquals("10000", values.get("customId"));

        savedPack.visible = false;
        manager.updatePack(savedPack);
        assertFalse(gson.fromJson(values.get("pack_10000"), TilePack.class).visible);

        manager.removeCustomPack(10000);
        assertNull(values.get("pack_10000"));
    }

    @Test
    public void builtInVisibilitySavesWithoutReplacingResourcePack() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        stubConfiguration(configManager, values);
        TilePackManager manager = new TilePackManager(gson, configManager);

        TilePack resourcePack = manager.getTilePacks().get(1);
        String originalTiles = resourcePack.packTiles;
        String originalName = resourcePack.packName;
        String originalLink = resourcePack.link;
        resourcePack.visible = false;
        manager.updatePack(resourcePack);

        TilePack savedConfig = gson.fromJson(values.get("pack_1"), TilePack.class);
        assertEquals(Integer.valueOf(1), savedConfig.id);
        assertFalse(savedConfig.visible);
        assertNull(savedConfig.packName);
        assertNull(savedConfig.link);
        assertNull(savedConfig.packTiles);
        assertEquals(originalTiles, manager.getTilePacks().get(1).packTiles);

        TilePack preparedPack = manager.preparePackForEditing(1);
        TilePack fullOverride = gson.fromJson(values.get("pack_1"), TilePack.class);
        assertEquals(Integer.valueOf(1), fullOverride.id);
        assertEquals(originalName, fullOverride.packName);
        assertEquals(originalLink, fullOverride.link);
        assertEquals(originalTiles, fullOverride.packTiles);
        assertFalse(fullOverride.visible);
        assertEquals(originalTiles, preparedPack.packTiles);
        assertTrue(manager.getFullOverridePackIds().contains(1));
    }

    @Test
    public void prepareBundledPackCreatesSameIdFullCloneWithoutChangingEnabledPacks() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        values.put("packs", "[1,3]");
        stubConfiguration(configManager, values);
        TilePackManager manager = new TilePackManager(gson, configManager);

        TilePack bundledPack = manager.getTilePacks().get(1);
        TilePack preparedPack = manager.preparePackForEditing(1);
        TilePack savedOverride = gson.fromJson(values.get("pack_1"), TilePack.class);

        assertNotSame(bundledPack, preparedPack);
        assertEquals(bundledPack.id, preparedPack.id);
        assertEquals(bundledPack.packName, preparedPack.packName);
        assertEquals(bundledPack.link, preparedPack.link);
        assertEquals(bundledPack.packTiles, preparedPack.packTiles);
        assertSame(bundledPack.parsedPackTiles, preparedPack.parsedPackTiles);
        assertEquals(bundledPack.visible, preparedPack.visible);
        assertEquals(bundledPack.id, savedOverride.id);
        assertEquals(bundledPack.packName, savedOverride.packName);
        assertEquals(bundledPack.link, savedOverride.link);
        assertEquals(bundledPack.packTiles, savedOverride.packTiles);
        assertEquals(bundledPack.visible, savedOverride.visible);
        assertEquals("[1,3]", values.get("packs"));
        assertEquals(java.util.Arrays.asList(1, 3), manager.getEnabledPacks());
        assertTrue(manager.getFullOverridePackIds().contains(1));
    }

    @Test
    public void titleUpdateIsIdempotentAndReloadsAsFullOverride() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        values.put("packs", "[1,3]");
        stubConfiguration(configManager, values);
        TilePackManager manager = new TilePackManager(gson, configManager);
        TilePack bundledPack = manager.getTilePacks().get(1);

        manager.updatePackTitle(1, "Renamed pack");
        TilePack titledPack = manager.getTilePacks().get(1);
        assertSame(titledPack, manager.preparePackForEditing(1));
        assertEquals("Renamed pack", titledPack.packName);
        assertEquals(bundledPack.link, titledPack.link);
        assertEquals(bundledPack.packTiles, titledPack.packTiles);
        assertEquals("[1,3]", values.get("packs"));

        TilePackManager reloadedManager = new TilePackManager(gson, configManager);
        TilePack reloadedPack = reloadedManager.getTilePacks().get(1);
        assertEquals("Renamed pack", reloadedPack.packName);
        assertEquals(bundledPack.link, reloadedPack.link);
        assertEquals(bundledPack.packTiles, reloadedPack.packTiles);
        assertTrue(reloadedManager.getFullOverridePackIds().contains(1));

        reloadedPack.visible = false;
        reloadedManager.updatePack(reloadedPack);
        TilePack savedAfterUpdate = gson.fromJson(values.get("pack_1"), TilePack.class);
        assertEquals("Renamed pack", savedAfterUpdate.packName);
        assertEquals(bundledPack.link, savedAfterUpdate.link);
        assertEquals(bundledPack.packTiles, savedAfterUpdate.packTiles);
        assertFalse(savedAfterUpdate.visible);
        assertEquals("[1,3]", values.get("packs"));
    }

    @Test
    public void preparingCustomPackDoesNotAllocateIdOrLoseItsData() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        values.put("customId", "10005");
        values.put("packs", "[10005]");
        values.put("pack_10005", "{\"id\":10005,\"packName\":\"Custom\",\"link\":\"https://example.com\",\"packTiles\":\"[]\",\"visible\":false}");
        stubConfiguration(configManager, values);
        TilePackManager manager = new TilePackManager(gson, configManager);
        TilePack customPack = manager.getTilePacks().get(10005);

        assertSame(customPack, manager.preparePackForEditing(10005));
        manager.updatePackTitle(10005, "Renamed custom");

        TilePack savedPack = gson.fromJson(values.get("pack_10005"), TilePack.class);
        assertEquals(Integer.valueOf(10005), savedPack.id);
        assertEquals("Renamed custom", savedPack.packName);
        assertEquals("https://example.com", savedPack.link);
        assertEquals("[]", savedPack.packTiles);
        assertFalse(savedPack.visible);
        assertEquals("10005", values.get("customId"));
        assertEquals("[10005]", values.get("packs"));
        assertEquals(1, manager.getTilePacks().keySet().stream().filter(id -> id >= 10000).count());
    }

    @Test
    public void loadsSavedCustomPackFromIndividualKey() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        values.put("pack_10000", "{\"id\":10000,\"packName\":\"Saved\",\"packTiles\":\"[]\",\"visible\":false}");
        stubConfiguration(configManager, values);
        when(configManager.getConfigurationKeys(anyString()))
                .thenReturn(Collections.singletonList("tilePacks.pack_10000"));

        TilePack pack = new TilePackManager(gson, configManager).getTilePacks().get(10000);

        assertNotNull(pack);
        assertEquals("Saved", pack.packName);
        assertFalse(pack.visible);
    }

    @Test
    public void defaultColorIsSavedReloadedAndOldRecordsRemainNull() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        values.put("pack_10005", "{\"id\":10005,\"packName\":\"Legacy\",\"packTiles\":\"[]\"}");
        stubConfiguration(configManager, values);
        TilePackManager manager = new TilePackManager(gson, configManager);

        assertNull(manager.getTilePacks().get(10005).defaultColor);
        manager.updatePackDefaultColor(10005, new Color(12, 34, 56, 78));

        TilePack saved = gson.fromJson(values.get("pack_10005"), TilePack.class);
        assertEquals(new Color(12, 34, 56, 78), saved.defaultColor);
        TilePack reloaded = new TilePackManager(gson, configManager).getTilePacks().get(10005);
        assertEquals(saved.defaultColor, reloaded.defaultColor);
    }

    @Test
    public void defaultColorUpdateDoesNotParseOrRewriteTileJson() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        String oldTileJson = "not-parsed-by-color-setting";
        values.put("pack_10005", gson.toJson(new TilePack(10005, "Legacy", oldTileJson)));
        stubConfiguration(configManager, values);
        TilePackManager manager = new TilePackManager(gson, configManager);

        manager.updatePackDefaultColor(10005, Color.CYAN);

        TilePack saved = gson.fromJson(values.get("pack_10005"), TilePack.class);
        assertEquals(Color.CYAN, saved.defaultColor);
        assertEquals(oldTileJson, saved.packTiles);
    }

    @Test
    public void bundledDefaultColorUpdateCreatesFullSameIdOverrideAndFullUpdatesRetainIt() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        stubConfiguration(configManager, values);
        TilePackManager manager = new TilePackManager(gson, configManager);
        TilePack bundled = manager.getTilePacks().get(1);
        Color color = new Color(23, 45, 67, 89);

        manager.updatePackDefaultColor(1, color);

        TilePack override = gson.fromJson(values.get("pack_1"), TilePack.class);
        assertEquals(Integer.valueOf(1), override.id);
        assertEquals(bundled.packName, override.packName);
        assertEquals(bundled.link, override.link);
        assertEquals(bundled.packTiles, override.packTiles);
        assertEquals(color, override.defaultColor);
        override.visible = false;
        manager.updatePack(override);
        assertEquals(color, gson.fromJson(values.get("pack_1"), TilePack.class).defaultColor);

        TilePack reloaded = new TilePackManager(gson, configManager).getTilePacks().get(1);
        assertEquals(color, reloaded.defaultColor);
        assertEquals(bundled.packTiles, reloaded.packTiles);
        assertTrue(manager.getFullOverridePackIds().contains(1));
    }

    @Test
    public void defaultColorAloneIdentifiesSavedBundledFullRecord() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        TilePack savedRecord = new TilePack();
        savedRecord.id = 1;
        savedRecord.visible = false;
        savedRecord.defaultColor = Color.GREEN;
        values.put("pack_1", gson.toJson(savedRecord));
        stubConfiguration(configManager, values);

        TilePackManager manager = new TilePackManager(gson, configManager);
        TilePack loadedPack = manager.getTilePacks().get(1);

        assertNotNull(loadedPack.defaultColor);
        assertFalse(loadedPack.visible);
        assertTrue(manager.getFullOverridePackIds().contains(1));
    }

    @Test
    public void creatingEmptyPackStoresEmptyArrayAndEnablesIt() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        stubConfiguration(configManager, values);
        TilePackManager manager = new TilePackManager(gson, configManager);

        manager.addCustomPack("Empty", "[]");

        TilePack saved = gson.fromJson(values.get("pack_10000"), TilePack.class);
        assertEquals("[]", saved.packTiles);
        assertEquals(java.util.Collections.singletonList(10000), manager.getEnabledPacks());
        assertTrue(manager.isPackEnabled(10000));
        assertEquals("10000", values.get("customId"));
    }

    @Test
    public void bundledPacksAreLoadedWithParsedTiles() {
        ConfigManager configManager = mockConfigManager();
        stubConfiguration(configManager, new HashMap<>());
        TilePackManager manager = new TilePackManager(gson, configManager);

        for (TilePack pack : manager.getTilePacks().values()) {
            assertNotNull("Pack " + pack.id + " has unparsed tiles", pack.parsedPackTiles);
        }
        TilePack abyssalSire = manager.getTilePacks().get(1);
        assertEquals(24, abyssalSire.parsedPackTiles.size());
        assertEquals(11850, abyssalSire.parsedPackTiles.get(0).getRegionId());
    }

    @Test
    public void parseTilesDropsNullEntriesAndRejectsUnreadableJson() {
        ConfigManager configManager = mockConfigManager();
        stubConfiguration(configManager, new HashMap<>());
        TilePackManager manager = new TilePackManager(gson, configManager);

        List<GroundMarkerPoint> parsed = manager.parseTiles(
                "[null,{\"regionId\":1,\"regionX\":2,\"regionY\":3,\"z\":0}]");

        assertEquals(1, parsed.size());
        assertEquals(2, parsed.get(0).getRegionX());
        assertEquals(Collections.emptyList(), manager.parseTiles("[]"));
        assertNull(manager.parseTiles("not json"));
        assertNull(manager.parseTiles("null"));
        assertNull(manager.parseTiles(null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void parsedTilesCannotBeModified() {
        ConfigManager configManager = mockConfigManager();
        stubConfiguration(configManager, new HashMap<>());
        TilePackManager manager = new TilePackManager(gson, configManager);

        manager.getTilePacks().get(1).parsedPackTiles.clear();
    }

    @Test
    public void customPackIsCreatedWithParsedTiles() {
        ConfigManager configManager = mockConfigManager();
        stubConfiguration(configManager, new HashMap<>());
        TilePackManager manager = new TilePackManager(gson, configManager);
        String tilesJson = gson.toJson(Arrays.asList(point(0, "first"), point(1, null)));

        Integer packId = manager.addCustomPack("Custom", tilesJson);

        TilePack pack = manager.getTilePacks().get(packId);
        assertEquals(tilesJson, pack.packTiles);
        assertEquals(2, pack.parsedPackTiles.size());
        assertEquals("first", pack.parsedPackTiles.get(0).getLabel());
        assertEquals(Color.RED, pack.parsedPackTiles.get(0).getColor());
    }

    @Test
    public void savedPackIsLoadedWithParsedTiles() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        values.put("pack_10000", gson.toJson(new TilePack(10000, "Saved",
                gson.toJson(Arrays.asList(point(0, "first"), point(1, null), point(2, null))))));
        stubConfiguration(configManager, values);

        TilePack pack = new TilePackManager(gson, configManager).getTilePacks().get(10000);

        assertEquals(3, pack.parsedPackTiles.size());
        assertEquals("first", pack.parsedPackTiles.get(0).getLabel());
    }

    @Test
    public void unreadableSavedTilesLeaveParsedTilesNull() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        values.put("pack_10005", gson.toJson(new TilePack(10005, "Broken", "not json")));
        stubConfiguration(configManager, values);

        TilePack pack = new TilePackManager(gson, configManager).getTilePacks().get(10005);

        assertEquals("not json", pack.packTiles);
        assertNull(pack.parsedPackTiles);
    }

    @Test
    public void parsedTilesAreNotSerializedIntoTheSavedPack() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        stubConfiguration(configManager, values);
        TilePackManager manager = new TilePackManager(gson, configManager);

        manager.addCustomPack("Custom", gson.toJson(Arrays.asList(point(0, null))));
        assertFalse(values.get("pack_10000").contains("parsedPackTiles"));

        manager.updatePackTiles(10000, Arrays.asList(point(1, null)));
        assertFalse(values.get("pack_10000").contains("parsedPackTiles"));
    }

    @Test
    public void updatePackTilesKeepsPackStringAndParsedListInSync() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        stubConfiguration(configManager, values);
        TilePackManager manager = new TilePackManager(gson, configManager);
        manager.addCustomPack("Tiles", "[]");
        List<GroundMarkerPoint> points = new ArrayList<>(Arrays.asList(point(0, "first"), point(1, null)));

        manager.updatePackTiles(10000, points);
        points.add(point(2, null));

        TilePack pack = manager.getTilePacks().get(10000);
        assertEquals(2, pack.parsedPackTiles.size());
        assertEquals("first", pack.parsedPackTiles.get(0).getLabel());
        TilePack saved = gson.fromJson(values.get("pack_10000"), TilePack.class);
        assertEquals(pack.packTiles, saved.packTiles);
        assertEquals(pack.parsedPackTiles, manager.parseTiles(saved.packTiles));
        TilePack reloaded = new TilePackManager(gson, configManager).getTilePacks().get(10000);
        assertEquals(pack.parsedPackTiles, reloaded.parsedPackTiles);
        assertEquals("first", reloaded.parsedPackTiles.get(0).getLabel());
    }

    @Test
    public void updatePackTilesOnBundledPackCreatesOverrideWithParsedTiles() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        stubConfiguration(configManager, values);
        TilePackManager manager = new TilePackManager(gson, configManager);
        TilePack bundled = manager.getTilePacks().get(1);
        String bundledName = bundled.packName;

        manager.updatePackTiles(1, Arrays.asList(point(5, "only")));

        TilePack updated = manager.getTilePacks().get(1);
        assertEquals(1, updated.parsedPackTiles.size());
        assertEquals("only", updated.parsedPackTiles.get(0).getLabel());
        TilePack saved = gson.fromJson(values.get("pack_1"), TilePack.class);
        assertEquals(bundledName, saved.packName);
        assertEquals(updated.packTiles, saved.packTiles);
        assertTrue(manager.getFullOverridePackIds().contains(1));
        TilePack reloaded = new TilePackManager(gson, configManager).getTilePacks().get(1);
        assertEquals(updated.parsedPackTiles, reloaded.parsedPackTiles);
    }

    private static GroundMarkerPoint point(int regionX, String label) {
        return new GroundMarkerPoint(12850, regionX, 29, 2, Color.RED, label);
    }

    private ConfigManager mockConfigManager() {
        return mock(ConfigManager.class);
    }

    private void stubConfiguration(ConfigManager configManager, Map<String, String> values) {
        when(configManager.getConfiguration(anyString(), anyString()))
                .thenAnswer(invocation -> values.get(invocation.getArgument(1)));
        when(configManager.getConfigurationKeys(anyString()))
                .thenAnswer(invocation -> {
                    java.util.List<String> keys = new java.util.ArrayList<>();
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
}