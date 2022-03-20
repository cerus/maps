package dev.cerus.maps.plugin.map;

import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.plugin.MapsPlugin;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.plugin.java.JavaPlugin;

public class MapScreenRegistry {

    private static final JavaPlugin plugin = JavaPlugin.getPlugin(MapsPlugin.class);
    private static final Map<Integer, MapScreen> screenMap = new HashMap<>();
    private static int highestId = 0;

    public static void store(final ConfigurationSection configuration) {
        configuration.set("screens", null);
        final ConfigurationSection screensSection = configuration.createSection("screens");
        screenLoop:
        for (final Map.Entry<Integer, MapScreen> entry : screenMap.entrySet()) {
            final int id = entry.getKey();
            final MapScreen screen = entry.getValue();
            if (screen.getFrameIds() == null) {
                continue;
            }

            final Entity[][] frames = new Entity[screen.getWidth()][screen.getHeight()];
            for (int x = 0; x < screen.getWidth(); x++) {
                for (int y = 0; y < screen.getHeight(); y++) {
                    final int fid = screen.getFrameIds()[x][y];
                    final Entity frame = Bukkit.getWorlds().stream()
                            .flatMap(world -> world.getEntities().stream())
                            .filter(entity -> entity.getEntityId() == fid)
                            .findAny()
                            .orElse(null);
                    if (frame == null) {
                        continue screenLoop;
                    }
                    frames[x][y] = frame;
                }
            }
            final World world = frames[0][0].getWorld();

            final ConfigurationSection section = screensSection.createSection(String.valueOf(id));
            section.set("id", id);
            section.set("width", screen.getWidth());
            section.set("height", screen.getHeight());
            section.set("world", world.getUID().toString());

            final ConfigurationSection framesSection = section.createSection("frames");
            for (int x = 0; x < screen.getWidth(); x++) {
                for (int y = 0; y < screen.getHeight(); y++) {
                    final Entity entity = frames[x][y];
                    final Chunk chunk = entity.getLocation().getChunk();
                    framesSection.set(entity.getUniqueId().toString(), chunk.getX() + ";" + chunk.getZ() + "|" + x + ";" + y);
                }
            }
        }
    }

    public static void load(final ConfigurationSection configuration, final VersionAdapter versionAdapter) {
        final ConfigurationSection screensSection = configuration.getConfigurationSection("screens");
        screenLoop:
        for (final String key : screensSection.getKeys(false)) {
            final ConfigurationSection section = screensSection.getConfigurationSection(key);
            final int id = section.getInt("id");
            final int width = section.getInt("width");
            final int height = section.getInt("height");

            final UUID worldId = UUID.fromString(section.getString("world"));
            final World world = Bukkit.getWorld(worldId);

            final Set<Chunk> tempChunkSet = new HashSet<>();
            final int[][] frameIds = new int[width][height];
            final ConfigurationSection framesSection = section.getConfigurationSection("frames");
            for (final String frameIdStr : framesSection.getKeys(false)) {
                final String chunkIdAndPosStr = framesSection.getString(frameIdStr);
                final String[] chunkIdSplit = chunkIdAndPosStr.split("\\|")[0].split(";");
                final String[] posSplit = chunkIdAndPosStr.split("\\|")[1].split(";");

                final Chunk chunk = world.getChunkAt(Integer.parseInt(chunkIdSplit[0]), Integer.parseInt(chunkIdSplit[1]));
                chunk.load();

                final ItemFrame itemFrame = Arrays.stream(chunk.getEntities())
                        .filter(entity -> entity.getUniqueId().toString().equals(frameIdStr))
                        .filter(entity -> entity instanceof ItemFrame)
                        .map(entity -> (ItemFrame) entity)
                        .findAny()
                        .orElse(null);
                if (itemFrame == null) {
                    System.out.println(chunk.getX() + ";" + chunk.getZ() + ": " + Arrays.stream(chunk.getEntities())
                            .map(entity -> entity.getUniqueId().toString())
                            .collect(Collectors.joining(", ")));
                    System.out.println(id + " abort: " + frameIdStr + " not found in " + chunk.getX() + ";" + chunk.getZ());
                    continue screenLoop;
                }

                frameIds[Integer.parseInt(posSplit[0])][Integer.parseInt(posSplit[1])] = itemFrame.getEntityId();
                tempChunkSet.add(chunk);
            }
            for (final Chunk chunk : tempChunkSet) {
                chunk.addPluginChunkTicket(plugin);
            }

            final MapScreen mapScreen = new MapScreen(id, versionAdapter, width, height);
            mapScreen.setFrameIds(frameIds);
            if (id > highestId) {
                highestId = id;
            }
            screenMap.put(id, mapScreen);
        }
    }

    public static int getNextFreeId() {
        for (int i = 1; i <= highestId; i++) {
            if (!screenMap.containsKey(i)) {
                return i;
            }
        }
        return highestId + 1;
    }

    public static void registerScreen(final MapScreen screen) {
        screenMap.put(screen.getId(), screen);
        if (screen.getFrameIds() != null) {
            Arrays.stream(screen.getFrameIds())
                    .flatMapToInt(Arrays::stream)
                    .mapToObj(i -> Bukkit.getWorlds().stream()
                            .flatMap(world -> world.getEntities().stream())
                            .filter(entity -> entity.getEntityId() == i)
                            .findAny()
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .map(entity -> entity.getLocation().getChunk())
                    .forEach(chunk -> chunk.addPluginChunkTicket(plugin));
        }
        if (screen.getId() > highestId) {
            highestId = screen.getId();
        }
    }

    public static void removeScreen(final int id) {
        final MapScreen screen = screenMap.remove(id);
        if (screen != null && screen.getFrameIds() != null) {
            Arrays.stream(screen.getFrameIds())
                    .flatMapToInt(Arrays::stream)
                    .mapToObj(i -> Bukkit.getWorlds().stream()
                            .flatMap(world -> world.getEntities().stream())
                            .filter(entity -> entity.getEntityId() == i)
                            .findAny()
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .map(entity -> entity.getLocation().getChunk())
                    .forEach(chunk -> chunk.removePluginChunkTicket(plugin));
        }
    }

    public static MapScreen getScreen(final int id) {
        return screenMap.get(id);
    }

    public static Collection<MapScreen> getScreens() {
        return screenMap.values();
    }

    public static Collection<Integer> getScreenIds() {
        return screenMap.keySet();
    }

}
