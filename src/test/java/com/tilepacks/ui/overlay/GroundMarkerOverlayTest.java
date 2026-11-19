package com.tilepacks.ui.overlay;

import com.tilepacks.TilePacksConfig;
import com.tilepacks.TilePacksConfig.OverrideOpacityMode;
import org.junit.Test;

import java.awt.Color;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class GroundMarkerOverlayTest {
    private static final Color OPAQUE = new Color(10, 20, 30, 255);
    private static final Color FAINT = new Color(10, 20, 30, 40);

    @Test
    public void offLeavesTheColorUntouched() {
        TilePacksConfig config = config(OverrideOpacityMode.OFF, 10);

        assertSame(OPAQUE, GroundMarkerOverlay.applyOverrideOpacity(OPAQUE, config));
        assertSame(FAINT, GroundMarkerOverlay.applyOverrideOpacity(FAINT, config));
    }

    @Test
    public void allSetsTheAlphaToTheOverrideForOpaqueAndFaintTiles() {
        TilePacksConfig config = config(OverrideOpacityMode.ALL, 50);

        assertEquals(128, GroundMarkerOverlay.applyOverrideOpacity(OPAQUE, config).getAlpha());
        assertEquals(128, GroundMarkerOverlay.applyOverrideOpacity(FAINT, config).getAlpha());
    }

    @Test
    public void allKeepsTheRgb() {
        Color result = GroundMarkerOverlay.applyOverrideOpacity(FAINT, config(OverrideOpacityMode.ALL, 50));

        assertEquals(10, result.getRed());
        assertEquals(20, result.getGreen());
        assertEquals(30, result.getBlue());
    }

    @Test
    public void capLowersTilesAboveTheCap() {
        Color result = GroundMarkerOverlay.applyOverrideOpacity(OPAQUE, config(OverrideOpacityMode.CAP, 50));

        assertEquals(128, result.getAlpha());
    }

    @Test
    public void capDoesNotRaiseTilesBelowTheCap() {
        Color result = GroundMarkerOverlay.applyOverrideOpacity(FAINT, config(OverrideOpacityMode.CAP, 50));

        assertEquals(FAINT, result);
    }

    @Test
    public void capLeavesTilesExactlyAtTheCapUnchanged() {
        Color atCap = new Color(10, 20, 30, 128);

        Color result = GroundMarkerOverlay.applyOverrideOpacity(atCap, config(OverrideOpacityMode.CAP, 50));

        assertEquals(atCap, result);
    }

    @Test
    public void capKeepsTheRgb() {
        Color result = GroundMarkerOverlay.applyOverrideOpacity(OPAQUE, config(OverrideOpacityMode.CAP, 50));

        assertEquals(10, result.getRed());
        assertEquals(20, result.getGreen());
        assertEquals(30, result.getBlue());
    }

    @Test
    public void capAtZeroHidesEveryTile() {
        TilePacksConfig config = config(OverrideOpacityMode.CAP, 0);

        assertEquals(0, GroundMarkerOverlay.applyOverrideOpacity(OPAQUE, config).getAlpha());
        assertEquals(0, GroundMarkerOverlay.applyOverrideOpacity(FAINT, config).getAlpha());
    }

    @Test
    public void capAtFullOpacityChangesNothing() {
        TilePacksConfig config = config(OverrideOpacityMode.CAP, 100);

        assertEquals(OPAQUE, GroundMarkerOverlay.applyOverrideOpacity(OPAQUE, config));
        assertEquals(FAINT, GroundMarkerOverlay.applyOverrideOpacity(FAINT, config));
    }

    @Test
    public void overridePercentIsConvertedToAlphaWithRounding() {
        assertEquals(64, GroundMarkerOverlay.applyOverrideOpacity(OPAQUE, config(OverrideOpacityMode.ALL, 25)).getAlpha());
        assertEquals(128, GroundMarkerOverlay.applyOverrideOpacity(OPAQUE, config(OverrideOpacityMode.ALL, 50)).getAlpha());
        assertEquals(255, GroundMarkerOverlay.applyOverrideOpacity(FAINT, config(OverrideOpacityMode.ALL, 100)).getAlpha());
    }

    private static TilePacksConfig config(OverrideOpacityMode mode, int overrideOpacity) {
        return new TilePacksConfig() {
            @Override
            public OverrideOpacityMode overrideOpacityMode() {
                return mode;
            }

            @Override
            public int overrideOpacity() {
                return overrideOpacity;
            }
        };
    }
}