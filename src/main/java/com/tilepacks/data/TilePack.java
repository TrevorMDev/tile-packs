package com.tilepacks.data;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.Keybind;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.List;

/**
 * Represents all the data needed to make a TilePack
 */
public class TilePack {
    public Integer id;
    public String packName;
    public String link;
    public String packTiles;
    public transient List<GroundMarkerPoint> parsedPackTiles;
    @Nullable
    public Color defaultColor;
    public boolean visible = true;
    @Nullable
    public Keybind hotkey;

    public TilePack() {
    }

    public TilePack(Integer id, String packName, String packTiles) {
        this.id = id;
        this.packName = packName;
        this.packTiles = packTiles;
    }

    public TilePack(Integer id, String packName, String link, String packTiles) {
        this.id = id;
        this.packName = packName;
        this.link = link;
        this.packTiles = packTiles;
    }

    public TilePack(Integer id, boolean visible) {
        this.id = id;
        this.visible = visible;
    }

    public TilePack(Integer id, boolean visible, Keybind hotkey) {
        this.id = id;
        this.visible = visible;
        this.hotkey = hotkey;
    }

    public int indexOf(WorldPoint point) {
        for (int i = 0; i < parsedPackTiles.size(); i++) {
            if(parsedPackTiles.get(i).matchesLocation(point)) {
                return i;
            }
        }
        return -1;
    }
}