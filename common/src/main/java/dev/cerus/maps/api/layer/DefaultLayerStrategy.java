package dev.cerus.maps.api.layer;

import dev.cerus.maps.api.MapScreen;
import org.bukkit.entity.Player;

public class DefaultLayerStrategy implements LayerStrategy {
    private final MapScreen parent;

    public DefaultLayerStrategy(MapScreen parent) {
        this.parent = parent;
    }

    @Override
    public ScreenLayer getLayerForViewer(Player viewer) {
        return parent.getActiveLayer();
    }
}
