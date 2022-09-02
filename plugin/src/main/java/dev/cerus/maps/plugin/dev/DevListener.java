package dev.cerus.maps.plugin.dev;

import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.Marker;
import dev.cerus.maps.api.event.PlayerClickScreenEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.map.MapCursor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Special listener for development operations, do not use in production
 */
public class DevListener implements Listener {

    private final JavaPlugin plugin;

    public DevListener(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(final PlayerClickScreenEvent event) {
        final MapScreen screen = event.getClickedScreen();
        if (!DevContext.DEV_SCREENS.contains(screen.getId())) {
            return;
        }

        final Marker marker = new Marker(
                event.getClickPos().x * 2 + 2,
                (screen.getHeight() * 128 - event.getClickPos().y) * 2 - 2,
                (byte) 0, event.isRightClick() ? MapCursor.Type.WHITE_CROSS : MapCursor.Type.RED_X, true,
                new TextComponent(event.getClickPos().x + ", " + event.getClickPos().y)
        );
        screen.addMarker(marker);
        screen.sendMarkers(event.getPlayer());
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            screen.removeMarker(marker);
            screen.sendMarkers(event.getPlayer());
        }, 5 * 20);
        screen.sendMarkers(event.getPlayer());
        event.setCancelled(true);
    }

}
