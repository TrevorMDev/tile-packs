package com.tilepacks;

import com.google.gson.Gson;
import com.google.inject.Provides;
import com.tilepacks.ui.overlay.GroundMarkerMinimapOverlay;
import com.tilepacks.ui.overlay.GroundMarkerOverlay;
import com.tilepacks.ui.panel.TilePacksPanel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.WorldViewLoaded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
        name = "Tile Packs"
)
public class TilePacksPlugin extends Plugin {
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
    private ChatMessageManager chatMessageManager;
    @Inject
    private ColorPickerManager colorPickerManager;
    @Inject
    private ChatboxPanelManager chatboxPanelManager;
    @Inject
    private ClientThread clientThread;

    private GroundMarkerMinimapOverlay minimapOverlay;
    private GroundMarkerOverlay overlay;
    private TilePackManager tilePackManager;
    private TilePackEditingManager tilePackEditingManager;
    private PointManager pointManager;
    private FilterManager filterManager;
    private TilePacksPanel panel;
    private NavigationButton navButton;

    @Override
    protected void startUp() throws Exception {
        try {
            new TilePackMigrations(gson, configManager, chatMessageManager).migrateToLatest();
        } catch (Exception e) {
            log.error("Unable to migrate Tile Packs configuration", e);
            chatMessageManager.queue(QueuedMessage.builder()
                    .type(ChatMessageType.CONSOLE)
                    .runeLiteFormattedMessage("Tile Packs failed to migrate to latest version. Please report this issue.")
                    .build());
            return;
        }

        tilePackManager = new TilePackManager(gson, configManager);
        pointManager = new PointManager(tilePackManager, client);
        tilePackEditingManager = new TilePackEditingManager(client, tilePackManager, pointManager, config, chatboxPanelManager, clientThread);
        filterManager = new FilterManager(configManager);

        overlay = new GroundMarkerOverlay(pointManager, client, config);
        minimapOverlay = new GroundMarkerMinimapOverlay(pointManager, client, config);
        overlayManager.add(overlay);
        overlayManager.add(minimapOverlay);
        panel = new TilePacksPanel(tilePackManager, tilePackEditingManager, pointManager, filterManager, chatMessageManager,
                gson, client, colorPickerManager, config);

        final BufferedImage icon = ImageUtil.loadImageResource(TilePacksPlugin.class, "tilepacks_icon.png");

        navButton = NavigationButton.builder()
                .tooltip("Tile Packs")
                .icon(icon)
                .priority(4)//how is this determined? Could it be a config?
                .panel(panel)
                .build();

        pointManager.loadPoints();

        log.debug("Tile Packs Plugin started");
        if (config.hidePlugin()) {
            return;
        }

        clientToolbar.addNavigation(navButton);

    }

    @Override
    protected void shutDown() throws Exception {
        if (panel != null) {
            panel.cancelPendingTitleEdit();
        }
        if (tilePackEditingManager != null) {
            tilePackEditingManager.stopEditing();
        }
        overlayManager.remove(overlay);
        overlayManager.remove(minimapOverlay);
        clientToolbar.removeNavigation(navButton);
        log.debug("Tile Packs Plugin stopped");
    }

    @Subscribe
    public void onConfigChanged(final ConfigChanged event) {
        if (event.getGroup().equals(TilePacksConfig.GROUP)) {
            switch (event.getKey()) {
                case "hidePlugin":
                    if (config.hidePlugin()) {
                        clientToolbar.removeNavigation(navButton);
                    } else {
                        clientToolbar.addNavigation(navButton);
                    }
                    break;
            }
        }
    }

    @Subscribe
    public void onProfileChanged(ProfileChanged profileChanged)
    {
        if (panel != null) {
            panel.cancelPendingTitleEdit();
        } else if (tilePackEditingManager != null) {
            tilePackEditingManager.stopEditing();
        }
        pointManager.loadPoints();
    }

    @Subscribe
    public void onWorldViewLoaded(WorldViewLoaded event) {
        log.debug("Changing world view");
        // map region has just been updated
        pointManager.loadWorldViewPoints(event.getWorldView());
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (tilePackEditingManager != null) {
            tilePackEditingManager.onMenuEntryAdded(event);
        }
    }

    @Provides
    TilePacksConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TilePacksConfig.class);
    }
}