package dev.cerus.maps.plugin.dev;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import dev.cerus.maps.api.MapAccess;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.Marker;
import dev.cerus.maps.api.colormap.ColorMap;
import dev.cerus.maps.api.colormap.ColorMaps;
import dev.cerus.maps.api.font.FontConverter;
import dev.cerus.maps.api.font.MapFont;
import dev.cerus.maps.api.graphics.ColorCache;
import dev.cerus.maps.api.graphics.MapColors;
import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.plugin.MapsPlugin;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.raycast.RayCastUtil;
import dev.cerus.maps.triangulation.ScreenTriangulation;
import dev.cerus.maps.util.Vec2;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Special command for development operations, do not use in production
 */
@CommandAlias("mapsdev")
@CommandPermission("maps.devcommand")
public class MapsDevCommand extends BaseCommand {

    @Dependency
    private VersionAdapter versionAdapter;

    @Subcommand("palette")
    public void handlePalette(Player player, String fileName) {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = image.createGraphics();
        java.awt.Color white = MapColors.color(MapColors.color(255, 255, 255));
        graphics.setColor(white);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.dispose();

        int x = 0;
        int y = 0;
        for (ColorMap.Color color : ColorMaps.current().getColors()) {
            if (color == null || color.mapColorInt() < 4) {
                continue;
            }
            image.setRGB(x, y, color.javaColor().getRGB());
            x += 1;
            if (x >= image.getWidth()) {
                x = 0;
                y += 1;
            }
        }

        File file = new File(JavaPlugin.getPlugin(MapsPlugin.class).getDataFolder(), fileName.replaceAll("[^A-Za-z0-9_]+", "") + ".png");
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Failed to capture palette");
            throw new RuntimeException(e);
        }
        player.sendMessage(ChatColor.GREEN + "Palette has been captured");
    }

    @Subcommand("tearing")
    public void handleTearing(final Player player, int screenId, int interval) {
        MapScreen screen = MapScreenRegistry.getScreen(screenId);
        if (screen == null) {
            return;
        }

        MapGraphics<MapAccess> gr = screen.getGraphics();
        gr.fillBuffer(MapColors.color(0, 0, 255));

        Bukkit.getScheduler().runTaskTimerAsynchronously(JavaPlugin.getPlugin(MapsPlugin.class), new Runnable() {
            private boolean colorSwitch;

            @Override
            public void run() {
                byte color = colorSwitch ? MapColors.color(0, 0, 0) : MapColors.color(255, 255, 255);
                colorSwitch = !colorSwitch;
                gr.fillRect(10, 10, gr.getWidth() - 20, gr.getHeight() - 20, color, 1f);
            }
        }, interval, interval);
    }

    @Subcommand("colors")
    public void handleColors(final Player player, int screenId) {
        MapScreen screen = MapScreenRegistry.getScreen(screenId);
        if (screen == null) {
            return;
        }

        long colors = Arrays.stream(ColorMaps.current().getColors())
                .filter(Objects::nonNull)
                .filter(c -> c.mapColorInt() >= 4)
                .count();
        int screenW = screen.getWidth() * 128;
        int screenH = screen.getHeight() * 128;

        int columns = (int) Math.ceil(Math.sqrt(colors));
        int rows = (int) Math.ceil((double) colors / (double) columns);
        int rectW = screenW / columns;
        int rectH = screenH / rows;

        player.sendMessage("colors: " + colors);
        player.sendMessage("screenW: " + screenW + " screenH: " + screenH);
        player.sendMessage("columns: " + columns + " rows: " + rows);
        player.sendMessage("rectW: " + rectW + " rectH: " + rectH);

        MapGraphics<MapAccess> graphics = screen.getGraphics();
        graphics.fillBuffer(MapColors.color(255, 255, 255));

        int x = 0;
        int y = 0;
        for (ColorMap.Color color : ColorMaps.current().getColors()) {
            if (color == null || color.mapColorInt() < 4) {
                continue;
            }
            graphics.fillRect(x, y, rectW, rectH, color.mapColor(), 1f);
            x += rectW;
            if (x + rectW > screen.getWidth() * 128) {
                x = 0;
                y += rectH;
            }
            if (y + rectH > screen.getHeight() * 128) {
                player.sendMessage("Oops, too big");
                break;
            }
        }

        screen.addViewer(player);
    }

    @Subcommand("triangulate2")
    public void handleTriangulate2(final Player player, final boolean once, final boolean verbose) {
        for (final MapScreen screen : MapScreenRegistry.getScreens()) {
            screen.getGraphics().fillBuffer(ColorCache.rgbToMap(255, 255, 255));
            screen.addViewer(player);
        }

        new BukkitRunnable() {

            private final List<Long> times = new ArrayList<>();
            private MapScreen lastScreen;
            private Vec2 lastPos;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }

                if (verbose) {
                    player.sendMessage("------");
                }

                MapScreen screen = null;
                Vec2 pos = null;
                double dist = Double.MAX_VALUE;
                long ns = System.nanoTime();
                for (final MapScreen mapScreen : MapScreenRegistry.getScreens()) {
                    if (verbose) {
                        player.sendMessage("> " + mapScreen.getId());
                    }
                    final Vec2 screenPos = ScreenTriangulation.triangulateScreenCoords(player, mapScreen);
                    if (screenPos != null) {
                        final double distance = ScreenTriangulation.distance(player, mapScreen);
                        if (pos != null) {
                            if (distance > dist) {
                                continue;
                            }
                        }
                        if (verbose) {
                            player.sendMessage("  > Yep");
                        }
                        dist = distance;
                        pos = screenPos;
                        screen = mapScreen;
                    }
                }
                ns = System.nanoTime() - ns;

                this.times.add(ns);
                if (this.times.size() > 100) {
                    this.times.remove(0);
                }
                if (!this.times.isEmpty()) {
                    double avg = this.times.get(0);
                    for (int i = 1; i < this.times.size(); i++) {
                        avg += this.times.get(i);
                    }
                    avg /= this.times.size();
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(String.format("%,.4f ns / %,.4f ms", avg, avg / TimeUnit.MILLISECONDS.toNanos(1))));
                }
                if (pos == null || screen == null) {
                    if (once) {
                        this.cancel();
                    }
                    return;
                }

                pos.y = screen.getHeight() * 128 - pos.y;

                if (this.lastPos != null) {
                    this.lastScreen.getGraphics().setPixel(this.lastPos.x, this.lastPos.y, ColorCache.rgbToMap(255, 255, 255));
                    if (this.lastScreen != screen) {
                        this.lastScreen.update();
                    }
                }
                screen.getGraphics().setPixel(pos.x, pos.y, ColorCache.rgbToMap(255, 0, 0));
                this.lastPos = pos;
                this.lastScreen = screen;
                screen.update();

                if (once) {
                    this.cancel();
                }
            }
        }.runTaskTimer(JavaPlugin.getPlugin(MapsPlugin.class), 0, 1);
    }

    @Subcommand("triangulate")
    public void handleTriangulate(final Player player, final int screenId) {
        final MapScreen screen = MapScreenRegistry.getScreen(screenId);
        if (screen == null) {
            return;
        }

        if (true) {
            final MapGraphics<MapAccess> graphics = screen.getGraphics();
            graphics.fillBuffer(ColorCache.rgbToMap(255, 255, 255));

            new BukkitRunnable() {

                private final List<Long> times = new ArrayList<>();
                private Vec2 lastPos;

                @Override
                public void run() {
                    long ns = System.nanoTime();
                    final Vec2 pos = ScreenTriangulation.triangulateScreenCoords(player, screen);
                    ns = System.nanoTime() - ns;
                    this.times.add(ns);
                    if (this.times.size() > 100) {
                        this.times.remove(0);
                    }

                    if (pos == null) {
                        player.sendMessage("fail");
                        this.cancel();
                        return;
                    }
                    pos.y = screen.getHeight() * 128 - pos.y;

                    if (this.lastPos != null) {
                        graphics.setPixel(this.lastPos.x, this.lastPos.y, ColorCache.rgbToMap(255, 255, 255));
                    }
                    graphics.setPixel(pos.x, pos.y, ColorCache.rgbToMap(255, 0, 0));
                    this.lastPos = pos;
                    screen.update();

                    if (!this.times.isEmpty()) {
                        double avg = this.times.get(0);
                        for (int i = 1; i < this.times.size(); i++) {
                            avg += this.times.get(i);
                        }
                        avg /= this.times.size();
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(String.format("%,.4f ns / %,.4f ms", avg, avg / TimeUnit.MILLISECONDS.toNanos(1))));
                    }
                }
            }.runTaskTimer(JavaPlugin.getPlugin(MapsPlugin.class), 0, 1);
            return;
        }

        final Location playerLoc = player.getLocation();
        final Location screenLoc = screen.getLocation();
        final BlockFace orientation = screen.getFrames()[0][0].getFacing();
        final boolean inFrontOfScreen = switch (orientation) {
            case NORTH -> playerLoc.getZ() < screenLoc.getZ();
            case EAST -> playerLoc.getX() > screenLoc.getX();
            case SOUTH -> playerLoc.getZ() > screenLoc.getZ();
            case WEST -> playerLoc.getX() < screenLoc.getX();
            default -> false;
        };
        if (!inFrontOfScreen) {
            player.sendMessage("not in front");
            return;
        }

        final float playerYaw = playerLoc.getYaw();
        final boolean isFacingScreen = switch (orientation) {
            case NORTH -> Math.abs(playerYaw) < 90;
            case EAST -> playerYaw > 0 && playerYaw < 180;
            case SOUTH -> playerYaw < -90 || playerYaw > 90;
            case WEST -> playerYaw < 0;
            default -> false;
        };
        if (!isFacingScreen) {
            player.sendMessage("not facing screen");
            return;
        }

        final double dist = switch (orientation) {
            case NORTH, SOUTH -> Math.max(playerLoc.getZ(), screenLoc.getZ()) - Math.min(playerLoc.getZ(), screenLoc.getZ());
            case EAST, WEST -> Math.max(playerLoc.getX(), screenLoc.getX()) - Math.min(playerLoc.getX(), screenLoc.getX());
            default -> 0d;
        };
        final float alpha = switch (orientation) {
            case NORTH -> Math.abs(playerYaw);
            case EAST -> playerYaw < 90f ? 90f - playerYaw : playerYaw - 90f;
            case SOUTH -> 90 - (Math.abs(playerYaw) - 90);
            case WEST -> playerYaw < -90f ? Math.abs(playerYaw) - 90f : 90f - Math.abs(playerYaw);
            default -> 0f;
        };
        final float alphaMod = switch (orientation) {
            case NORTH -> playerYaw < 0 ? -1f : 1f;
            case EAST -> playerYaw < 90 ? -1f : 1f;
            case SOUTH -> playerYaw > 0 ? -1f : 1f;
            case WEST -> playerYaw < -90 ? -1f : 1f;
            default -> 0f;
        };
        final double a = this.triangleA(dist, alpha);
        final double aMod = a * alphaMod;

        player.sendMessage(String.format("yaw      = %,.4f", playerYaw));
        player.sendMessage(String.format("dist     = %,.4f", dist));
        player.sendMessage(String.format("alpha    = %,.4f", alpha));
        player.sendMessage(String.format("a        = %,.4f", a));
        player.sendMessage(String.format("aMod     = %,.4f", aMod));

        final Location eyeLoc = player.getEyeLocation().clone();
        final Location clickLoc = player.getEyeLocation().clone();
        switch (orientation) {
            case NORTH -> {
                player.sendMessage("  > X - aMod");
                clickLoc.setX(clickLoc.getX() - aMod);
                clickLoc.setZ(screenLoc.getZ());
            }
            case EAST -> {
                player.sendMessage("  > Z - aMod");
                clickLoc.setZ(clickLoc.getZ() - aMod);
                clickLoc.setX(screenLoc.getX());
            }
            case SOUTH -> {
                player.sendMessage("  > X + aMod");
                clickLoc.setX(clickLoc.getX() + aMod);
                clickLoc.setZ(screenLoc.getZ());
            }
            case WEST -> {
                player.sendMessage("  > Z + aMod");
                clickLoc.setZ(clickLoc.getZ() + aMod);
                clickLoc.setX(screenLoc.getX());
            }
        }

        final Location relPos = screen.getHitBox().bottomLeft().clone().subtract(clickLoc.clone());
        final Vec2 screenPos = switch (orientation) {
            case NORTH -> {
                final int x = (int) (relPos.getX() * 128d);
                final int y = (int) ((-relPos.getY()) * 128d);
                yield new Vec2(x, y);
            }
            case EAST -> {
                final int x = (int) (relPos.getZ() * 128d);
                final int y = (int) ((-relPos.getY()) * 128d);
                yield new Vec2(x, y);
            }
            case SOUTH -> {
                final int x = (int) ((-relPos.getX()) * 128d);
                final int y = (int) ((-relPos.getY()) * 128d);
                yield new Vec2(x, y);
            }
            case WEST -> {
                final int x = (int) ((-relPos.getZ()) * 128d);
                final int y = (int) ((-relPos.getY()) * 128d);
                yield new Vec2(x, y);
            }
            default -> new Vec2(0, 0);
        };
        screen.getGraphics().setPixel(screenPos.x, (screen.getHeight() * 128) - screenPos.y, ColorCache.rgbToMap(255, 0, 0));
        screen.update();

        player.sendMessage("Hitbox = " + screen.getHitBox().contains(clickLoc));
        player.sendMessage("Hitbox bl = %,.2f %,.2f %,.2f".formatted(
                screen.getHitBox().bottomLeft().getX(),
                screen.getHitBox().bottomLeft().getY(),
                screen.getHitBox().bottomLeft().getZ()
        ));
        player.sendMessage("Hitbox tr = %,.2f %,.2f %,.2f".formatted(
                screen.getHitBox().topRight().getX(),
                screen.getHitBox().topRight().getY(),
                screen.getHitBox().topRight().getZ()
        ));
        player.sendMessage("Click = %,.2f %,.2f %,.2f".formatted(
                clickLoc.getX(),
                clickLoc.getY(),
                clickLoc.getZ()
        ));

        final MapsPlugin plugin = JavaPlugin.getPlugin(MapsPlugin.class);
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (this.ticks++ > 20 * 10) {
                    this.cancel();
                }

                clickLoc.getWorld().spawnParticle(Particle.REDSTONE, clickLoc, 1, 0f, 0f, 0f, 0, new Particle.DustOptions(Color.RED, 0.25f));
                screenLoc.getWorld().spawnParticle(Particle.REDSTONE, screenLoc, 1, 0f, 0f, 0f, 0, new Particle.DustOptions(Color.GREEN, 0.25f));
                screenLoc.getWorld().spawnParticle(Particle.REDSTONE, playerLoc, 1, 0f, 0f, 0f, 0, new Particle.DustOptions(Color.YELLOW, 0.25f));
                screenLoc.getWorld().spawnParticle(Particle.REDSTONE, eyeLoc, 1, 0f, 0f, 0f, 0, new Particle.DustOptions(Color.ORANGE, 0.25f));
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private double triangleA(final double b, final double alpha) {
        final double c = b / Math.cos(Math.toRadians(alpha));
        Bukkit.broadcastMessage("t> alpha    = " + String.format("%,.4f", alpha));
        Bukkit.broadcastMessage("t> b        = " + String.format("%,.4f", b));
        Bukkit.broadcastMessage("t> c        = " + String.format("%,.4f", c));
        return this.betterSqrt((c * c) - (b * b));
    }

    private double betterSqrt(final double d) {
        final double sqrt = this.sqrt(d);
        return (sqrt + d / sqrt) * 0.5d;
    }

    private double sqrt(final double d) {
        return Double.longBitsToDouble(((Double.doubleToLongBits(d) - (1L << 52)) >> 1) + (1L << 61));
    }

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
            mapFont = FontConverter.convert(font, IntStream.range(0, 0x10FFFF).boxed().toList());
        } catch (final Throwable t) {
            t.printStackTrace();
            player.sendMessage("Error " + t.getMessage());
            return;
        }

        final MapGraphics<MapAccess> graphics = screen.getGraphics();
        graphics.fillBuffer((byte) 0);
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
        screen.fullUpdate();

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
                        this.prev.update();
                    }
                    player.sendMessage("Cancel");
                    return;
                }

                final Optional<RayCastUtil.Result> resultOpt = RayCastUtil.getTargetedScreen(player, 10, MapScreenRegistry.getScreens());
                if (resultOpt.isEmpty()) {
                    if (this.prev != null) {
                        this.prev.update();
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
                    screen.update();
                    screen.removeMarker(marker);

                    if (this.prev != screen && this.prev != null) {
                        this.prev.update();
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
            screen.update();
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
