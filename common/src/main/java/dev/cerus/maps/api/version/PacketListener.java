package dev.cerus.maps.api.version;

import org.bukkit.entity.Player;

/**
 * Interceptor for packets
 */
public interface PacketListener {

    /**
     * Intercepts left clicks
     *
     * @param player The acting player
     *
     * @return True if this click should be cancelled
     */
    boolean handlePlayerLeftClick(Player player);

    /**
     * Intercepts right clicks
     *
     * @param player The acting player
     *
     * @return True if this click should be cancelled
     */
    boolean handlePlayerRightClick(Player player);

}
