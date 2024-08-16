package dev.cerus.maps.plugin.listener;

import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.event.PlayerClickScreenEvent;
import dev.cerus.maps.api.version.PacketListener;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.plugin.MapsPlugin;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.raycast.RayCastUtil;
import dev.cerus.maps.triangulation.ScreenTriangulation;
import dev.cerus.maps.util.Vec2;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Injects a packet listener into players when they join
 */
public class PlayerListener implements Listener {

    private static final double MAX_DISTANCE_TO_SCREEN = 64 * 64;

    private final VersionAdapter versionAdapter;
    private final double maxRayLength;
    private final boolean useTriangulation;

    public PlayerListener(final VersionAdapter versionAdapter, final boolean useTriangulation, final double maxRayLength) {
        this.versionAdapter = versionAdapter;
        this.useTriangulation = useTriangulation;
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
        Optional<RayCastUtil.Result> resultOptional = Optional.empty();
        if (this.useTriangulation) {
            final Location playerLoc = player.getLocation();
            for (final MapScreen screen : MapScreenRegistry.getScreens()) {
                if (player.getWorld() != screen.getLocation().getWorld()) {
                    continue;
                }
                final double dist = playerLoc.distanceSquared(screen.getLocation());
                if (dist < MAX_DISTANCE_TO_SCREEN) { // Only run calculations for screens in range
                    final Vec2 coords = ScreenTriangulation.triangulateScreenCoords(player, screen);
                    if (coords != null) {
                        resultOptional = Optional.of(new RayCastUtil.Result(screen, coords.x, coords.y));
                        break;
                    }
                }
            }
        } else {
            resultOptional = RayCastUtil.getTargetedScreen(player, this.maxRayLength, MapScreenRegistry.getScreens());
        }
        if (resultOptional.isEmpty()) {
            return false;
        }

        final RayCastUtil.Result result = resultOptional.get();
        final PlayerClickScreenEvent event = new PlayerClickScreenEvent(player,
                !Bukkit.isPrimaryThread(),
                result.targetScreen(),
                new Vec2(result.screenX(), result.targetScreen().getHeight() * 128 - result.screenY()),
                rightClick);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }

}
