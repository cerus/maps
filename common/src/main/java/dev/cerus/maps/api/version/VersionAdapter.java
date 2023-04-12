package dev.cerus.maps.api.version;

import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.api.Frame;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Interface for version specific code
 */
public interface VersionAdapter {

    /**
     * Make a packet to send a map to a player
     *
     * @param ignoreBounds If true, implementations should use the full buffer and ignore the bounds
     * @param map          The map
     *
     * @return The new packet
     */
    Object makeMapPacket(boolean ignoreBounds, ClientsideMap map);

    /**
     * Make a frame update packet to display fake items
     *
     * @param frameId The id of the frame
     * @param map     The map to display
     *
     * @return The new packet
     */
    Object makeFramePacket(int frameId, boolean visible, ClientsideMap map);

    default Object makeFramePacket(final int frameId, final ClientsideMap map) {
        return this.makeFramePacket(frameId, true, map);
    }

    /**
     * Make a frame spawn packet
     *
     * @param frame The frame to spawn
     *
     * @return The spawn packet
     */
    Object makeFrameSpawnPacket(Frame frame);

    /**
     * Make a frame despawn packet
     *
     * @param frame The frame to despawn
     *
     * @return The despawn packet
     */
    Object makeFrameDespawnPacket(Frame frame);

    /**
     * Send a packet to a playe
     *
     * @param player The player
     * @param packet The packet
     */
    void sendPacket(Player player, Object packet);

    /**
     * Inject a packet listener into the players connection
     *
     * @param player   The player to inject
     * @param listener The listener
     */
    void inject(Player player, PacketListener listener, JavaPlugin plugin);

    /**
     * Spawn a barrier block marker particle
     *
     * @param player The receiver
     * @param loc    The location to spawn the particle at
     */
    void spawnBarrierParticle(Player player, Location loc);

}
