package dev.cerus.maps.api;

import dev.cerus.maps.api.graphics.ClientsideMapGraphics;
import dev.cerus.maps.api.version.VersionAdapter;
import org.bukkit.entity.Player;

public class ClientsideMap {

    private static int COUNTER = Integer.MIN_VALUE;

    private final int id;
    private final ClientsideMapGraphics graphics;

    public ClientsideMap() {
        this(COUNTER++);
    }

    public ClientsideMap(final int id) {
        this.id = id;
        this.graphics = new ClientsideMapGraphics();
    }

    public void sendTo(final VersionAdapter versionAdapter, final Player player) {
        versionAdapter.sendPacket(player, versionAdapter.makeMapPacket(this));
    }

    public ClientsideMapGraphics getGraphics() {
        return this.graphics;
    }

    public int getId() {
        return this.id;
    }

}
