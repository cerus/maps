package dev.cerus.maps.api;

import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.api.version.VersionAdapter;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;

public class ClientsideMap {

    private static final int WIDTH = 128;
    private static int COUNTER = Integer.MIN_VALUE;

    private final int id;
    private final byte[] data;
    private final List<MapCursor> cursors;
    private int x;
    private int y;
    private int width;
    private int height;

    public ClientsideMap() {
        this(COUNTER++);
    }

    public ClientsideMap(final int id) {
        this.id = id;
        this.data = new byte[WIDTH * WIDTH];
        this.cursors = new ArrayList<>();
    }

    public void sendTo(final VersionAdapter versionAdapter, final Player player) {
        this.sendTo(versionAdapter, false, player);
    }

    public void sendTo(final VersionAdapter versionAdapter, final boolean ignoreBounds, final Player player) {
        versionAdapter.sendPacket(player, versionAdapter.makeMapPacket(ignoreBounds, this));
    }

    public void draw(final MapGraphics<ClientsideMap, ?> graphics) {
        graphics.draw(this, null);
    }

    public void addCursor(final MapCursor cursor) {
        this.cursors.add(cursor);
    }

    public void removeCursor(final MapCursor cursor) {
        this.cursors.remove(cursor);
    }

    public void clearCursors() {
        this.cursors.clear();
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

    public List<MapCursor> getCursors() {
        return List.copyOf(this.cursors);
    }

}
