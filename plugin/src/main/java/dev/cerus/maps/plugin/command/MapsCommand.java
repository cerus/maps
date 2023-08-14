package dev.cerus.maps.plugin.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import dev.cerus.maps.api.Frame;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.Marker;
import dev.cerus.maps.api.graphics.ColorCache;
import dev.cerus.maps.api.graphics.CompositeColorCache;
import dev.cerus.maps.api.graphics.FunctionalMapGraphics;
import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.api.graphics.StandaloneMapGraphics;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.util.EntityUtil;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MinecraftFont;

@CommandAlias("maps")
@CommandPermission("maps.command")
public class MapsCommand extends BaseCommand {

    @Dependency
    private VersionAdapter versionAdapter;

    @Subcommand("createscreen")
    @CommandPermission("maps.command.createscreen")
    public void handleOldCreateScreen(final Player player) {
        this.handleCreateScreen(player);
    }

    @Subcommand("screen create")
    @CommandPermission("maps.command.createscreen")
    public void handleCreateScreen(final Player player) {
        final ItemFrame startingFrame = player.getNearbyEntities(10, 10, 10).stream()
                .filter(entity -> entity instanceof ItemFrame)
                .filter(entity -> EntityUtil.isLookingAt(player, entity))
                .map(entity -> (ItemFrame) entity)
                .min(Comparator.comparingDouble(entity -> entity.getLocation().distance(player.getLocation())))
                .orElse(null);
        if (startingFrame == null) {
            return;
        }

<<<<<<< Updated upstream
        final EntityUtil.ItemFrameResult result = EntityUtil.getNearbyItemFrames(startingFrame, startingFrame.getFacing(), 20, 20);
=======
        final EntityUtil.ItemFrameResult result = EntityUtil.getNearbyItemFrames(startingFrame, startingFrame.getFacing(), 50, 50);
        for (int x = 0; x < result.getWidth(); x++) {
            for (int y = 0; y < result.getHeight(); y++) {
                final ItemFrame frame = result.getFrames()[x][y];
                if (frame.getItem().getType() != Material.AIR) {
                    player.sendMessage("§cFrames can not contain items");
                    this.versionAdapter.spawnBarrierParticle(player, frame.getLocation().getBlock().getRelative(frame.getFacing()).getLocation().clone().add(0.5, 0.5, 0.5));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    return;
                }
            }
        }


>>>>>>> Stashed changes
        final Frame[][] frames = new Frame[result.getWidth()][result.getHeight()];
        for (int x = 0; x < result.getWidth(); x++) {
            for (int y = 0; y < result.getHeight(); y++) {
                final ItemFrame frame = result.getFrames()[x][y];
                frames[x][result.getHeight() - 1 - y] = new Frame(
                        frame.getWorld(),
                        frame.getLocation().getBlockX(),
                        frame.getLocation().getBlockY(),
                        frame.getLocation().getBlockZ(),
                        frame.getFacing(),
                        frame.getEntityId(),
                        frame.isVisible()
                );
                frame.remove();
            }
        }

        final int id = MapScreenRegistry.getNextFreeId();
        final MapScreen mapScreen = new MapScreen(id, this.versionAdapter, result.getWidth(), result.getHeight());
        mapScreen.setLocation(startingFrame.getLocation());
        mapScreen.setFrames(frames);
        MapScreenRegistry.registerScreen(mapScreen);
        player.sendMessage("§aScreen §e#" + id + " §ahas been created!");
        this.handleTestScreen(player, id);
    }

    @Subcommand("screen test")
    @CommandPermission("maps.command.testscreen")
    public void handleTestScreen(final Player player, final int id) {
        final MapScreen mapScreen = MapScreenRegistry.getScreen(id);
        if (mapScreen == null) {
            player.sendMessage("§cScreen not found");
            return;
        }

        final MapGraphics<?, ?> graphics = mapScreen.getGraphics();
        graphics.fillComplete((byte) 34);
        //graphics.fill((byte) 0);
        graphics.drawRect(0, 0, mapScreen.getWidth() * 128 - 1, mapScreen.getHeight() * 128 - 1, (byte) 118, 1f);
        graphics.drawLine(1, 1, mapScreen.getWidth() * 128 - 2, mapScreen.getHeight() * 128 - 2, (byte) 118, 1f);
        graphics.drawLine(1, mapScreen.getHeight() * 128 - 2, mapScreen.getWidth() * 128 - 2, 1, (byte) 118, 1f);

        final String text = "#" + id;
        final int width = MinecraftFont.Font.getWidth(text) * 3;
        final int height = MinecraftFont.Font.getHeight() * 3;
        graphics.drawText((mapScreen.getWidth() * 128 / 2) - (width / 2), (mapScreen.getHeight() * 128 / 2) - (height / 2), text, (byte) 15, 3);

        mapScreen.spawnFrames(player);
        mapScreen.sendMaps(true, player);
    }

    @Subcommand("debugscreen roundrect")
    @CommandPermission("maps.command.debugscreen")
    public void handleDebugScreenRoundRect(final Player player, final int id) {
        final MapScreen mapScreen = MapScreenRegistry.getScreen(id);
        if (mapScreen == null) {
            player.sendMessage("§cScreen not found");
            return;
        }

        final MapGraphics<?, ?> graphics = mapScreen.getGraphics();
        graphics.fillComplete(ColorCache.rgbToMap(0, 0, 255));
        mapScreen.clearMarkers();

        final MapGraphics<MapGraphics<?, ?>, ?> canvas = MapGraphics.standalone(300, 40);

        final MapGraphics<MapGraphics<?, ?>, ?> circleTopLeft = MapGraphics.standalone(8, 8);
        circleTopLeft.drawEllipse(7, 7, 7, 7, ColorCache.rgbToMap(232, 232, 232));
        final MapGraphics<MapGraphics<?, ?>, ?> circleTopRight = MapGraphics.standalone(8, 8);
        circleTopRight.drawEllipse(0, 7, 7, 7, ColorCache.rgbToMap(232, 232, 232));
        final MapGraphics<MapGraphics<?, ?>, ?> circleBottomLeft = MapGraphics.standalone(8, 8);
        circleBottomLeft.drawEllipse(7, 0, 7, 7, ColorCache.rgbToMap(232, 232, 232));
        final MapGraphics<MapGraphics<?, ?>, ?> circleBottomRight = MapGraphics.standalone(8, 8);
        circleBottomRight.drawEllipse(0, 0, 7, 7, ColorCache.rgbToMap(232, 232, 232));

        canvas.place(circleTopLeft, 0, 0);
        canvas.place(circleTopRight, canvas.getWidth() - 8, 0);
        canvas.place(circleBottomLeft, 0, canvas.getHeight() - 8);
        canvas.place(circleBottomRight, canvas.getWidth() - 8, canvas.getHeight() - 8);
        canvas.drawLine(0, 8, 0, canvas.getHeight() - 8, ColorCache.rgbToMap(232, 232, 232), 1f);
        canvas.drawLine(canvas.getWidth() - 1, 8, canvas.getWidth() - 1, canvas.getHeight() - 8, ColorCache.rgbToMap(232, 232, 232), 1f);
        canvas.drawLine(8, 0, canvas.getWidth() - 8, 0, ColorCache.rgbToMap(232, 232, 232), 1f);
        canvas.drawLine(8, canvas.getHeight() - 1, canvas.getWidth() - 8, canvas.getHeight() - 1, ColorCache.rgbToMap(232, 232, 232), 1f);
        canvas.fill(canvas.getWidth() / 2, 1, ColorCache.rgbToMap(255, 255, 255), 1f);
        //canvas.drawRect(7, 0, canvas.getWidth() - 15, canvas.getHeight() - 1, ColorCache.rgbToMap(255, 255, 255));

        //canvas.drawRect(32, 0, canvas.getWidth() - 33, canvas.getHeight() - 1, ColorCache.rgbToMap(255, 0, 0));
        graphics.place(canvas, 64, 64);

        mapScreen.spawnFrames(player);
        mapScreen.sendMaps(false, player);
    }

    @Subcommand("debugscreen ellipse")
    @CommandPermission("maps.command.debugscreen")
    public void handleDebugScreenEllipse(final Player player, final int id) {
        final MapScreen mapScreen = MapScreenRegistry.getScreen(id);
        if (mapScreen == null) {
            player.sendMessage("§cScreen not found");
            return;
        }

        final MapGraphics<?, ?> graphics = mapScreen.getGraphics();
        //graphics.fillComplete(ColorCache.rgbToMap(0, 0, 255));
        mapScreen.clearMarkers();

        FunctionalMapGraphics.backedBy(graphics)
                .with(g -> g.fillComplete(ColorCache.rgbToMap(0, 255, 255)))
                .with(g -> g.drawEllipse(
                        mapScreen.getWidth() * 128 / 2,
                        mapScreen.getHeight() * 128 / 2,
                        (mapScreen.getWidth() * 128 - 64) / 2,
                        (mapScreen.getHeight() * 128 - 128) / 2,
                        ColorCache.rgbToMap(255, 0, 0)
                ))
                .with(g -> g.fill(
                        mapScreen.getWidth() * 128 / 2,
                        mapScreen.getHeight() * 128 / 2,
                        ColorCache.rgbToMap(0, 255, 0),
                        1f
                ));

        /*graphics.drawEllipse(
                mapScreen.getWidth() * 128 / 2,
                mapScreen.getHeight() * 128 / 2,
                (mapScreen.getWidth() * 128 - 64) / 2,
                (mapScreen.getHeight() * 128 - 128) / 2,
                ColorCache.rgbToMap(255, 0, 0)
        );
        graphics.fill(
                mapScreen.getWidth() * 128 / 2,
                mapScreen.getHeight() * 128 / 2,
                ColorCache.rgbToMap(0, 255, 0),
                1f
        );*/
        mapScreen.spawnFrames(player);
        mapScreen.sendMaps(false, player);
    }

    @Subcommand("debugscreen composite")
    @CommandPermission("maps.command.debugscreen")
    public void handleDebugScreenComposite(final Player player, final int id, final float a) {
        final MapScreen mapScreen = MapScreenRegistry.getScreen(id);
        if (mapScreen == null) {
            player.sendMessage("§cScreen not found");
            return;
        }

        long nanoBefore = System.nanoTime();

        final MapGraphics<?, ?> graphics = mapScreen.getGraphics();
        graphics.fillComplete(ColorCache.rgbToMap(0, 0, 255));
        mapScreen.clearMarkers();

        final StandaloneMapGraphics group = new StandaloneMapGraphics(64 + 32, 64 + 32);

        final StandaloneMapGraphics blackBox = new StandaloneMapGraphics(64, 64);
        //blackBox.fillComplete(ColorCache.rgbToMap(0, 0, 0));
        blackBox.drawEllipse(32, 32, 31, 31, ColorCache.rgbToMap(0, 0, 0));
        blackBox.fill(32, 32, ColorCache.rgbToMap(0, 0, 0), 1f);

        final StandaloneMapGraphics whiteBox = new StandaloneMapGraphics(64, 64);
        //whiteBox.fillComplete(ColorCache.rgbToMap(255, 255, 255));
        whiteBox.drawEllipse(32, 32, 31, 31, ColorCache.rgbToMap(255, 255, 255));
        whiteBox.fill(32, 32, ColorCache.rgbToMap(255, 255, 255), 1f);

        group.place(blackBox, 0, 0);
        group.compositeIn(whiteBox, 32, 32);
        group.drawRect(0, 0, 64 + 32 - 1, 64 + 32 - 1, ColorCache.rgbToMap(255, 0, 0), 1f);
        graphics.place(group, 64 - 48, 64 - 48, a);
        mapScreen.addMarker(new Marker(
                128 - 48 * 2, 128 - 48 * 2,
                (byte) 8,
                MapCursor.Type.WHITE_CROSS,
                true,
                new TextComponent("In")
        ));

        group.fillComplete((byte) 0);
        group.place(blackBox, 0, 0);
        group.compositeOut(whiteBox, 32, 32);
        group.drawRect(0, 0, 64 + 32 - 1, 64 + 32 - 1, ColorCache.rgbToMap(255, 0, 0), 1f);
        graphics.place(group, 128 + 64 - 48, 64 - 48, a);
        mapScreen.addMarker(new Marker(
                (128 + 64 - 48) * 2, 128 - 48 * 2,
                (byte) 8,
                MapCursor.Type.WHITE_CROSS,
                true,
                new TextComponent("Out")
        ));

        group.fillComplete((byte) 0);
        group.place(blackBox, 0, 0);
        group.place(whiteBox, 32, 32);
        group.drawRect(0, 0, 64 + 32 - 1, 64 + 32 - 1, ColorCache.rgbToMap(255, 0, 0), 1f);
        graphics.place(group, 128 + 64 - 48 + 64 + 64, 64 - 48, a);
        mapScreen.addMarker(new Marker(
                (128 + 64 - 48 + 64 + 64) * 2, 128 - 48 * 2,
                (byte) 8,
                MapCursor.Type.WHITE_CROSS,
                true,
                new TextComponent("Normal")
        ));

        group.fillComplete((byte) 0);
        group.place(blackBox, 0, 0);
        group.compositeAtop(whiteBox, 32, 32);
        group.drawRect(0, 0, 64 + 32 - 1, 64 + 32 - 1, ColorCache.rgbToMap(255, 0, 0), 1f);
        graphics.place(group, 64 - 48, 128 + 64 - 48, a);
        mapScreen.addMarker(new Marker(
                128 - 48 * 2, (128 + 64 - 48) * 2,
                (byte) 8,
                MapCursor.Type.WHITE_CROSS,
                true,
                new TextComponent("Atop")
        ));

        group.fillComplete((byte) 0);
        group.place(blackBox, 0, 0);
        group.compositeXor(whiteBox, 32, 32);
        group.drawRect(0, 0, 64 + 32 - 1, 64 + 32 - 1, ColorCache.rgbToMap(255, 0, 0), 1f);
        graphics.place(group, 128 + 64 - 48, 128 + 64 - 48, a);
        mapScreen.addMarker(new Marker(
                (128 + 64 - 48) * 2, (128 + 64 - 48) * 2,
                (byte) 8,
                MapCursor.Type.WHITE_CROSS,
                true,
                new TextComponent("Xor")
        ));

        group.fillComplete((byte) 0);
        group.place(blackBox, 0, 0);
        group.compositeOver(whiteBox, 32, 32);
        group.drawRect(0, 0, 64 + 32 - 1, 64 + 32 - 1, ColorCache.rgbToMap(255, 0, 0), 1f);
        graphics.place(group, 128 + 64 - 48 + 128, 128 + 64 - 48, a);
        mapScreen.addMarker(new Marker(
                (128 + 64 - 48 + 128) * 2, (128 + 64 - 48) * 2,
                (byte) 8,
                MapCursor.Type.WHITE_CROSS,
                true,
                new TextComponent("Over")
        ));

        long nanoAfter = System.nanoTime();
        long nanoDiff = nanoAfter - nanoBefore;
        player.sendMessage(String.format("Compute took %d ns (%.4f ms)", nanoDiff, ((double) nanoDiff) / TimeUnit.MILLISECONDS.toNanos(1)));

        nanoBefore = System.nanoTime();
        mapScreen.spawnFrames(player);
        mapScreen.sendMaps(false, player);
        nanoAfter = System.nanoTime();

        nanoDiff = nanoAfter - nanoBefore;
        player.sendMessage(String.format("Send took %d ns (%.4f ms)", nanoDiff, ((double) nanoDiff) / TimeUnit.MILLISECONDS.toNanos(1)));
    }

    @Subcommand("debugscreen alphabench")
    @CommandPermission("maps.command.debugscreen")
    public void handleDebugScreenAlphaBench(final Player player, final int id, final float a) {
        final MapScreen mapScreen = MapScreenRegistry.getScreen(id);
        if (mapScreen == null) {
            player.sendMessage("§cScreen not found");
            return;
        }

        final MapGraphics<?, ?> img;
        try {
            final BufferedImage image = ImageIO.read(new URL("https://cerus.dev/img/edina_lang_logo.png"));
            img = StandaloneMapGraphics.newGraphicsObject(image.getWidth(), image.getHeight());
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    final int rgb = image.getRGB(x, y);
                    final Color color = new Color(rgb, image.getColorModel().hasAlpha());
                    img.setPixel(x, y, ColorCache.rgbToMap(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue()
                    ));
                }
            }
        } catch (final IOException ignored) {
            player.sendMessage("§cError");
            return;
        }

        long nanoBefore = System.nanoTime();

        final MapGraphics<?, ?> graphics = mapScreen.getGraphics();
        graphics.fillComplete(ColorCache.rgbToMap(0, 255, 0));
        mapScreen.clearMarkers();
        graphics.place(img, (graphics.getWidth() / 2) - (img.getWidth() / 2), (graphics.getHeight() / 2) - (img.getHeight() / 2), a);

        long nanoAfter = System.nanoTime();
        long nanoDiff = nanoAfter - nanoBefore;
        player.sendMessage(String.format("Compute took %d ns (%.4f ms)", nanoDiff, ((double) nanoDiff) / TimeUnit.MILLISECONDS.toNanos(1)));

        nanoBefore = System.nanoTime();
        mapScreen.spawnFrames(player);
        mapScreen.sendMaps(false, player);
        nanoAfter = System.nanoTime();

        nanoDiff = nanoAfter - nanoBefore;
        player.sendMessage(String.format("Send took %d ns (%.4f ms)", nanoDiff, ((double) nanoDiff) / TimeUnit.MILLISECONDS.toNanos(1)));
        player.sendMessage("Composition cache: %d".formatted(CompositeColorCache.size()));
    }

    @Subcommand("screen remove")
    @CommandPermission("maps.command.removescreen")
    public void handleRemoveScreen(final Player player, final int id) {
        MapScreenRegistry.removeScreen(id);
        player.sendMessage("§aScreen was removed");
    }

    @Subcommand("screen list")
    @CommandPermission("maps.command.listscreens")
    public void handleListScreens(final Player player) {
        final List<Integer> screenIds = new ArrayList<>(MapScreenRegistry.getScreenIds());
        player.sendMessage("§6There are " + screenIds.size() + " map screens on this server");
        final ComponentBuilder listBuilder = new ComponentBuilder("§e[");
        for (int i = 0; i < screenIds.size(); i++) {
            final int screenId = screenIds.get(i);
            final boolean last = i == screenIds.size() - 1;
            listBuilder.append(new ComponentBuilder("§e" + screenId + (last ? "" : ", "))
                    .retain(ComponentBuilder.FormatRetention.NONE)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to run §f/maps screen info " + screenId)))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/maps screen info " + screenId))
                    .create());
        }
        listBuilder.append("§e]");
        player.spigot().sendMessage(listBuilder.create());
    }

    @Subcommand("screen info")
    @CommandPermission("maps.command.info")
    public void handleInfo(final Player player, final int id) {
        final MapScreen screen = MapScreenRegistry.getScreen(id);
        if (screen == null) {
            player.sendMessage("§cScreen not found");
            return;
        }

        player.sendMessage("§7Screen §b#" + id + " §7(" + screen.getWidth() + "x" + screen.getHeight() + ")");
        if (screen.getLocation() != null) {
            player.spigot().sendMessage(new ComponentBuilder("§8[§eTeleport§8]")
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to teleport")))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minecraft:tp %s %f %f %f".formatted(
                            player.getName(), screen.getLocation().getX(),
                            screen.getLocation().getY(), screen.getLocation().getZ()
                    )))
                    .create());
        }
    }

}
