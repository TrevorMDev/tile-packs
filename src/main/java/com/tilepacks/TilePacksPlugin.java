package com.tilepacks;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
	name = "Tile Packs"
)
public class TilePacksPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private TilePacksConfig config;


	@Inject
	private ClientToolbar clientToolbar;

	private TilePacksPanel panel;
	private NavigationButton navButton;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Tile Packs Plugin starting");
		panel = new TilePacksPanel();
		final BufferedImage icon = ImageUtil.loadImageResource(TilePacksPlugin.class, "tilepacks_icon.png");

		navButton = NavigationButton.builder()
				.tooltip("Tile Packs")
				.icon(icon)
				.priority(4)//how is this determined? Could it be a config?
				.panel(panel)
				.build();

		if(config.hidePlugin()) {
			log.info("tile packs hidden, skipping adding");
			return;
		}

		clientToolbar.addNavigation(navButton);
		log.info("Tile Packs Plugin started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Tile Packs Plugin stopping");
		clientToolbar.removeNavigation(navButton);
		log.info("Tile Packs Plugin stopped");
	}

	@Subscribe
	public void onConfigChanged(final ConfigChanged event)
	{
		if (event.getGroup().equals(TilePacksConfig.GROUP))
		{
			switch (event.getKey())
			{
				case "hidePlugin":
					if (config.hidePlugin())
					{
						clientToolbar.removeNavigation(navButton);
					}
					else
					{
						clientToolbar.addNavigation(navButton);
					}
					break;
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
//			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Provides
	TilePacksConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TilePacksConfig.class);
	}
}
