package dev.cerus.maps.api.graphics.filter;

import dev.cerus.maps.api.colormap.ColorMaps;
import dev.cerus.maps.api.graphics.ColorCache;
import dev.cerus.maps.api.graphics.MapGraphics;
import java.awt.Color;

/**
 * Performs box blur on an area. Can be reused
 */
public class BoxBlurFilter implements Filter {

    private final int passes;
    private final TransparencyHandling transparencyHandling;

    public BoxBlurFilter() {
        this(1, TransparencyHandling.IGNORE);
    }

    public BoxBlurFilter(final int passes, final TransparencyHandling transparencyHandling) {
        if (passes <= 0) {
            throw new IllegalArgumentException("passes <= 0");
        }
        this.passes = passes;
        this.transparencyHandling = transparencyHandling;
    }

    // https://en.wikipedia.org/wiki/Box_blur
    @Override
    public byte apply(final MapGraphics<?> graphics, final int x, final int y, final int minX, final int maxX, final int minY, final int maxY) {
        if (x < minX + 1 || y < minY + 1 || x + 1 == maxX || y + 1 == maxY
                || (graphics.getPixel(x, y) >= 0 && graphics.getPixel(x, y) <= 3)) {
            return graphics.getPixel(x, y);
        }

        try {
            final int[] avg = this.averageNearestNinePixels(graphics, x, y);
            return ColorCache.rgbToMap(avg[0], avg[1], avg[2]);
        } catch (final KeepOriginalSignal ignored) {
            return graphics.getPixel(x, y);
        }
    }

    private int[] averageNearestNinePixels(final MapGraphics<?> graphics, final int x, final int y) {
        return this.average(
                this.mapToRgb(graphics.getPixel(x - 1, y + 1)),
                this.mapToRgb(graphics.getPixel(x, y + 1)),
                this.mapToRgb(graphics.getPixel(x + 1, y + 1)),
                this.mapToRgb(graphics.getPixel(x - 1, y)),
                this.mapToRgb(graphics.getPixel(x, y)),
                this.mapToRgb(graphics.getPixel(x + 1, y)),
                this.mapToRgb(graphics.getPixel(x - 1, y - 1)),
                this.mapToRgb(graphics.getPixel(x, y - 1)),
                this.mapToRgb(graphics.getPixel(x + 1, y - 1))
        );
    }

    private int[] average(final int... colors) {
        int r = 0;
        int g = 0;
        int b = 0;
        int count = 0;
        for (final int rgb : colors) {
            if (rgb == -1) {
                continue;
            }
            final Color color = new Color(rgb);
            r += color.getRed();
            g += color.getGreen();
            b += color.getBlue();
            count++;
        }
        return new int[] {r / count, g / count, b / count};
    }

    private int mapToRgb(final byte color) {
        if (color >= 0 && color <= 3) {
            if (this.transparencyHandling == TransparencyHandling.TREAT_AS_WHITE) {
                return new Color(255, 255, 255, 0).getRGB();
            } else if (this.transparencyHandling == TransparencyHandling.TREAT_AS_BLACK) {
                return new Color(0, 0, 0, 0).getRGB();
            } else if (this.transparencyHandling == TransparencyHandling.IGNORE) {
                return -1;
            } else if (this.transparencyHandling == TransparencyHandling.KEEP_ORIGINAL) {
                // This seemed like the easiest way to me
                throw new KeepOriginalSignal();
            }
        }
        //return MapColor.mapColorToRgb(color).getRGB();
        return ColorMaps.current().mapColorToRgb(color).getRGB();
    }

    @Override
    public int passes() {
        return this.passes;
    }

    public enum TransparencyHandling {
        TREAT_AS_WHITE,
        TREAT_AS_BLACK,
        KEEP_ORIGINAL,
        IGNORE
    }

    private static class KeepOriginalSignal extends RuntimeException {
    }

}
