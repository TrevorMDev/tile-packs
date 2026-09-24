package com.tilepacks;

import com.google.common.base.Strings;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;

/**
 * This class is responsible for all functions that interface with or modify the configs related to global filters.
 */
@Slf4j
@Value
public class FilterManager {
    private static final String CONFIG_GROUP = "tilePacks";
    private static final String SHOW_VISIBLE = "showVisible";
    private static final String SHOW_INVISIBLE = "showInvisible";

    @NonFinal
    private boolean showVisible;
    @NonFinal
    private boolean showInvisible;

    private final ConfigManager configManager;

    @Inject
    FilterManager( ConfigManager configManager) {
        this.configManager = configManager;
        this.loadFilters();
    }

    //loads the filters from the config
    private void loadFilters() {
        String showVisibleConfig = configManager.getConfiguration(CONFIG_GROUP, SHOW_VISIBLE);

        if (Strings.isNullOrEmpty(showVisibleConfig)) {
            this.showVisible = true;
        } else {
            this.showVisible = Boolean.parseBoolean(showVisibleConfig);
        }

        String showInvisibleConfig = configManager.getConfiguration(CONFIG_GROUP, SHOW_INVISIBLE);

        if (Strings.isNullOrEmpty(showInvisibleConfig)) {
            this.showInvisible = false;
        } else {
            this.showInvisible = Boolean.parseBoolean(showInvisibleConfig);
        }
    }

    //sets showVisible in the config
    public void setShowVisible(boolean showVisible) {
        this.showVisible = showVisible;
        configManager.setConfiguration(CONFIG_GROUP, SHOW_VISIBLE, showVisible);
    }

    //returns the value of showVisible
    public boolean getShowVisible() {
        return this.showVisible;
    }

    //sets showInvisible in the config
    public void setShowInvisible(boolean showInvisible) {
        this.showInvisible = showInvisible;
        configManager.setConfiguration(CONFIG_GROUP, SHOW_INVISIBLE, showInvisible);
    }

    //returns the value of showVisible
    public boolean getShowInvisible() {
        return this.showInvisible;
    }
}
