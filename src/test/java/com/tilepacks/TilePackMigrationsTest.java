package com.tilepacks;

import com.tilepacks.data.TilePack;
import net.runelite.client.config.ConfigManager;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TilePackMigrationsTest extends TilePacksTest {

    @Test
    public void migratesCustomPacksAndBuiltInConfigs() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        values.put("customPacks", "{\"10000\":{\"id\":10000,\"packName\":\"Custom\",\"packTiles\":\"[]\"}}");
        values.put("tilePackConfigs", "{\"10000\":{\"packId\":10000,\"visible\":false},\"42\":{\"packId\":42,\"visible\":false}}");
        stubConfiguration(configManager, values);

        new TilePackMigrations(gson, configManager).migrateToLatest();

        TilePack customPack = gson.fromJson(values.get("pack_10000"), TilePack.class);
        TilePack builtInConfig = gson.fromJson(values.get("pack_42"), TilePack.class);
        assertEquals("Custom", customPack.packName);
        assertFalse(customPack.visible);
        assertEquals(Integer.valueOf(42), builtInConfig.id);
        assertFalse(builtInConfig.visible);
        assertNull(values.get("customPacks"));
        assertNull(values.get("tilePackConfigs"));
        assertEquals("2", values.get("tilePacksVersion"));
    }

    @Test
    public void missingLegacyVisibilityDefaultsToVisible() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        values.put("customPacks", "{\"10000\":{\"id\":10000,\"packName\":\"Custom\",\"packTiles\":\"[]\"}}");
        stubConfiguration(configManager, values);

        new TilePackMigrations(gson, configManager).migrateToLatest();

        TilePack pack = gson.fromJson(values.get("pack_10000"), TilePack.class);
        assertTrue(pack.visible);
    }

    @Test
    public void alreadyMigratedConfigurationIsNotChanged() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        values.put("tilePacksVersion", "2");
        values.put("pack_10000", "{\"id\":10000,\"packName\":\"Custom\",\"packTiles\":\"[]\",\"visible\":true}");
        stubConfiguration(configManager, values);

        new TilePackMigrations(gson, configManager).migrateToLatest();

        assertEquals("Custom", gson.fromJson(values.get("pack_10000"), TilePack.class).packName);
        assertEquals("2", values.get("tilePacksVersion"));
        verify(configManager, never()).setConfiguration(anyString(), anyString(), any());
    }

    @Test(expected = RuntimeException.class)
    public void malformedLegacyConfigurationKeepsOldValues() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        values.put("customPacks", "not json");
        stubConfiguration(configManager, values);

        try {
            new TilePackMigrations(gson, configManager).migrateToLatest();
        } finally {
            assertEquals("not json", values.get("customPacks"));
            assertNull(values.get("tilePacksVersion"));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void failedReadBackKeepsOldValues() {
        ConfigManager configManager = mockConfigManager();
        Map<String, String> values = new HashMap<>();
        values.put("customPacks", "{\"10000\":{\"id\":10000,\"packName\":\"Custom\",\"packTiles\":\"[]\"}}");
        stubConfiguration(configManager, values);
        doAnswer(invocation -> null)
                .when(configManager).setConfiguration(anyString(), anyString(), any());

        try {
            new TilePackMigrations(gson, configManager).migrateToLatest();
        } finally {
            assertNotNull(values.get("customPacks"));
            assertNull(values.get("tilePacksVersion"));
        }
    }

    private ConfigManager mockConfigManager() {
        return mock(ConfigManager.class);
    }

    private void stubConfiguration(ConfigManager configManager, Map<String, String> values) {
        when(configManager.getConfiguration(anyString(), anyString()))
                .thenAnswer(invocation -> values.get(invocation.getArgument(1)));
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
