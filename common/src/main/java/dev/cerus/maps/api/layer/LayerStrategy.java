package dev.cerus.maps.api.layer;

import org.bukkit.entity.Player;

public interface LayerStrategy {

    default void onViewerAdded(Player player) {
    }

    default void onViewerRemoved(Player player) {
    }

    ScreenLayer getLayerForViewer(Player viewer);
}
