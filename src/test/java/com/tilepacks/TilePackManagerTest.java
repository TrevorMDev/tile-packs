package com.tilepacks;

import com.tilepacks.data.TilePack;
import net.runelite.client.config.ConfigManager;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
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