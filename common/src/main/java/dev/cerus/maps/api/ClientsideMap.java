package dev.cerus.maps.api;

import dev.cerus.maps.api.graphics.ClientsideMapGraphics;
import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.api.version.VersionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

/**
 * A map that is completely clientside
 */
public class ClientsideMap implements MapAccess {

    private static final int WIDTH = 128;
    private static int COUNTER = Integer.MIN_VALUE;

    private final int id;
    private final byte[] data;
    private final List<Marker> markers;
    private int boundsX;
    private int boundsY;
    private int boundsWidth;
    private int boundsHeight;
    private boolean dirtyMarkers;

    public ClientsideMap() {
        this(COUNTER++);
    }

    public ClientsideMap(int id) {
        this.id = id;
        this.data = new byte[WIDTH * WIDTH];
        this.markers = new ArrayList<>();
    }

    /**
     * Creates a new graphics object for this clientside map.
     * <p>
     * The result of this method call should be stored. It is a waste of resources to constantly allocate and destroy {@link MapGraphics} objects.
     *
     * @return a newly allocated {@link MapGraphics} instance
     */
    public MapGraphics<ClientsideMap> createGraphics() {
        return new ClientsideMapGraphics(this);
    }

    /**
     * Send this map to a player
     *
     * @param versionAdapter The version adapter
     * @param player         The player
     */
    public void sendTo(VersionAdapter versionAdapter, Player player) {
        this.sendTo(versionAdapter, false, player);
    }

    /**
     * Send this map to a player
     *
     * @param versionAdapter The version adapter
     * @param ignoreBounds   True if the whole map should be sent
     * @param player         The player
     */
    public void sendTo(VersionAdapter versionAdapter, boolean ignoreBounds, Player player) {
        versionAdapter.sendPacket(player, versionAdapter.makeMapPacket(ignoreBounds, this));
    }

    /**
     * Add a marker
     *
     * @param marker The marker to add
     */
    public void addMarker(Marker marker) {
        this.markers.add(marker);
        marker.setParent(this);
        this.dirtyMarkers = true;
    }

    /**
     * Remove a marker
     *
     * @param marker The marker to remove
     */
    public void removeMarker(Marker marker) {
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

    public synchronized byte[] getData() {
        return this.data;
    }

    public int getBoundsX() {
        return Math.max(0, this.boundsX);
    }

    public void setBoundsX(int boundsX) {
        this.boundsX = boundsX;
    }

    public int getBoundsY() {
        return Math.max(0, this.boundsY);
    }

    public void setBoundsY(int boundsY) {
        this.boundsY = boundsY;
    }

    public int getBoundsWidth() {
        return boundsWidth;
    }

    public void setBoundsWidth(int boundsWidth) {
        this.boundsWidth = boundsWidth;
    }

    public int getBoundsHeight() {
        return boundsHeight;
    }

    public void setBoundsHeight(int boundsHeight) {
        this.boundsHeight = boundsHeight;
    }

    public List<Marker> getMarkers() {
        return List.copyOf(this.markers);
    }

    public boolean hasDirtyMarkers() {
        return this.dirtyMarkers;
    }

    public void setDirtyMarkers(boolean dirtyMarkers) {
        this.dirtyMarkers = dirtyMarkers;
    }

    @Override
    public void markFullyDirty() {
        setBoundsX(0);
        setBoundsY(0);
        setBoundsWidth(128);
        setBoundsHeight(128);
    }

    @Override
    public void markDirty(int x, int y) {
        if (x < boundsX || boundsX < 0) {
            setBoundsX(x);
        }
        if (y < boundsY || boundsY < 0) {
            setBoundsY(y);
        }
        if (x >= boundsX + boundsWidth) {
            setBoundsWidth(x - boundsX + 1);
        }
        if (y >= boundsY + boundsHeight) {
            setBoundsHeight(y - boundsY + 1);
        }
    }

    @Override
    public void clearDirty() {
        setBoundsX(-1);
        setBoundsY(-1);
        setBoundsWidth(0);
        setBoundsHeight(0);
    }

    @Override
    public ClientsideMap getMap(int column, int row) {
        return this;
    }

    @Override
    public Object getMapPacket(VersionAdapter versionAdapter, int column, int row, boolean full) {
        if (!full && getBoundsHeight() == 0 && getBoundsWidth() == 0) {
            return null;
        }
        return versionAdapter.makeMapPacket(full, this);
    }

    @Override
    @Deprecated
    public MapGraphics<? extends MapAccess> getGraphics() {
        return createGraphics();
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 1;
    }
}
