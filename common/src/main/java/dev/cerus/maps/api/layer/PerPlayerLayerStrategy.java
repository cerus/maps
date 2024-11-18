package dev.cerus.maps.api.layer;

import dev.cerus.maps.api.MapScreen;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class PerPlayerLayerStrategy implements LayerStrategy {
    private final Map<UUID, Integer> indexMap = new HashMap<>();
    private final MapScreen parent;

    public PerPlayerLayerStrategy(MapScreen parent) {
        this.parent = parent;
    }

    @Override
    public ScreenLayer getLayerForViewer(Player viewer) {
        int layerIndex = indexMap.computeIfAbsent(viewer.getUniqueId(), $ -> parent.addLayer().getIndex());
        return parent.getLayer(layerIndex);
    }
}
