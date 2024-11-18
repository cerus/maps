package dev.cerus.maps.api.graphics.filter;

import dev.cerus.maps.api.colormap.ColorMaps;
import dev.cerus.maps.api.graphics.ColorCache;
import dev.cerus.maps.api.graphics.MapGraphics;
import java.awt.Color;

public class GrayscaleFilter implements Filter {

    @Override
    public byte apply(final MapGraphics<?> graphics, final int x, final int y, final int minX, final int maxX, final int minY, final int maxY) {
        final byte pixel = graphics.getPixel(x, y);
        if (graphics.isTransparent(pixel)) {
            return pixel;
        }
        //final Color color = MapColor.mapColorToRgb(pixel);
        final Color color = ColorMaps.current().mapColorToRgb(pixel);
        if (color == null) {
            return pixel;
        }
        final int avg = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        return ColorCache.rgbToMap(
                avg,
                avg,
                avg
        );
    }

    @Override
    public int passes() {
        return 1;
    }

}
