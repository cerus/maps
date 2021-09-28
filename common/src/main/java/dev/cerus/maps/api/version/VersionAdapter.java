package dev.cerus.maps.api.version;

import dev.cerus.maps.api.ClientsideMap;
import org.bukkit.entity.Player;

public interface VersionAdapter {

    Object makeMapPacket(ClientsideMap map);

    Object makeFramePacket(int frameId, ClientsideMap map);

    void sendPacket(Player player, Object packet);

}
