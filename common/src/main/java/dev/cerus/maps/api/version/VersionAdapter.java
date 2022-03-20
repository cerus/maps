package dev.cerus.maps.api.version;

import dev.cerus.maps.api.ClientsideMap;
import org.bukkit.entity.Player;

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
    Object makeFramePacket(int frameId, ClientsideMap map);

    /**
     * Send a packet to a playe
     *
     * @param player The player
     * @param packet The packet
     */
    void sendPacket(Player player, Object packet);

}
