package com.tilepacks;

import net.runelite.client.config.*;

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
    default Color overrideColor() {
        return Color.YELLOW;
    }

    @ConfigItem(
            keyName = "drawTilesOnMinimmap",
            name = "Draw tiles on minimap",
            description = "Configures whether tile packs tiles should be drawn on minimap"
    )
    default boolean drawTilesOnMinimmap() {
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

    @Range(min = 0, max = 100)
    @ConfigItem(
            keyName = "overrideOpacity",
            name = "Override Opacity",
            description = "Opacity (0-100) used by Override Opacity Mode"
    )
    default int overrideOpacity() {
        return 100;
    }

    @ConfigItem(
            keyName = "overrideOpacityMode",
            name = "Override Opacity Mode",
            description = "Off: No override. All: Always use Override. Cap: Opacity reduced to Override, but not raised if lower."
    )
    default OverrideOpacityMode overrideOpacityMode() {
        return OverrideOpacityMode.OFF;
    }

    enum OverrideOpacityMode {
        OFF("Off"),
        ALL("All"),
        CAP("Cap");

        private final String label;

        OverrideOpacityMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}