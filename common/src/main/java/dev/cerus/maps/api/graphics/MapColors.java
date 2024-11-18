package dev.cerus.maps.api.graphics;

import dev.cerus.maps.api.colormap.ColorMaps;
import java.awt.Color;

public final class MapColors {
    private MapColors() {
        throw new UnsupportedOperationException();
    }

    public static byte color(Color color) {
        return color(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static byte color(int r, int g, int b) {
        return ColorCache.rgbToMap(r, g ,b);
    }

    public static Color color(byte mapColor) {
        return ColorMaps.current().mapColorToRgb(mapColor);
    }
}
