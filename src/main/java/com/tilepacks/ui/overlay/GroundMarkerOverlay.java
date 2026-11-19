package com.tilepacks.ui.overlay;

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.tilepacks.PointManager;
import com.tilepacks.TilePacksConfig;
import com.tilepacks.TilePacksConfig.OverrideOpacityMode;
import com.tilepacks.data.ColorTileMarker;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.*;

/**
 * Manages the drawing of the markers on ground
 */
public class GroundMarkerOverlay extends Overlay {
    private static final int MAX_DRAW_DISTANCE = 32;

    private final PointManager pointManager;
    private final Client client;
    private final TilePacksConfig config;

    @Inject
    public GroundMarkerOverlay(PointManager pointManager, Client client, TilePacksConfig config) {
        this.pointManager = pointManager;
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(PRIORITY_LOW);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        final Multimap<WorldView, ColorTileMarker> points = pointManager.getPoints();
        if (points.isEmpty()) {
            return null;
        }

        Stroke stroke = new BasicStroke((float) config.borderWidth());
        for (WorldView worldView : points.keySet()) {
            for (final ColorTileMarker point : points.get(worldView)) {
                WorldPoint worldPoint = point.getWorldPoint();
                if (worldPoint.getPlane() != worldView.getPlane()) {
                    continue;
                }

                Color tileColor;
                if (config.overrideColorActive()) {
                    tileColor = config.overrideColor();
                } else {
                    tileColor = point.getColor();
                }

                String label = null;
                if (config.showLabels()) {
                    label = point.getLabel();
                }

                drawTile(graphics, worldView, worldPoint, tileColor, label, stroke);
            }
        }

        return null;
    }

    private void drawTile(Graphics2D graphics, WorldView worldView, WorldPoint point, Color color, @Nullable String label, Stroke borderStroke) {
        if (client.getLocalPlayer().getWorldView().isTopLevel())
        {
            WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

            if (point.distanceTo(playerLocation) >= MAX_DRAW_DISTANCE)
            {
                return;
            }
        }

        LocalPoint lp = LocalPoint.fromWorld(worldView, point);
        if (lp == null) {
            return;
        }

        Polygon poly = Perspective.getCanvasTilePoly(client, lp);
        if (poly != null) {
            OverlayUtil.renderPolygon(graphics, poly, applyOverrideOpacity(color, config),
                    new Color(0, 0, 0, config.fillOpacity()), borderStroke);
        }

        if (!Strings.isNullOrEmpty(label)) {
            Point canvasTextLocation = Perspective.getCanvasTextLocation(client, graphics, lp, label, 0);
            if (canvasTextLocation != null) {
                OverlayUtil.renderTextLocation(graphics, canvasTextLocation, label, color);
            }
        }
    }

    /**
     * Returns the color with the global override opacity applied for the configured mode. The RGB stays the same.
     * ALL sets the alpha to the override opacity; CAP only lowers alpha that is above it.
     * Package private and static so it can be unit tested without a client.
     */
    static Color applyOverrideOpacity(Color color, TilePacksConfig config) {
        OverrideOpacityMode mode = config.overrideOpacityMode();
        if (mode == OverrideOpacityMode.OFF) {
            return color;
        }
        int overrideAlpha = Math.round(config.overrideOpacity() * 255f / 100f);
        int alpha = mode == OverrideOpacityMode.CAP ? Math.min(color.getAlpha(), overrideAlpha) : overrideAlpha;
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}