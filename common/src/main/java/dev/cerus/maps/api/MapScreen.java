package dev.cerus.maps.api;

import dev.cerus.maps.api.graphics.MapScreenGraphics;
import dev.cerus.maps.api.version.VersionAdapter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MapScreen {

    private final VersionAdapter versionAdapter;
    private final ClientsideMap[][] mapArray;
    private final MapScreenGraphics graphics;
    private final int width;
    private final int height;
    private byte[][][] previousData;
    private int[][] frameIds;

    public MapScreen(final VersionAdapter versionAdapter, final int w, final int h) {
        this.versionAdapter = versionAdapter;
        this.mapArray = new ClientsideMap[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                this.mapArray[x][y] = new ClientsideMap();
            }
        }
        this.graphics = new MapScreenGraphics(w, h, this.mapArray);
        this.width = w;
        this.height = h;
    }

    public MapScreen(final VersionAdapter versionAdapter, final int w, final int h, final ClientsideMap[][] mapArray) {
        this.versionAdapter = versionAdapter;
        this.mapArray = mapArray;
        this.graphics = new MapScreenGraphics(w, h, mapArray);
        this.width = w;
        this.height = h;
    }

    public void update(final DirtyHandlingPolicy policy) {
        this.update(policy, Bukkit.getOnlinePlayers());
    }

    public void update(final DirtyHandlingPolicy policy, final Collection<? extends Player> players) {
        this.update(policy, players.toArray(Player[]::new));
    }

    public void update(final DirtyHandlingPolicy policy, final Player... players) {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                final ClientsideMap map = this.mapArray[x][y];
                if (policy != DirtyHandlingPolicy.IGNORE && this.previousData != null) {
                    if (Arrays.equals(map.getGraphics().getData(), this.previousData[x][y])) {
                        continue;
                    }
                } else if (!map.getGraphics().isDirty() && policy == DirtyHandlingPolicy.SEND_DIRTY_ONLY) {
                    continue;
                }

                final Set<Object> packets = new HashSet<>();
                packets.add(this.versionAdapter.makeMapPacket(map));

                if (this.frameIds != null) {
                    final int id = this.frameIds[x][y];
                    packets.add(this.versionAdapter.makeFramePacket(id, map));
                }

                for (final Player player : players) {
                    packets.forEach(o -> this.versionAdapter.sendPacket(player, o));
                }

                if (this.previousData != null) {
                    this.previousData[x][y] = Arrays.copyOf(map.getGraphics().getData(), map.getGraphics().getData().length);
                }
            }
        }
    }

    public void sendFramesOnly(final Player... players) {
        if (this.frameIds == null) {
            return;
        }

        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                final ClientsideMap map = this.mapArray[x][y];
                final int frameId = this.frameIds[x][y];

                for (final Player player : players) {
                    this.versionAdapter.sendPacket(player, this.versionAdapter.makeFramePacket(frameId, map));
                }
            }
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public MapScreenGraphics getGraphics() {
        return this.graphics;
    }

    public int[][] getFrameIds() {
        return this.frameIds;
    }

    public void setFrameIds(final int[][] frameIds) {
        this.frameIds = frameIds;
    }

    public void enableAdvancedContentChangeAlgorithm() {
        this.previousData = new byte[this.width][this.height][128 * 128];
    }

    public void disableAdvancedContentChangeAlgorithm() {
        this.previousData = null;
    }

    public boolean isAdvancedContentChangeAlgorithmEnabled() {
        return this.previousData != null;
    }

    public enum DirtyHandlingPolicy {
        /**
         * Only send maps that were marked as dirty (maps whose content changed)
         * Can save bandwidth and time
         */
        SEND_DIRTY_ONLY,

        /**
         * Send dirty and clean maps
         */
        IGNORE
    }

}
