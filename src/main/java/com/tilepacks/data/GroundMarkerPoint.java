package com.tilepacks.data;

import lombok.EqualsAndHashCode;
import lombok.Value;

import javax.annotation.Nullable;
import java.awt.*;

/**
 * Used for serialization of ground marker points.
 */
@Value
@EqualsAndHashCode(exclude = {"color", "label"})
public class GroundMarkerPoint {
    public int regionId;
    public int regionX;
    public int regionY;
    public int z;
    @Nullable
    public Color color;
    @Nullable
    public String label;

    public GroundMarkerPoint(int regionId, int regionX, int regionY, int z, @Nullable Color color, @Nullable String label) {
        this.regionId = regionId;
        this.regionX = regionX;
        this.regionY = regionY;
        this.z = z;
        this.color = color;
        this.label = label;
    }

    public boolean matchesLocation(GroundMarkerPoint point) {
        return point.getRegionId() == regionId
                && point.getRegionX() == regionX
                && point.getRegionY() == regionY
                && point.getZ() == z;
    }
}