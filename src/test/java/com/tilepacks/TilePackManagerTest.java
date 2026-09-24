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
import static org.junit.Assert.assertNull;
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
        resourcePack.visible = false;
        manager.updatePack(resourcePack);

        TilePack savedConfig = gson.fromJson(values.get("pack_1"), TilePack.class);
        assertEquals(Integer.valueOf(1), savedConfig.id);
        assertFalse(savedConfig.visible);
        assertNull(savedConfig.packTiles);
        assertEquals(originalTiles, manager.getTilePacks().get(1).packTiles);
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
                .thenReturn(Collections.emptyList());
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