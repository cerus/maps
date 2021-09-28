package dev.cerus.maps.plugin.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import dev.cerus.maps.api.MapColor;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.graphics.MapScreenGraphics;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.util.EntityUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.map.MinecraftFont;

@CommandAlias("maps")
public class MapsCommand extends BaseCommand {

    @Dependency
    private VersionAdapter versionAdapter;

    @Subcommand("createscreen")
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
    public void handleTestScreen(final Player player, final int id) {
        final MapScreen mapScreen = MapScreenRegistry.getScreen(id);
        if (mapScreen == null) {
            player.sendMessage("§cScreen not found");
            return;
        }

        final MapScreenGraphics graphics = mapScreen.getGraphics();
        graphics.fill((byte) MapColor.WHITE_2.getId());
        graphics.drawRect(0, 0, mapScreen.getWidth() * 128 - 1, mapScreen.getHeight() * 128 - 1, (byte) MapColor.BLACK_2.getId());
        graphics.drawLine(0, 0, mapScreen.getWidth() * 128, mapScreen.getHeight() * 128, (byte) MapColor.BLACK_2.getId());
        graphics.drawLine(0, mapScreen.getHeight() * 128, mapScreen.getWidth() * 128, 0, (byte) MapColor.BLACK_2.getId());

        final String text = "#" + id;
        final int width = MinecraftFont.Font.getWidth(text) * 3;
        final int height = MinecraftFont.Font.getHeight() * 3;
        graphics.drawText((mapScreen.getWidth() * 128 / 2) - (width / 2), (mapScreen.getHeight() * 128 / 2) - (height / 2), text, (byte) MapColor.GRAY_3.getId(), 3);

        mapScreen.update(MapScreen.DirtyHandlingPolicy.IGNORE, player);
    }

    @Subcommand("removescreen")
    public void handleRemoveScreen(final Player player, final int id) {
        MapScreenRegistry.removeScreen(id);
        player.sendMessage("§aScreen was removed");
    }

    @Subcommand("listscreens")
    public void handleListScreens(final Player player) {
        final Collection<Integer> screenIds = MapScreenRegistry.getScreenIds();
        player.sendMessage("§6There are " + screenIds.size() + " map screens on this server");
        player.sendMessage("§e" + Arrays.toString(screenIds.toArray(new Integer[0])));
    }

    @Subcommand("togglealgo")
    public void handleToggleAlgo(final Player player, final int id) {
        final MapScreen screen = MapScreenRegistry.getScreen(id);
        if (screen == null) {
            player.sendMessage("§cScreen not found");
            return;
        }

        if (screen.isAdvancedContentChangeAlgorithmEnabled()) {
            screen.disableAdvancedContentChangeAlgorithm();
            player.sendMessage("§eAdvanced content change algorithm has been disabled for screen #" + id + ".");
        } else {
            screen.enableAdvancedContentChangeAlgorithm();
            player.sendMessage("§aAdvanced content change algorithm has been enabled for screen #" + id + ".");
            player.sendMessage("§cWarning: §7Map screens with the algorithm enabled will use double the memory.");
        }
    }

    @Subcommand("info")
    public void handleInfo(final Player player, final int id) {
        final MapScreen screen = MapScreenRegistry.getScreen(id);
        if (screen == null) {
            player.sendMessage("§cScreen not found");
            return;
        }

        player.sendMessage("§7Screen §b#" + id + " §7(" + screen.getWidth() + "x" + screen.getHeight() + ")");
        player.sendMessage("§7Advanced content change algorithm is "
                + (screen.isAdvancedContentChangeAlgorithmEnabled() ? "§aenabled" : "§cdisabled"));
    }

}
