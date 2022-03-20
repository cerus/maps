package dev.cerus.maps.api.graphics;

import dev.cerus.maps.api.MapColor;
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
        final int index = convert3Dto1D(r, g, b);
        if (mappedColors[index] == 0) {
            mappedColors[index] = (byte) MapColor.rgbToMapColor(r, g, b).getId();
        }
        return mappedColors[index];
    }

    // https://stackoverflow.com/a/34363187/10821925
    private static int convert3Dto1D(final int x, final int y, final int z) {
        if (true) {
            return new Color(x, y, z, 0).getRGB();
        }
        return (z * 256 * 256) + (y * 256) + x;
    }

}
