package dev.cerus.maps.api;

import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.api.version.VersionAdapter;
import java.util.Collection;

/**
 * Base class for drawable map objects
 * <p>
 * See {@link ClientsideMap}, {@link MapScreen}, {@link dev.cerus.maps.api.layer.ScreenLayer}
 */
public interface MapAccess {

    /**
     * Mark the entire data buffer as dirty. Triggers a complete re-send of the entire data on some implementations.
     */
    void markFullyDirty();

    /**
     * Mark the data at the specified coordinates as "dirty". This is used to indicate that a pixel has changed
     * and will be used by the engine for calculating which parts of the buffer to send to players.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    void markDirty(int x, int y);

    /**
     * Clear all dirty states.
     */
    void clearDirty();

    /**
     * Add a marker to this map object.
     *
     * @param marker The marker to add
     */
    void addMarker(Marker marker);

    /**
     * Remove a marker from this map object.
     *
     * @param marker The marker to remove
     */
    void removeMarker(Marker marker);

    /**
     * Remove all markers from this map object.
     */
    void clearMarkers();

    Collection<Marker> getMarkers();

    /**
     * Get the base map at the specified coordinates.
     * <p>
     * This method highly depends on the implementation. {@link ClientsideMap}s for example will always return themselves.
     *
     * @param column The column
     * @param row    The row
     *
     * @return The base map
     */
    ClientsideMap getMap(int column, int row);

    Object getMapPacket(VersionAdapter versionAdapter, int column, int row, boolean full);

    MapGraphics<? extends MapAccess> getGraphics();

    /**
     * Get the width of this map object.
     * <p>
     * This is not to be confused with the pixel widths, which would be <pre>getWidth() * 128</pre>.
     *
     * @return The width
     */
    int getWidth();

    /**
     * Get the height of this map object.
     * <p>
     * This is not to be confused with the pixel height, which would be <pre>getHeight() * 128</pre>.
     *
     * @return The width
     */
    int getHeight();
}
