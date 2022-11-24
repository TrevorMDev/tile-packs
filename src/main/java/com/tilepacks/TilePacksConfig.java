package com.tilepacks;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(TilePacksConfig.GROUP)
public interface TilePacksConfig extends Config
{
	String GROUP = "tilepacks";

	@ConfigItem(
		keyName = "hidePlugin",
		name = "Hide on toolbar",
		description = "When checked, the plugin will not appear in the tool bar"
	)
	default boolean hidePlugin()
	{
		return false;
	}

	@ConfigItem(
			keyName = "borderWidth",
			name = "Border Width",
			description = "Width of the marked tile border"
	)
	default double borderWidth()
	{
		return 2;
	}

	@ConfigItem(
			keyName = "fillOpacity",
			name = "Fill Opacity",
			description = "Opacity of the tile fill color"
	)
	default int fillOpacity()
	{
		return 50;
	}
}