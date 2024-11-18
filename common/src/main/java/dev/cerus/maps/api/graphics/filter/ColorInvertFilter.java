package dev.cerus.maps.api.graphics.filter;

import dev.cerus.maps.api.graphics.MapColors;
import dev.cerus.maps.api.graphics.MapGraphics;
import java.awt.Color;

public class ColorInvertFilter implements Filter {

    @Override
    public byte apply(MapGraphics<?> graphics, int x, int y, int minX, int maxX, int minY, int maxY) {
        byte pixel = graphics.getPixel(x, y);
        if (graphics.isTransparent(pixel)) {
            return pixel;
        }
        Color color = MapColors.color(pixel);
        if (color == null) {
            return pixel;
        }
        int rgb = 0xFFFFFF - color.getRGB();
        return MapColors.color(
                (rgb >> 16) & 0xFF,
                (rgb >> 8) & 0xFF,
                (rgb >> 0) & 0xFF
        );
    }

    @Override
    public int passes() {
        return 1;
    }

}
