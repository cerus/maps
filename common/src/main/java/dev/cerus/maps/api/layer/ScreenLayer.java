package dev.cerus.maps.api.layer;

import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.api.MapAccess;
import dev.cerus.maps.api.Marker;
import dev.cerus.maps.api.graphics.BasicMapGraphics;
import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.api.version.VersionAdapter;
import java.util.Arrays;
import java.util.Collection;
import org.bukkit.Bukkit;

public class ScreenLayer implements MapAccess {

    private final int index;
    private final String name;
    private final int width;
    private final int height;
    private final ClientsideMap[] maps;
    private final MapGraphics<MapAccess> graphics;
    private int boundsX;
    private int boundsY;
    private int boundsWidth;
    private int boundsHeight;

    public ScreenLayer(int index, String name, int width, int height) {
        this(index, name, width, height, createArray(width, height));
    }

    public ScreenLayer(int index, String name, int width, int height, ClientsideMap[] maps) {
        this.index = index;
        this.name = name;
        this.width = width;
        this.height = height;
        this.maps = maps;
        this.graphics = new BasicMapGraphics(this);
    }

    private static ClientsideMap[] createArray(int width, int height) {
        ClientsideMap[] maps = new ClientsideMap[width * height];
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                maps[col + row * width] = new ClientsideMap();
            }
        }
        return maps;
    }

    @Override
    public void markFullyDirty() {
        boundsX = boundsY = 0;
        boundsWidth = width * 128;
        boundsHeight = height * 128;
        recalculateBounds();
    }

    @Override
    public void markDirty(int x, int y) {
        if (x < 0 || y < 0 || x > width * 128 || y > height * 128) {
            throw new IllegalArgumentException("Invalid x or y: " + x + ", " + y);
        }
        if (x < boundsX || boundsX < 0) {
            boundsX = x;
        }
        if (y < boundsY || boundsY < 0) {
            boundsY = y;
        }
        if (x >= boundsX + boundsWidth) {
            boundsWidth = x - boundsX + 1;
        }
        if (y >= boundsY + boundsHeight) {
            boundsHeight = y - boundsY + 1;
        }
        recalculateBounds();
    }

    private void recalculateBounds() {
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                getMap(col, row).clearDirty();
                if ((boundsY > row * 128 + 127 || boundsY + boundsHeight < row * 128)
                    || (boundsX > col * 128 + 127 || boundsX + boundsWidth < col * 128)) {
                    continue;
                }

                int mapBoundsX = Math.max(boundsX - col * 128, 0);
                int mapBoundsY = Math.max(boundsY - row * 128, 0);
                int mapBoundsWidth = Math.max(Math.min(boundsX + boundsWidth - col * 128, 128 - mapBoundsX), 0);
                int mapBoundsHeight = Math.max(Math.min(boundsY + boundsHeight - row * 128, 128 - mapBoundsY), 0);

                ClientsideMap map = getMap(col, row);
                map.setBoundsX(mapBoundsX);
                map.setBoundsY(mapBoundsY);
                map.setBoundsWidth(mapBoundsWidth);
                map.setBoundsHeight(mapBoundsHeight);
            }
        }

        if (true) {
            return;
        }

        for (int col = boundsX / 128; col < (int) Math.min(width, Math.ceil((boundsX + boundsWidth) / 128d)); col++) {
            for (int row = boundsY / 128; row < (int) Math.min(height, Math.ceil((boundsY + boundsHeight) / 128d)); row++) {
                int mapBoundsX = Math.max(0, boundsX - (col * 128));
                int mapBoundsY = Math.max(0, boundsY - (row * 128));
                int mapBoundsWidth = Math.min(128 - mapBoundsX, (boundsX + boundsWidth) - (col * 128));
                int mapBoundsHeight = Math.min(128 - mapBoundsY, (boundsY + boundsHeight) - (row * 128));

                //Bukkit.getLogger().info("DEBUG: " + col+","+row+": x="+mapBoundsX + " y="+mapBoundsY + " w="+mapBoundsWidth + " h="+mapBoundsHeight);

                ClientsideMap map = getMap(col, row);
                map.setBoundsX(mapBoundsX);
                map.setBoundsY(mapBoundsY);
                map.setBoundsWidth(mapBoundsWidth);
                map.setBoundsHeight(mapBoundsHeight);
            }
        }
    }

    @Override
    public void clearDirty() {
        boundsX = boundsY = -1;
        boundsWidth = boundsHeight = 0;

        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                getMap(col, row).clearDirty();
            }
        }
    }

    @Override
    public void addMarker(Marker marker) {
        int col = marker.getX() / 256;
        int row = marker.getY() / 256;
        if (col < this.width && row < this.height) {
            getMap(col, row).addMarker(marker);
        }
    }

    @Override
    public void removeMarker(Marker marker) {
        int col = marker.getX() / 256;
        int row = marker.getY() / 256;
        if (col < this.width && row < this.height) {
            getMap(col, row).removeMarker(marker);
        }
    }

    @Override
    public void clearMarkers() {
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                getMap(col, row).clearMarkers();
            }
        }
    }

    @Override
    public Collection<Marker> getMarkers() {
        return Arrays.stream(maps)
                .flatMap(map -> map.getMarkers().stream())
                .toList();
    }

    @Override
    public ClientsideMap getMap(int column, int row) {
        return maps[column + row * width];
    }

    @Override
    public Object getMapPacket(VersionAdapter versionAdapter, int column, int row, boolean full) {
        return versionAdapter.makeMapPacket(full, getMap(column, row));
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public MapGraphics<MapAccess> getGraphics() {
        return graphics;
    }

    public String getName() {
        return name;
    }
}
