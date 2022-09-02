package dev.cerus.maps.plugin.dev;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.Marker;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.raycast.RayCastUtil;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;

/**
 * Special command for development operations, do not use in production
 */
@CommandAlias("mapsdev")
@CommandPermission("maps.devcommand")
public class MapsDevCommand extends BaseCommand {

    @Dependency
    private VersionAdapter versionAdapter;

    @Subcommand("raytesttask")
    public void handleRayTestTask(final Player player) {
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new Runnable() {
            private MapScreen prev;

            @Override
            public void run() {
                if (player.isSneaking()) {
                    executor.shutdown();
                    if (this.prev != null) {
                        this.prev.sendMarkers(player);
                    }
                    player.sendMessage("Cancel");
                    return;
                }

                final Optional<RayCastUtil.Result> resultOpt = RayCastUtil.getTargetedScreen(player, 10, MapScreenRegistry.getScreens());
                if (resultOpt.isEmpty()) {
                    if (this.prev != null) {
                        this.prev.sendMarkers(player);
                        this.prev = null;
                    }
                } else {
                    final RayCastUtil.Result result = resultOpt.get();
                    final MapScreen screen = result.targetScreen();
                    final Marker marker = new Marker(
                            result.screenX() * 2 + 4,
                            (result.targetScreen().getHeight() * 128 - result.screenY()) * 2,
                            (byte) 14, MapCursor.Type.BANNER_WHITE, true
                    );
                    screen.addMarker(marker);
                    screen.sendMarkers(player);
                    screen.removeMarker(marker);

                    if (this.prev != screen && this.prev != null) {
                        this.prev.sendMarkers(player);
                    }
                    this.prev = screen;
                }
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
        player.sendMessage("Start");
    }

    @Subcommand("raytest")
    @Conditions("")
    public void handleRayTest(final Player player, final int rot) {
        final List<MapScreen> screens = MapScreenRegistry.getScreens().stream()
                .peek(MapScreen::calculateHitBox)
                .filter(s -> s.getHitBox() != null)
                .toList();
        for (final MapScreen screen : screens) {
            final MapScreen.HitBox box = screen.getHitBox();
            screen.getLocation().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, box.bottomLeft().getX(), box.bottomLeft().getY(), box.bottomLeft().getZ(), 1, 0, 0, 0, 0);
            screen.getLocation().getWorld().spawnParticle(Particle.END_ROD, box.topRight().getX(), box.topRight().getY(), box.topRight().getZ(), 1, 0, 0, 0, 0);
        }

        final long now = System.nanoTime();
        final Optional<RayCastUtil.Result> resultOpt = RayCastUtil.getTargetedScreen(player, 10, screens);
        resultOpt.ifPresent(result -> {
            final MapScreen screen = result.targetScreen();
            screen.clearMarkers();
            screen.addMarker(new Marker(
                    result.screenX() * 2 + 4,
                    (result.targetScreen().getHeight() * 128 - result.screenY()) * 2,
                    (byte) rot, MapCursor.Type.BANNER_WHITE, true)
            );
            screen.sendMarkers(player);
        });
        final long diff = System.nanoTime() - now;
        player.sendMessage(diff + "ns (" + String.format("%.4f", ((double) diff) / ((double) TimeUnit.MILLISECONDS.toNanos(1))) + "ms)");
    }

    @Subcommand("devscreen add")
    public void handleDevScreenAdd(final Player player, final int screen) {
        DevContext.DEV_SCREENS.add(screen);
        player.sendMessage(screen + " added");
    }

    @Subcommand("devscreen remove")
    public void handleDevScreenRemove(final Player player, final int screen) {
        DevContext.DEV_SCREENS.remove(screen);
        player.sendMessage(screen + " removed");
    }

}
