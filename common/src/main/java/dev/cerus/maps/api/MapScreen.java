package dev.cerus.maps.api;

import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.api.layer.DefaultLayerStrategy;
import dev.cerus.maps.api.layer.LayerStrategy;
import dev.cerus.maps.api.layer.ScreenLayer;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.util.HitBoxCalculatorUtil;
import dev.cerus.maps.util.PacketQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an array of clientside maps that form a screen
 */
public class MapScreen implements MapAccess {

    private final Set<UUID> viewers = new HashSet<>();
    private final Map<UUID, ScreenLayer> lastTrackedLayers = new HashMap<>();
    private final Map<String, ScreenLayer> namedLayers = new HashMap<>();
    private final Set<ScreenLayer> drawnLayers = new HashSet<>();

    private final int id;
    private final VersionAdapter versionAdapter;
    private final List<ScreenLayer> layers;
    private final int width;
    private final int height;
    private int refreshRate;
    private HitBox hitBox;
    private LayerStrategy layerStrategy;
    private Frame[][] frames;
    private Location location;
    private int activeLayerIndex;

    public MapScreen(int id, VersionAdapter versionAdapter, int w, int h) {
        this(id, versionAdapter, w, h, new ArrayList<>());
    }

    public MapScreen(int id, VersionAdapter versionAdapter, int w, int h, List<ScreenLayer> layers) {
        this.id = id;
        this.versionAdapter = versionAdapter;
        this.layers = layers;
        this.width = w;
        this.height = h;
        this.layerStrategy = new DefaultLayerStrategy(this);
        this.refreshRate = 1;
        addLayer();
    }

    public ScreenLayer addLayer() {
        return addLayer(null);
    }

    public ScreenLayer addLayer(String name) {
        if (namedLayers.containsKey(name)) {
            throw new IllegalArgumentException("Layer already exists: " + name);
        }
        ScreenLayer layer = new ScreenLayer(layers.size(), name, width, height);
        layers.add(layer);
        if (name != null) {
            namedLayers.put(name, layer);
        }
        return layer;
    }

    public void switchToLayer(int index) {
        if (index < 0 || index >= layers.size()) {
            throw new IllegalArgumentException("Invalid layer index: " + index);
        }
        activeLayerIndex = index;
        updateFrames(getActiveLayer(), viewers.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList());
        markFullyDirty();
    }

    public ScreenLayer getActiveLayer() {
        return layers.get(activeLayerIndex);
    }

    public ScreenLayer getLayer(int index) {
        if (index < 0 || index >= layers.size()) {
            throw new IllegalArgumentException("Invalid layer index: " + index);
        }
        return layers.get(index);
    }

    public ScreenLayer getOrCreateLayer(String name) {
        ScreenLayer layer = getLayer(name);
        if (layer != null) {
            return layer;
        }
        return addLayer(name);
    }

    public boolean removeLayer(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Layer name cannot be null");
        }
        namedLayers.remove(name);
        return layers.removeIf(layer -> name.equals(layer.getName()));
    }

    public ScreenLayer getLayer(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Layer name cannot be null");
        }
        return namedLayers.get(name);
    }

    public ScreenLayer getLayerForViewer(Player player) {
        return layerStrategy.getLayerForViewer(player);
    }

    public void addViewer(Player player) {
        addViewer(player, true);
    }

    public void addViewer(Player player, boolean sendData) {
        if (viewers.contains(player.getUniqueId())) {
            return;
        }
        viewers.add(player.getUniqueId());
        layerStrategy.onViewerAdded(player);
        if (sendData) {
            spawnFrames(getActiveLayer(), player);
            update(player, true);
        }
    }

    public void removeViewer(Player player) {
        removeViewer(player, true);
    }

    public void removeViewer(Player player, boolean sendData) {
        if (viewers.contains(player.getUniqueId())) {
            return;
        }
        viewers.remove(player.getUniqueId());
        lastTrackedLayers.remove(player.getUniqueId());
        layerStrategy.onViewerRemoved(player);
        if (sendData) {
            despawnFrames(player);
        }
    }

    /**
     * Add a marker
     *
     * @param marker The marker to add
     */
    public void addMarker(Marker marker) {
        getActiveLayer().addMarker(marker);
    }

    /**
     * Remove a marker
     *
     * @param marker The marker to remove
     */
    public void removeMarker(Marker marker) {
        getActiveLayer().removeMarker(marker);
    }

    /**
     * Remove all markers
     */
    public void clearMarkers() {
        getActiveLayer().clearMarkers();
    }

    public void fullUpdate() {
        update(false, true);
    }

    public void update() {
        update(true, false);
    }

    private void update(boolean resetDirty, boolean fullMap) {
        viewers.removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
        if (viewers.isEmpty()) {
            return;
        }

        Set<ScreenLayer> layersToReset = null;
        for (UUID viewerId : viewers) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer == null) {
                continue;
            }
            ScreenLayer viewerLayer = getLayerForViewer(viewer);
            if (!drawnLayers.contains(viewerLayer)) {
                viewerLayer.getGraphics().draw();
                drawnLayers.add(viewerLayer);
            }
            if (lastTrackedLayers.get(viewerId) != viewerLayer) {
                updateFrames(viewerLayer, Collections.singleton(viewer));
                lastTrackedLayers.put(viewerId, viewerLayer);
            }
            update(viewer, fullMap);
            if (resetDirty) {
                if (layersToReset == null) {
                    layersToReset = new HashSet<>();
                }
                layersToReset.add(viewerLayer);
            }
        }
        if (layersToReset != null) {
            layersToReset.forEach(this::resetDirty);
        }
        drawnLayers.clear();
    }

    private void update(Player player, boolean fullMap) {
        ScreenLayer layer = getLayerForViewer(player);
        PacketQueue queue = PacketQueue.create();
        forEachMap(layer, map -> {
            if (fullMap || map.getBoundsWidth() != 0 || map.getBoundsHeight() != 0 || map.hasDirtyMarkers()) {
                try {
                    queue.add(this.versionAdapter.makeMapPacket(fullMap, map));
                } catch (Throwable t) {
                    Bukkit.getLogger().log(Level.SEVERE, "Failed to make map packet - screen=%s layer=%d map=%d".formatted(getId(), getActiveLayerIndex(), map.getId()), new Throwable("Here", t));
                }
            }
        });
        queue.flush(versionAdapter, Collections.singleton(player));
    }

    public void resetDirty(ScreenLayer layer) {
        forEachMap(layer, map -> {
            map.setBoundsX(0);
            map.setBoundsY(0);
            map.setBoundsWidth(0);
            map.setBoundsHeight(0);
            map.setDirtyMarkers(false);
        });
    }

    /**
     * @param players
     *
     * @deprecated Use {@link MapScreen#updateFrames(ScreenLayer, Player...)} instead
     */
    @Deprecated(forRemoval = true)
    public void sendFrames(Player... players) {
        updateFrames(getActiveLayer(), players);
    }

    public void updateFrames(ScreenLayer layer, Player... players) {
        updateFrames(layer, Arrays.asList(players));
    }

    /**
     * Send the frame updates to the players
     *
     * @param players The receivers
     */
    public void updateFrames(ScreenLayer layer, Iterable<Player> players) {
        if (this.frames == null) {
            return;
        }

        PacketQueue queue = PacketQueue.create();
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                ClientsideMap map = layer.getMap(col, row);
                int frameId = this.frames[col][row].getEntityId();
                queue.add(this.versionAdapter.makeFramePacket(frameId, this.frames[col][row].isVisible(), map));
            }
        }
        queue.flush(versionAdapter, players);
    }

    public void spawnFrames(@Nullable ScreenLayer layer, Player... players) {
        spawnFrames(layer, Arrays.asList(players));
    }

    /**
     * Spawn the frames
     *
     * @param players The receivers
     */
    public void spawnFrames(@Nullable ScreenLayer layer, Iterable<Player> players) {
        PacketQueue queue = PacketQueue.create();
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                queue.add(this.versionAdapter.makeFrameSpawnPacket(this.frames[col][row]));
            }
        }
        queue.flush(versionAdapter, players);
        if (layer != null) {
            updateFrames(layer, players);
        }
    }

    public void despawnFrames(Player... players) {
        despawnFrames(Arrays.asList(players));
    }

    /**
     * Remove the frames
     *
     * @param players The receivers
     */
    public void despawnFrames(Iterable<Player> players) {
        PacketQueue queue = PacketQueue.create();
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                queue.add(this.versionAdapter.makeFrameDespawnPacket(this.frames[col][row]));
            }
        }
        queue.flush(versionAdapter, players);
    }

    /**
     * @param players
     *
     * @deprecated Use {@link MapScreen#despawnFrames(Player...)} instead
     */
    @Deprecated(forRemoval = true)
    public void destroyFrames(Player... players) {
        despawnFrames(players);
    }

    private void forEachMap(ScreenLayer layer, Consumer<ClientsideMap> action) {
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                action.accept(layer.getMap(col, row));
            }
        }
    }

    public int getId() {
        return this.id;
    }

    @Override
    public ClientsideMap getMap(int column, int row) {
        return getActiveLayer().getMap(column, row);
    }

    @Override
    public Object getMapPacket(VersionAdapter versionAdapter, int column, int row, boolean full) {
        return getActiveLayer().getMapPacket(versionAdapter, column, row, full);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @Override
    public MapGraphics<MapAccess> getGraphics() {
        return getActiveLayer().getGraphics();
    }

    public Frame[][] getFrames() {
        return this.frames;
    }

    public void setFrames(Frame[][] frames) {
        this.frames = frames;
        this.calculateHitBox();
    }

    public int[][] getFrameIds() {
        int[][] frameIds = new int[this.width][this.height];
        for (int x = 0; x < this.frames.length; x++) {
            for (int y = 0; y < this.frames[x].length; y++) {
                frameIds[x][y] = this.frames[x][y].getEntityId();
            }
        }
        return frameIds;
    }

    @Override
    public void markFullyDirty() {
        getActiveLayer().markFullyDirty();
    }

    @Override
    public void markDirty(int x, int y) {
        getActiveLayer().markDirty(x, y);
    }

    @Override
    public void clearDirty() {
        getActiveLayer().clearDirty();
    }

    public Collection<Marker> getMarkers() {
        return getActiveLayer().getMarkers();
    }

    public Location getLocation() {
        if (this.location == null && this.frames != null
            && this.frames.length > 0 && this.frames[0].length > 0) {
            Frame f = this.frames[0][0];
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

    public void setLocation(Location location) {
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

    public void setHitBox(HitBox hitBox) {
        this.hitBox = hitBox;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    public void setRefreshRate(int refreshRate) {
        this.refreshRate = Math.max(0, refreshRate);
    }

    public void setLayerStrategy(LayerStrategy layerStrategy) {
        this.layerStrategy = layerStrategy;
    }

    public int getActiveLayerIndex() {
        return activeLayerIndex;
    }

    public record HitBox(Location bottomLeft, Location topRight) {

        public boolean contains(Location loc) {
            return (loc.getX() >= Math.min(this.bottomLeft.getX(), this.topRight.getX()) && loc.getX() <= Math.max(this.bottomLeft.getX(), this.topRight.getX()))
                   && (loc.getY() >= Math.min(this.bottomLeft.getY(), this.topRight.getY()) && loc.getY() <= Math.max(this.bottomLeft.getY(), this.topRight.getY()))
                   && (loc.getZ() >= Math.min(this.bottomLeft.getZ(), this.topRight.getZ()) && loc.getZ() <= Math.max(this.bottomLeft.getZ(), this.topRight.getZ()));
        }

    }

}
