package dev.cerus.maps.api.graphics.filter;

import dev.cerus.maps.api.graphics.MapGraphics;

public interface Filter {

    byte calculate(MapGraphics<?, ?> graphics, int x, int y, int minX, int maxX, int minY, int maxY);

    int passes();

}
