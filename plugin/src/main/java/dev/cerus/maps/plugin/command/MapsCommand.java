package dev.cerus.maps.plugin.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import dev.cerus.maps.api.MapColor;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.Marker;
import dev.cerus.maps.api.graphics.MapScreenGraphics;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.util.EntityUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;
import net.md_5.bungee.api.chat.TextComponent;
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

        final EntityUtil.ItemFrameResult result = EntityUtil.getNearbyItemFrames(startingFrame, startingFrame.getFacing(), 20, 20);
        final int[][] frameIds = new int[result.getWidth()][result.getHeight()];
        for (int x = 0; x < result.getWidth(); x++) {
            for (int y = 0; y < result.getHeight(); y++) {
                final ItemFrame frame = result.getFrames()[x][y];
                frameIds[x][result.getHeight() - 1 - y] = frame.getEntityId();
            }
        }

        final MapScreen mapScreen = new MapScreen(this.versionAdapter, result.getWidth(), result.getHeight());
        mapScreen.setFrameIds(frameIds);
        final int id = MapScreenRegistry.registerScreen(mapScreen);
        player.sendMessage("§aScreen §e#" + id + " §ahas been created!");
        this.handleTestScreen(player, id);
    }

    @Subcommand("testscreen")
    @CommandPermission("maps.command.testscreen")
    public void handleTestScreen(final Player player, final int id) {
        final MapScreen mapScreen = MapScreenRegistry.getScreen(id);
        if (mapScreen == null) {
            player.sendMessage("§cScreen not found");
            return;
        }

        final MapScreenGraphics graphics = mapScreen.getGraphics();
        graphics.fill((byte) MapColor.WHITE_2.getId());
        graphics.drawRect(0, 0, mapScreen.getWidth() * 128 - 1, mapScreen.getHeight() * 128 - 1, (byte) MapColor.BLACK_2.getId());
        graphics.drawLine(1, 1, mapScreen.getWidth() * 128 - 2, mapScreen.getHeight() * 128 - 2, (byte) MapColor.BLACK_2.getId());
        graphics.drawLine(1, mapScreen.getHeight() * 128 - 2, mapScreen.getWidth() * 128 - 2, 1, (byte) MapColor.BLACK_2.getId());

        final String text = "#" + id + " - " + ThreadLocalRandom.current().nextInt(256);
        final int width = MinecraftFont.Font.getWidth(text) * 3;
        final int height = MinecraftFont.Font.getHeight() * 3;
        graphics.drawText((mapScreen.getWidth() * 128 / 2) - (width / 2), (mapScreen.getHeight() * 128 / 2) - (height / 2), text, (byte) MapColor.GRAY_3.getId(), 3);

        if (mapScreen.getMarkers().isEmpty()) {
            mapScreen.addMarker(new Marker(
                    16,
                    16,
                    (byte) 1,
                    MapCursor.Type.MANSION,
                    true,
                    new TextComponent("affe")
            ));
        }

        mapScreen.sendFrames(player);
        mapScreen.sendMaps(false, player);
    }

    @Subcommand("removescreen")
    @CommandPermission("maps.command.removescreen")
    public void handleRemoveScreen(final Player player, final int id) {
        MapScreenRegistry.removeScreen(id);
        player.sendMessage("§aScreen was removed");
    }

    @Subcommand("listscreens")
    @CommandPermission("maps.command.listscreens")
    public void handleListScreens(final Player player) {
        final Collection<Integer> screenIds = MapScreenRegistry.getScreenIds();
        player.sendMessage("§6There are " + screenIds.size() + " map screens on this server");
        player.sendMessage("§e" + Arrays.toString(screenIds.toArray(new Integer[0])));
    }

    @Subcommand("info")
    @CommandPermission("maps.command.info")
    public void handleInfo(final Player player, final int id) {
        final MapScreen screen = MapScreenRegistry.getScreen(id);
        if (screen == null) {
            player.sendMessage("§cScreen not found");
            return;
        }

        player.sendMessage("§7Screen §b#" + id + " §7(" + screen.getWidth() + "x" + screen.getHeight() + ")");
    }

    @Subcommand("inject")
    @CommandPermission("maps.command.inject")
    public void handleInject(final Player player, final int id) {
        this.versionAdapter.inject(player);
    }

}
