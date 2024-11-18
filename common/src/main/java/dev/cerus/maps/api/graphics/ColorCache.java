package dev.cerus.maps.api.graphics;

import dev.cerus.maps.api.colormap.ColorMaps;
import java.awt.Color;

/**
 * Lazy cache that maps rgb values to map colors
 */
public class ColorCache {

    private static final byte[] mappedColors = new byte[256 * 256 * 256];

    private ColorCache() {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieves a cached color. If the color is not cached it will be calculated and then cached.
     * <p>
     * Takes up 16MB of memory when full
     *
     * @param r Red value
     * @param g Green value
     * @param b Blue value
     *
     * @return Cached map color
     */
    public static byte rgbToMap(final int r, final int g, final int b) {
        final int index = new Color(r, g, b, 0).getRGB();
        if (mappedColors[index] == 0) {
            mappedColors[index] = ColorMaps.current().rgbToMapColor(r, g, b).mapColor();
        }
        return mappedColors[index];
    }

}
