package dev.cerus.maps.plugin.listener;

import dev.cerus.maps.api.event.PlayerClickScreenEvent;
import dev.cerus.maps.api.version.PacketListener;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.plugin.MapsPlugin;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.raycast.RayCastUtil;
import dev.cerus.maps.util.Vec2;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Injects a packet listener into players when they join
 */
public class PlayerListener implements Listener {

    private final VersionAdapter versionAdapter;
    private final double maxRayLength;

    public PlayerListener(final VersionAdapter versionAdapter, final double maxRayLength) {
        this.versionAdapter = versionAdapter;
        this.maxRayLength = maxRayLength;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        this.versionAdapter.inject(event.getPlayer(), new PacketListener() {
            @Override
            public boolean handlePlayerLeftClick(final Player player) {
                return PlayerListener.this.call(player, false);
            }

            @Override
            public boolean handlePlayerRightClick(final Player player) {
                return PlayerListener.this.call(player, true);
            }
        }, JavaPlugin.getPlugin(MapsPlugin.class));
    }

    private boolean call(final Player player, final boolean rightClick) {
        final Optional<RayCastUtil.Result> resultOptional = RayCastUtil.getTargetedScreen(player, this.maxRayLength, MapScreenRegistry.getScreens());
        if (resultOptional.isEmpty()) {
            return false;
        }

        final RayCastUtil.Result result = resultOptional.get();
        final PlayerClickScreenEvent event = new PlayerClickScreenEvent(player,
                !Bukkit.isPrimaryThread(),
                result.targetScreen(),
                new Vec2(result.screenX(), result.screenY()),
                rightClick);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }

}
