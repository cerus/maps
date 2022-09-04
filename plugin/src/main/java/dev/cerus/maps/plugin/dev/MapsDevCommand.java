package dev.cerus.maps.plugin.dev;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.Marker;
import dev.cerus.maps.api.font.FontConverter;
import dev.cerus.maps.api.font.MapFont;
import dev.cerus.maps.api.graphics.ColorCache;
import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.plugin.MapsPlugin;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.raycast.RayCastUtil;
import java.awt.Font;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Special command for development operations, do not use in production
 */
@CommandAlias("mapsdev")
@CommandPermission("maps.devcommand")
public class MapsDevCommand extends BaseCommand {

    @Dependency
    private VersionAdapter versionAdapter;

    @Subcommand("fonttest")
    public void handleFontTest(final Player player, final int screenId, final String fontName, final int fontSize, String text) {
        final MapScreen screen = MapScreenRegistry.getScreen(screenId);
        if (screen == null || !fontName.matches("[A-Za-z0-9]+")) {
            return;
        }

        final MapFont mapFont;
        try {
            final Font font = Font.createFont(Font.TRUETYPE_FONT, new File(JavaPlugin.getPlugin(MapsPlugin.class).getDataFolder(), fontName + ".ttf"))
                    .deriveFont((float) fontSize);
            mapFont = FontConverter.convert(font, FontConverter.ASCII + FontConverter.UMLAUTS + FontConverter.SHARP_S + " ");
        } catch (final Throwable t) {
            t.printStackTrace();
            player.sendMessage("Error " + t.getMessage());
            return;
        }

        final MapGraphics<MapScreen, ClientsideMap[][]> graphics = screen.getGraphics();
        graphics.fillComplete((byte) 0);
        text = text.replace("\\n", "\n");
        final int width = Arrays.stream(text.split("\n")).mapToInt(mapFont::getWidth).max().orElse(0);
        final int height = Arrays.stream(text.split("\n")).mapToInt(mapFont::getHeight).sum();
        graphics.drawText(
                (graphics.getWidth() / 2) - (width / 2),
                (graphics.getHeight() / 2) - (height / 2),
                text,
                mapFont,
                ColorCache.rgbToMap(255, 255, 255),
                1
        );
        screen.sendMaps(true);

        player.sendMessage("W: " + width);
        player.sendMessage("H: " + height);
    }

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
