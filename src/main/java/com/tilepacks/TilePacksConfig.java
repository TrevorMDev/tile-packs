package com.tilepacks;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup(TilePacksConfig.GROUP)
public interface TilePacksConfig extends Config {
    String GROUP = "tilepacks";

    @ConfigItem(
            keyName = "hidePlugin",
            name = "Hide on toolbar",
            description = "When checked, the plugin will not appear in the tool bar"
    )
    default boolean hidePlugin() {
        return false;
    }

    @ConfigItem(
            keyName = "showLabels",
            name = "Show Labels",
            description = "When checked, labels will render as defined in each pack"
    )
    default boolean showLabels() {
        return true;
    }

    @ConfigItem(
            keyName = "overrideColorActive",
            name = "Override Color Active",
            description = "When checked, all tiles will render as the selected color"
    )
    default boolean overrideColorActive() {
        return false;
    }

    @Alpha
    @ConfigItem(
            keyName = "overrideColor",
            name = "Override Color",
            description = "If Use Override Color is checked, all tiles will be this color."
    )
    default Color overrideColor()
    {
        return Color.YELLOW;
    }

    @ConfigItem(
            keyName = "drawTilesOnMinimmap",
            name = "Draw tiles on minimap",
            description = "Configures whether tile packs tiles should be drawn on minimap"
    )
    default boolean drawTilesOnMinimmap()
    {
        return false;
    }

    @ConfigItem(
            keyName = "borderWidth",
            name = "Border Width",
            description = "Width of the marked tile border"
    )
    default double borderWidth() {
        return 2;
    }

    @ConfigItem(
            keyName = "fillOpacity",
            name = "Fill Opacity",
            description = "Opacity of the tile fill color"
    )
    default int fillOpacity() {
        return 50;
    }
}