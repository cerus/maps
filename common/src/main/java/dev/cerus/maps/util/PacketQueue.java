package dev.cerus.maps.util;

import dev.cerus.maps.api.version.VersionAdapter;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;

public class PacketQueue {
    private final Set<Object> packets = new HashSet<>();

    public static PacketQueue create() {
        return new PacketQueue();
    }

    public PacketQueue add(Object packet) {
        packets.add(packet);
        return this;
    }

    public void flush(VersionAdapter versionAdapter, Iterable<Player> players) {
        Object bundledPackets = getBundledPackets(versionAdapter);
        for (Player player : players) {
            versionAdapter.sendPacket(player, bundledPackets);
            /*for (Object packet : packets) {
                versionAdapter.sendPacket(player, packet);
            }*/
        }
    }

    public Set<Object> getPackets() {
        return packets;
    }

    public Object getBundledPackets(VersionAdapter adapter) {
        return adapter.bundlePackets(packets);
    }
}
