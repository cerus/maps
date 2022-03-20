package dev.cerus.maps.api.graphics.filter;

import dev.cerus.maps.api.graphics.MapGraphics;

/**
 * A filter is a transformation that can be applied to an area of pixels.
 * This interface is tailored towards filters, but you can also use it for
 * non-filter transformations.
 */
public interface Filter {

    /**
     * Gets the transformed color for a specific pixel
     *
     * @param graphics The parent graphics
     * @param x        The pixel x
     * @param y        The pixel y
     * @param minX     The min x of the area
     * @param maxX     The max x of the area
     * @param minY     The min y of the area
     * @param maxY     The max y of the area
     *
     * @return The transformed color
     */
    byte apply(MapGraphics<?, ?> graphics, int x, int y, int minX, int maxX, int minY, int maxY);

    /**
     * The amount of times this filter should be applied
     *
     * @return The passes
     */
    int passes();

}
