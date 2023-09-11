package dev.cerus.maps.api;

import dev.cerus.maps.api.graphics.FastMapScreenGraphics;
import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.api.graphics.MapScreenGraphics;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.util.HitBoxCalculatorUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Represents an array of clientside maps that form a screen
 */
public class MapScreen {

    private final int id;
    private final VersionAdapter versionAdapter;
    private final ClientsideMap[][] mapArray;
    private final int width;
    private final int height;
    private HitBox hitBox;
    private MapGraphics<MapScreen, ClientsideMap[][]> graphics;
    private Frame[][] frames;
    private Location location;

    public MapScreen(final int id, final VersionAdapter versionAdapter, final int w, final int h) {
        this.id = id;
        this.versionAdapter = versionAdapter;
        this.mapArray = new ClientsideMap[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                this.mapArray[x][y] = new ClientsideMap();
            }
        }
        this.graphics = new MapScreenGraphics(w, h);
        this.width = w;
        this.height = h;
    }

    public MapScreen(final int id, final VersionAdapter versionAdapter, final int w, final int h, final ClientsideMap[][] mapArray) {
        this.id = id;
        this.versionAdapter = versionAdapter;
        this.mapArray = mapArray;
        this.graphics = new MapScreenGraphics(w, h);
        this.width = w;
        this.height = h;
    }

    /**
     * Add a marker
     *
     * @param marker The marker to add
     */
    public void addMarker(final Marker marker) {
        final int arrX = marker.getX() / 256;
        final int arrY = marker.getY() / 256;
        if (arrX < this.width && arrY < this.height) {
            this.mapArray[arrX][arrY].addMarker(marker);
        }
    }

    /**
     * Remove a marker
     *
     * @param marker The marker to remove
     */
    public void removeMarker(final Marker marker) {
        final int arrX = marker.getX() / 256;
        final int arrY = marker.getY() / 256;
        if (arrX < this.width && arrY < this.height) {
            this.mapArray[arrX][arrY].removeMarker(marker);
        }
    }

    /**
     * Remove all markers
     */
    public void clearMarkers() {
        for (final ClientsideMap[] array : this.mapArray) {
            for (final ClientsideMap clientsideMap : array) {
                clientsideMap.clearMarkers();
            }
        }
    }

    /**
     * Send the map data to all online players
     *
     * @param full True if full map data should be sent
     */
    public void sendMaps(final boolean full) {
        this.sendMaps(full, Bukkit.getOnlinePlayers());
    }

    /**
     * Send the map data to the specified players
     *
     * @param full    True if full map data should be sent
     * @param players The receivers
     */
    public void sendMaps(final boolean full, final Collection<? extends Player> players) {
        this.sendMaps(full, players.toArray(Player[]::new));
    }

    /**
     * Send the map data to the specified players
     *
     * @param full    True if full map data should be sent
     * @param players The receivers
     */
    public void sendMaps(final boolean full, final Player... players) {
        // Render the buffer
        this.graphics.renderOnto(this, this.mapArray);

        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                final ClientsideMap map = this.mapArray[x][y];
                // If bounds are zero there are no changes
                if (!full && map.getX() == 0 && map.getY() == 0 && map.getWidth() == 0
                    && map.getHeight() == 0 && !map.hasDirtyMarkers()) {
                    continue;
                }

                final Object packet = this.versionAdapter.makeMapPacket(full, map);
                for (final Player player : players) {
                    this.versionAdapter.sendPacket(player, packet);
                }
                map.setDirtyMarkers(false);
            }
        }
    }

    public void sendMarkers(final Player... players) {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                final ClientsideMap map = this.mapArray[x][y];
                // If bounds are zero there are no changes
                if (!map.hasDirtyMarkers()) {
                    continue;
                }

                map.setX(0);
                map.setY(0);
                map.setWidth(0);
                map.setHeight(0);

                final Object packet = this.versionAdapter.makeMapPacket(false, map);
                for (final Player player : players) {
                    this.versionAdapter.sendPacket(player, packet);
                }
                map.setDirtyMarkers(false);
            }
        }
    }

    /**
     * Send the frame updates to the players
     *
     * @param players The receivers
     */
    public void sendFrames(final Player... players) {
        if (this.frames == null) {
            return;
        }

        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                final ClientsideMap map = this.mapArray[x][y];
                final int frameId = this.frames[x][y].getEntityId();

                for (final Player player : players) {
                    this.versionAdapter.sendPacket(player, this.versionAdapter.makeFramePacket(frameId, this.frames[x][y].isVisible(), map));
                }
            }
        }
    }

    /**
     * Spawn the frames
     *
     * @param players The receivers
     */
    public void spawnFrames(final Player... players) {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                final ClientsideMap map = this.mapArray[x][y];
                final int frameId = this.frames[x][y].getEntityId();

                for (final Player player : players) {
                    this.versionAdapter.sendPacket(player, this.versionAdapter.makeFrameSpawnPacket(this.frames[x][y]));
                    this.versionAdapter.sendPacket(player, this.versionAdapter.makeFramePacket(frameId, this.frames[x][y].isVisible(), map));
                }
            }
        }
    }

    /**
     * Remove the frames
     *
     * @param players The receivers
     */
    public void despawnFrames(final Player... players) {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                for (final Player player : players) {
                    this.versionAdapter.sendPacket(player, this.versionAdapter.makeFrameDespawnPacket(this.frames[x][y]));
                }
            }
        }
    }

    public void destroyFrames(final Player... players) {
        this.despawnFrames(players);
    }

    public int getId() {
        return this.id;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void useFastGraphics(final boolean b) {
        this.graphics = b ? new FastMapScreenGraphics(this.width, this.height)
                : new MapScreenGraphics(this.width, this.height);
    }

    public MapGraphics<MapScreen, ClientsideMap[][]> getGraphics() {
        return this.graphics;
    }

    public Frame[][] getFrames() {
        return this.frames;
    }

    public void setFrames(final Frame[][] frames) {
        this.frames = frames;
        this.calculateHitBox();
    }

    public int[][] getFrameIds() {
        final int[][] frameIds = new int[this.width][this.height];
        for (int x = 0; x < this.frames.length; x++) {
            for (int y = 0; y < this.frames[x].length; y++) {
                frameIds[x][y] = this.frames[x][y].getEntityId();
            }
        }
        return frameIds;
    }

    @Deprecated
    public void setFrameIds(final int[][] frameIds) {
        //this.frameIds = frameIds;
        Logger.getLogger("maps").warning("One of your plugin has called the deprecated MapScreen#setFrameIds() method.");
        Logger.getLogger("maps").warning("Please update your plugins to maps v3!");
    }

    public Collection<Marker> getMarkers() {
        return Arrays.stream(this.mapArray)
                .flatMap(Arrays::stream)
                .flatMap(clientsideMap -> clientsideMap.getMarkers().stream())
                .toList();
    }

    public Location getLocation() {
        if (this.location == null && this.frames != null
            && this.frames.length > 0 && this.frames[0].length > 0) {
            final Frame f = this.frames[0][0];
            this.location = new Location(f.getWorld(), f.getPosX(), f.getPosY(), f.getPosZ());
            switch (f.getFacing()) {
                case NORTH -> {
                    this.location.setX(this.location.getBlockX() + 0.99d);
                    this.location.setZ(this.location.getBlockZ() + 0.99d);
                }
                case EAST -> this.location.setZ(this.location.getBlockZ() + 0.99d);
                case WEST -> this.location.setX(this.location.getBlockX() + 0.99d);
            }
        }
        return this.location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    /**
     * Recalculate the hit box of this screen
     */
    public void calculateHitBox() {
        this.hitBox = HitBoxCalculatorUtil.calculateHitBox(this);
    }

    public HitBox getHitBox() {
        return this.hitBox;
    }

    public void setHitBox(final HitBox hitBox) {
        this.hitBox = hitBox;
    }

    public record HitBox(Location bottomLeft, Location topRight) {

        public boolean contains(final Location loc) {
            return (loc.getX() >= Math.min(this.bottomLeft.getX(), this.topRight.getX()) && loc.getX() <= Math.max(this.bottomLeft.getX(), this.topRight.getX()))
                   && (loc.getY() >= Math.min(this.bottomLeft.getY(), this.topRight.getY()) && loc.getY() <= Math.max(this.bottomLeft.getY(), this.topRight.getY()))
                   && (loc.getZ() >= Math.min(this.bottomLeft.getZ(), this.topRight.getZ()) && loc.getZ() <= Math.max(this.bottomLeft.getZ(), this.topRight.getZ()));
        }

    }

}
