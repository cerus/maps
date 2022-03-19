package dev.cerus.maps.api.graphics;

import dev.cerus.maps.api.MapColor;

public class ColorCache {

    private static final byte[] mappedColors = new byte[256 * 256 * 256];

    private ColorCache() {
        throw new UnsupportedOperationException();
    }

    public static byte rgbToMap(final int r, final int g, final int b) {
        final int index = convert3Dto1D(r, g, b);
        if (mappedColors[index] == 0) {
            mappedColors[index] = (byte) MapColor.rgbToMapColor(r, g, b).getId();
        }
        return mappedColors[index];
    }

    // https://stackoverflow.com/a/34363187/10821925
    private static int convert3Dto1D(final int x, final int y, final int z) {
        return (z * 256 * 256) + (y * 256) + x;
    }

}
