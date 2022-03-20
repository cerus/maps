package dev.cerus.maps.api;

import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.api.version.VersionAdapter;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

/**
 * A map that is completely clientside
 */
public class ClientsideMap {

    private static final int WIDTH = 128;
    private static int COUNTER = Integer.MIN_VALUE;

    private final int id;
    private final byte[] data;
    private final List<Marker> markers;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean dirtyMarkers;

    public ClientsideMap() {
        this(COUNTER++);
    }

    public ClientsideMap(final int id) {
        this.id = id;
        this.data = new byte[WIDTH * WIDTH];
        this.markers = new ArrayList<>();
    }

    /**
     * Send this map to a player
     *
     * @param versionAdapter The version adapter
     * @param player         The player
     */
    public void sendTo(final VersionAdapter versionAdapter, final Player player) {
        this.sendTo(versionAdapter, false, player);
    }

    /**
     * Send this map to a player
     *
     * @param versionAdapter The version adapter
     * @param ignoreBounds   True if the whole map should be sent
     * @param player         The player
     */
    public void sendTo(final VersionAdapter versionAdapter, final boolean ignoreBounds, final Player player) {
        versionAdapter.sendPacket(player, versionAdapter.makeMapPacket(ignoreBounds, this));
    }

    /**
     * Draw a graphics buffer onto this map
     *
     * @param graphics The buffer
     */
    public void draw(final MapGraphics<ClientsideMap, ?> graphics) {
        graphics.renderOnto(this, null);
    }

    /**
     * Add a marker
     *
     * @param marker The marker to add
     */
    public void addMarker(final Marker marker) {
        this.markers.add(marker);
        marker.setParent(this);
        this.dirtyMarkers = true;
    }

    /**
     * Remove a marker
     *
     * @param marker The marker to remove
     */
    public void removeMarker(final Marker marker) {
        if (this.markers.remove(marker)) {
            this.dirtyMarkers = true;
        }
    }

    /**
     * Remove all markers
     */
    public void clearMarkers() {
        if (!this.markers.isEmpty()) {
            this.markers.clear();
            this.dirtyMarkers = true;
        }
    }

    public int getId() {
        return this.id;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getX() {
        return this.x;
    }

    public void setX(final int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(final int y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(final int height) {
        this.height = height;
    }

    public List<Marker> getMarkers() {
        return List.copyOf(this.markers);
    }

    public boolean hasDirtyMarkers() {
        return this.dirtyMarkers;
    }

    public void setDirtyMarkers(final boolean dirtyMarkers) {
        this.dirtyMarkers = dirtyMarkers;
    }

}
