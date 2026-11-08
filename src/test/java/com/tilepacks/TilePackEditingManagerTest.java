package com.tilepacks;

import com.tilepacks.data.GroundMarkerPoint;
import com.tilepacks.data.TilePack;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.game.chatbox.ChatboxTextInput;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TilePackEditingManagerTest extends TilePacksTest {
    private static final int PACK_ID = 10000;
    private static final WorldPoint POINT_A = new WorldPoint(3200, 3229, 2); // region 12850, x 0, y 29
    private static final WorldPoint POINT_B = new WorldPoint(3201, 3229, 2); // region 12850, x 1, y 29

    // ------------------------------------------------------------------
    // Existing tests
    // ------------------------------------------------------------------

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
        ChatboxPanelManager chatboxPanelManager = mock(ChatboxPanelManager.class);
        ClientThread clientThread = mock(ClientThread.class);
        TilePackEditingManager editingManager = new TilePackEditingManager(
                mock(Client.class), gson, tilePackManager, pointManager, new TestConfig(Color.YELLOW), chatboxPanelManager, clientThread);
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
        ChatboxPanelManager chatboxPanelManager = mock(ChatboxPanelManager.class);
        ClientThread clientThread = mock(ClientThread.class);
        TilePackEditingManager editingManager = new TilePackEditingManager(
                mock(Client.class), gson, tilePackManager, mock(PointManager.class), new TestConfig(Color.YELLOW), chatboxPanelManager, clientThread);
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
        ChatboxPanelManager chatboxPanelManager = mock(ChatboxPanelManager.class);
        ClientThread clientThread = mock(ClientThread.class);
        TilePackEditingManager editingManager = new TilePackEditingManager(
                mock(Client.class), gson, tilePackManager, mock(PointManager.class), new TestConfig(Color.YELLOW), chatboxPanelManager, clientThread);
        WorldPoint point = new WorldPoint(3200, 3229, 2);

        assertFalse(editingManager.toggleTile(10000, point));
        editingManager.editPack(10000);
        editingManager.stopEditing();
        assertFalse(editingManager.toggleTile(10000, point));
        assertEquals("[]", gson.fromJson(values.get("pack_10000"), TilePack.class).packTiles);
    }

    // ------------------------------------------------------------------
    // setTileLabel
    // ------------------------------------------------------------------

    @Test
    public void setTileLabelUpdatesOnlyTheTargetTileAndPersists() {
        Fixture f = new Fixture(tile(0, Color.RED, null), tile(1, Color.GREEN, "keep"));
        f.editingManager.editPack(PACK_ID);

        assertTrue(f.editingManager.setTileLabel(PACK_ID, POINT_A, "Bank"));

        List<GroundMarkerPoint> saved = f.savedTiles();
        assertEquals(2, saved.size());
        assertEquals(0, saved.get(0).getRegionX());
        assertEquals("Bank", saved.get(0).getLabel());
        assertEquals(Color.RED, saved.get(0).getColor());
        assertEquals(1, saved.get(1).getRegionX());
        assertEquals("keep", saved.get(1).getLabel());
        assertEquals(Color.GREEN, saved.get(1).getColor());
        verify(f.pointManager).loadPoints();
    }

    @Test
    public void setTileLabelTrimsWhitespace() {
        Fixture f = new Fixture(tile(0, Color.RED, null));
        f.editingManager.editPack(PACK_ID);

        assertTrue(f.editingManager.setTileLabel(PACK_ID, POINT_A, "  Bank  "));
        assertEquals("Bank", f.savedTiles().get(0).getLabel());
    }

    @Test
    public void setTileLabelWithBlankOrNullClearsTheLabel() {
        Fixture f = new Fixture(tile(0, Color.RED, "old"));
        f.editingManager.editPack(PACK_ID);

        assertTrue(f.editingManager.setTileLabel(PACK_ID, POINT_A, "   "));
        assertNull(f.savedTiles().get(0).getLabel());

        assertTrue(f.editingManager.setTileLabel(PACK_ID, POINT_A, "again"));
        assertEquals("again", f.savedTiles().get(0).getLabel());

        assertTrue(f.editingManager.setTileLabel(PACK_ID, POINT_A, null));
        assertNull(f.savedTiles().get(0).getLabel());
    }

    @Test
    public void setTileLabelSkipsTheSaveWhenNothingChanged() {
        Fixture f = new Fixture(tile(0, Color.RED, "Bank"), tile(1, Color.RED, null));
        f.editingManager.editPack(PACK_ID);
        String before = f.rawSave();

        assertFalse(f.editingManager.setTileLabel(PACK_ID, POINT_A, "Bank"));
        assertFalse(f.editingManager.setTileLabel(PACK_ID, POINT_A, "  Bank "));  // same after trimming
        assertFalse(f.editingManager.setTileLabel(PACK_ID, POINT_B, ""));         // null -> null

        assertEquals(before, f.rawSave());
        verify(f.pointManager, never()).loadPoints();
    }

    @Test
    public void setTileLabelIgnoresTilesThatAreNotInThePack() {
        Fixture f = new Fixture(tile(0, Color.RED, null));
        f.editingManager.editPack(PACK_ID);
        String before = f.rawSave();

        assertFalse(f.editingManager.setTileLabel(PACK_ID, POINT_B, "Bank"));

        assertEquals(before, f.rawSave());
        verify(f.pointManager, never()).loadPoints();
    }

    @Test
    public void setTileLabelRequiresTheOpenEditorTarget() {
        Fixture f = new Fixture(tile(0, Color.RED, null));
        String before = f.rawSave();

        assertFalse(f.editingManager.setTileLabel(PACK_ID, POINT_A, "Bank"));   // not editing yet
        f.editingManager.editPack(PACK_ID);
        assertFalse(f.editingManager.setTileLabel(10001, POINT_A, "Bank"));     // different pack
        f.editingManager.stopEditing();
        assertFalse(f.editingManager.setTileLabel(PACK_ID, POINT_A, "Bank"));   // left edit mode

        assertEquals(before, f.rawSave());
        verify(f.pointManager, never()).loadPoints();
    }

    // ------------------------------------------------------------------
    // Editing state and cache lifecycle
    // ------------------------------------------------------------------

    @Test
    public void editPackOnlyAcceptsExistingPacks() {
        Fixture f = new Fixture();

        f.editingManager.editPack(PACK_ID);
        assertEquals(Integer.valueOf(PACK_ID), f.editingManager.getEditingPackId());

        f.editingManager.editPack(99999);
        assertNull(f.editingManager.getEditingPackId());

        f.editingManager.editPack(PACK_ID);
        f.editingManager.editPack(null);
        assertNull(f.editingManager.getEditingPackId());
    }

    @Test
    public void clearLeavesEditModeAndRejectsEdits() {
        Fixture f = new Fixture(tile(0, Color.RED, null));
        f.editingManager.editPack(PACK_ID);

        f.editingManager.clear();

        assertNull(f.editingManager.getEditingPackId());
        assertFalse(f.editingManager.toggleTile(PACK_ID, POINT_A));
        assertFalse(f.editingManager.setTileLabel(PACK_ID, POINT_A, "Bank"));
    }

    @Test
    public void clearDropsTheParsedCacheSoNextEditReadsFreshData() {
        Fixture f = new Fixture(tile(0, Color.RED, null));
        f.editingManager.editPack(PACK_ID);
        f.tilePackManager.updatePackTiles(PACK_ID, gson.toJson(Collections.singletonList(tile(1, Color.GREEN, null))));

        f.editingManager.clear();
        f.editingManager.editPack(PACK_ID);

        // Fresh parse sees only tile B, so toggling A adds it rather than removing it
        assertTrue(f.editingManager.toggleTile(PACK_ID, POINT_A));
        List<GroundMarkerPoint> saved = f.savedTiles();
        assertEquals(2, saved.size());
        assertEquals(1, saved.get(0).getRegionX());
        assertEquals(0, saved.get(1).getRegionX());
    }

    /** Pins the documented contract: stopEditing keeps the parsed cache. Update if you add invalidation. */
    @Test
    public void stopEditingRetainsTheParsedCache() {
        Fixture f = new Fixture(tile(0, Color.RED, null));
        f.editingManager.editPack(PACK_ID);
        f.editingManager.stopEditing();
        f.tilePackManager.updatePackTiles(PACK_ID, gson.toJson(Collections.singletonList(tile(1, Color.GREEN, null))));

        f.editingManager.editPack(PACK_ID);

        // The cached copy (tile A only) is still used, so toggling A removes it
        assertTrue(f.editingManager.toggleTile(PACK_ID, POINT_A));
        assertEquals("[]", f.savedPack().packTiles);
    }

    @Test
    public void unreadableTileDataDisablesEditsWithoutSaving() {
        for (String bad : new String[]{"not json", "null"}) {
            Fixture f = new Fixture(bad, new TestConfig(Color.YELLOW));
            String before = f.rawSave();

            f.editingManager.editPack(PACK_ID);

            assertFalse(f.editingManager.toggleTile(PACK_ID, POINT_A));
            assertFalse(f.editingManager.setTileLabel(PACK_ID, POINT_A, "Bank"));
            assertEquals(before, f.rawSave());
            verify(f.pointManager, never()).loadPoints();
        }
    }

    // ------------------------------------------------------------------
    // New tile colour resolution
    // ------------------------------------------------------------------

    @Test
    public void newTilesFallBackToYellowWithoutPackColorOrOverride() {
        Fixture f = new Fixture();
        f.editingManager.editPack(PACK_ID);

        assertTrue(f.editingManager.toggleTile(PACK_ID, POINT_A));
        assertEquals(Color.YELLOW, f.savedTiles().get(0).getColor());
    }

    @Test
    public void newTilesUseTheOverrideColorWhenActive() {
        TilePacksConfig overrideActive = new TestConfig(Color.MAGENTA) {
            @Override
            public boolean overrideColorActive() {
                return true;
            }
        };
        Fixture f = new Fixture("[]", overrideActive);
        f.editingManager.editPack(PACK_ID);

        assertTrue(f.editingManager.toggleTile(PACK_ID, POINT_A));
        assertEquals(Color.MAGENTA, f.savedTiles().get(0).getColor());
    }

    @Test
    public void packDefaultColorBeatsAnActiveOverride() {
        TilePacksConfig overrideActive = new TestConfig(Color.MAGENTA) {
            @Override
            public boolean overrideColorActive() {
                return true;
            }
        };
        Fixture f = new Fixture("[]", overrideActive);
        f.tilePackManager.updatePackDefaultColor(PACK_ID, Color.BLUE);
        f.editingManager.editPack(PACK_ID);

        assertTrue(f.editingManager.toggleTile(PACK_ID, POINT_A));
        assertEquals(Color.BLUE, f.savedTiles().get(0).getColor());
    }

    // ------------------------------------------------------------------
    // Label prompt
    // ------------------------------------------------------------------

    @Test
    public void promptForLabelOpensChatboxPrefilledWithTheCurrentLabel() {
        Fixture f = new Fixture(tile(0, Color.RED, "Bank"));
        ChatboxTextInput input = f.stubTextInput();
        f.editingManager.editPack(PACK_ID);

        f.editingManager.promptForLabel(PACK_ID, POINT_A);

        verify(f.chatboxPanelManager).openTextInput("Tile label");
        verify(input).value("Bank");
        verify(input).build();
    }

    @Test
    public void promptForLabelPrefillsEmptyTextForUnlabelledTiles() {
        Fixture f = new Fixture(tile(0, Color.RED, null));
        ChatboxTextInput input = f.stubTextInput();
        f.editingManager.editPack(PACK_ID);

        f.editingManager.promptForLabel(PACK_ID, POINT_A);

        verify(input).value("");
    }

    @Test
    public void promptForLabelDoesNothingWithoutAnEditableTile() {
        Fixture f = new Fixture(tile(0, Color.RED, null));
        f.stubTextInput();

        f.editingManager.promptForLabel(PACK_ID, POINT_A);   // not in edit mode
        f.editingManager.editPack(PACK_ID);
        f.editingManager.promptForLabel(PACK_ID, POINT_B);   // tile not in the pack

        verify(f.chatboxPanelManager, never()).openTextInput(anyString());
    }

    @Test
    public void finishingThePromptSavesTheLabelOnTheClientThread() {
        Fixture f = new Fixture(tile(0, Color.RED, null));
        ChatboxTextInput input = f.stubTextInput();
        f.runClientThreadRunnables();
        f.editingManager.editPack(PACK_ID);
        f.editingManager.promptForLabel(PACK_ID, POINT_A);

        f.capturePromptCallback(input).accept("Vault");

        assertEquals("Vault", f.savedTiles().get(0).getLabel());
        verify(f.pointManager).loadPoints();
    }

    /** Regression: a BooleanSupplier overload would be retried every tick whenever it returns false. */
    @Test
    public void labelUpdateIsScheduledAsARunnableNotARetryingSupplier() {
        Fixture f = new Fixture(tile(0, Color.RED, null));
        ChatboxTextInput input = f.stubTextInput();
        f.editingManager.editPack(PACK_ID);
        f.editingManager.promptForLabel(PACK_ID, POINT_A);

        f.capturePromptCallback(input).accept("Vault");

        verify(f.clientThread).invoke(any(Runnable.class));
        verify(f.clientThread, never()).invoke(any(BooleanSupplier.class));
    }

    @Test
    public void promptFinishedAfterLeavingEditModeDoesNotSave() {
        Fixture f = new Fixture(tile(0, Color.RED, null));
        ChatboxTextInput input = f.stubTextInput();
        f.runClientThreadRunnables();
        f.editingManager.editPack(PACK_ID);
        f.editingManager.promptForLabel(PACK_ID, POINT_A);

        f.editingManager.stopEditing();
        f.capturePromptCallback(input).accept("Late");

        assertNull(f.savedTiles().get(0).getLabel());
        verify(f.pointManager, never()).loadPoints();
    }

    @Test
    public void promptFinishedAfterTheTileWasRemovedDoesNotSave() {
        Fixture f = new Fixture(tile(0, Color.RED, null));
        ChatboxTextInput input = f.stubTextInput();
        f.runClientThreadRunnables();
        f.editingManager.editPack(PACK_ID);
        f.editingManager.promptForLabel(PACK_ID, POINT_A);

        assertTrue(f.editingManager.toggleTile(PACK_ID, POINT_A));   // removes the tile
        f.capturePromptCallback(input).accept("Late");

        assertEquals("[]", f.savedPack().packTiles);
        verify(f.pointManager, times(1)).loadPoints();               // only the removal saved
    }

    // ------------------------------------------------------------------
    // onMenuEntryAdded guards
    // ------------------------------------------------------------------

    @Test
    public void menuEntriesAreNotAddedOutsideEditMode() {
        Fixture f = new Fixture(tile(0, Color.RED, null));
        when(f.client.isKeyPressed(KeyCode.KC_SHIFT)).thenReturn(true);

        f.editingManager.onMenuEntryAdded(walkMenuEvent());

        verify(f.client, never()).createMenuEntry(anyInt());
        verify(f.client, never()).getWorldView(anyInt());
    }

    @Test
    public void menuEntriesAreNotAddedWithoutShift() {
        Fixture f = new Fixture(tile(0, Color.RED, null));
        f.editingManager.editPack(PACK_ID);

        f.editingManager.onMenuEntryAdded(walkMenuEvent());

        verify(f.client, never()).createMenuEntry(anyInt());
        verify(f.client, never()).getWorldView(anyInt());
    }

    @Test
    public void menuEntriesAreNotAddedWithoutAWorldViewOrSelectedTile() {
        Fixture f = new Fixture(tile(0, Color.RED, null));
        f.editingManager.editPack(PACK_ID);
        when(f.client.isKeyPressed(KeyCode.KC_SHIFT)).thenReturn(true);

        f.editingManager.onMenuEntryAdded(walkMenuEvent());                  // no world view
        when(f.client.getWorldView(anyInt())).thenReturn(mock(WorldView.class));
        f.editingManager.onMenuEntryAdded(walkMenuEvent());                  // no selected tile

        verify(f.client, never()).createMenuEntry(anyInt());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static GroundMarkerPoint tile(int regionX, Color color, String label) {
        return new GroundMarkerPoint(12850, regionX, 29, 2, color, label);
    }

    private MenuEntryAdded walkMenuEvent() {
        MenuEntry entry = mock(MenuEntry.class);
        when(entry.getType()).thenReturn(MenuAction.WALK);
        return new MenuEntryAdded(entry);
    }

    private List<GroundMarkerPoint> parsePoints(String json) {
        return gson.fromJson(json, new com.google.common.reflect.TypeToken<List<GroundMarkerPoint>>() { }.getType());
    }

    /** Wires up a manager around a single pack (id 10000) seeded with the given tiles. */
    private final class Fixture {
        final Map<String, String> values = new HashMap<>();
        final PointManager pointManager = mock(PointManager.class);
        final ChatboxPanelManager chatboxPanelManager = mock(ChatboxPanelManager.class);
        final ClientThread clientThread = mock(ClientThread.class);
        final Client client = mock(Client.class);
        final TilePackManager tilePackManager;
        final TilePackEditingManager editingManager;

        Fixture(GroundMarkerPoint... tiles) {
            this(gson.toJson(Arrays.asList(tiles)), new TestConfig(Color.YELLOW));
        }

        Fixture(String packTilesJson, TilePacksConfig config) {
            ConfigManager configManager = mock(ConfigManager.class);
            stubConfiguration(configManager, values);
            values.put("pack_" + PACK_ID, gson.toJson(new TilePack(PACK_ID, "Tiles", packTilesJson)));
            tilePackManager = new TilePackManager(gson, configManager);
            editingManager = new TilePackEditingManager(client, gson, tilePackManager, pointManager,
                    config, chatboxPanelManager, clientThread);
        }

        String rawSave() {
            return values.get("pack_" + PACK_ID);
        }

        TilePack savedPack() {
            return gson.fromJson(rawSave(), TilePack.class);
        }

        List<GroundMarkerPoint> savedTiles() {
            return parsePoints(savedPack().packTiles);
        }

        /** Makes the mocked ClientThread run Runnables immediately. */
        void runClientThreadRunnables() {
            doAnswer(invocation -> {
                invocation.getArgument(0, Runnable.class).run();
                return null;
            }).when(clientThread).invoke(any(Runnable.class));
        }

        ChatboxTextInput stubTextInput() {
            ChatboxTextInput input = mock(ChatboxTextInput.class, RETURNS_SELF);
            when(chatboxPanelManager.openTextInput(anyString())).thenReturn(input);
            return input;
        }

        @SuppressWarnings("unchecked")
        Consumer<String> capturePromptCallback(ChatboxTextInput input) {
            ArgumentCaptor<Consumer<String>> captor = ArgumentCaptor.forClass(Consumer.class);
            verify(input).onDone(captor.capture());
            return captor.getValue();
        }
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