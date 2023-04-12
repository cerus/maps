package dev.cerus.maps.plugin.map;

import dev.cerus.maps.api.Frame;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.util.EntityIdUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.plugin.java.JavaPlugin;

class MapScreenStorage {

    static Optional<Integer> determineVersion(final ConfigurationSection section) {
        return section.contains("version") ? Optional.of(section.getInt("version")) : Optional.empty();
    }

    static void storeV1(final ConfigurationSection configuration,
                        final Map<Integer, MapScreen> screenMap) {
        configuration.set("screens", null);
        final ConfigurationSection screensSection = configuration.createSection("screens");
        configuration.set("version", 1);
        for (final Map.Entry<Integer, MapScreen> entry : screenMap.entrySet()) {
            final int id = entry.getKey();
            final MapScreen screen = entry.getValue();
            if (screen.getFrames() == null) {
                continue;
            }

            final World world = screen.getFrames()[0][0].getWorld();
            final List<String> frameList = new ArrayList<>();
            for (int x = 0; x < screen.getWidth(); x++) {
                for (int y = 0; y < screen.getHeight(); y++) {
                    final Frame frame = screen.getFrames()[x][y];
                    frameList.add(frame.getPosX() + ";" + frame.getPosY() + ";"
                            + frame.getPosZ() + "/" + frame.getFacing().name()
                            + "/" + x + ";" + y + "/" + frame.isVisible() + "/"
                            + frame.isGlowing());
                }
            }

            final ConfigurationSection section = screensSection.createSection(String.valueOf(id));
            section.set("id", id);
            section.set("width", screen.getWidth());
            section.set("height", screen.getHeight());
            section.set("world", world.getUID().toString());
            section.set("frames", frameList);
        }
    }

    static void loadV1(final ConfigurationSection configuration,
                       final VersionAdapter versionAdapter,
                       final AtomicInteger highestId,
                       final Map<Integer, MapScreen> screenMap) {
        final ConfigurationSection screensSection = configuration.getConfigurationSection("screens");
        for (final String key : screensSection.getKeys(false)) {
            final ConfigurationSection section = screensSection.getConfigurationSection(key);
            final int id = section.getInt("id");
            final int width = section.getInt("width");
            final int height = section.getInt("height");

            final UUID worldId = UUID.fromString(section.getString("world"));
            final World world = Bukkit.getWorld(worldId);

            final List<String> framesList = section.getStringList("frames");
            final Frame[][] frames = new Frame[width][height];
            // - "POSX;POSY;POSZ/FACING/IDX;IDY/VISIBLE"
            for (final String frameStr : framesList) {
                final String[] itemSplit = frameStr.split("/");
                final String[] posSplit = itemSplit[0].split(";");
                final String[] idSplit = itemSplit[2].split(";");
                final BlockFace facing = BlockFace.valueOf(itemSplit[1]);
                final int fx = Integer.parseInt(idSplit[0]);
                final int fy = Integer.parseInt(idSplit[1]);
                frames[fx][fy] = new Frame(
                        world,
                        Integer.parseInt(posSplit[0]),
                        Integer.parseInt(posSplit[1]),
                        Integer.parseInt(posSplit[2]),
                        facing,
                        EntityIdUtil.next(),
                        itemSplit.length < 4 || Boolean.parseBoolean(itemSplit[3]),
                        itemSplit.length >= 5 && Boolean.parseBoolean(itemSplit[4])
                );
            }

            final MapScreen mapScreen = new MapScreen(id, versionAdapter, width, height);
            mapScreen.setFrames(frames);
            if (id > highestId.get()) {
                highestId.set(id);
            }
            screenMap.put(id, mapScreen);
        }
    }

    static void storeLegacy(final ConfigurationSection configuration,
                            final Map<Integer, MapScreen> screenMap) {
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

    static void loadLegacy(final ConfigurationSection configuration,
                           final VersionAdapter versionAdapter,
                           final JavaPlugin plugin,
                           final AtomicInteger highestId,
                           final Map<Integer, MapScreen> screenMap) {
        final ConfigurationSection screensSection = configuration.getConfigurationSection("screens");
        screenLoop:
        for (final String key : screensSection.getKeys(false)) {
            final ConfigurationSection section = screensSection.getConfigurationSection(key);
            final int id = section.getInt("id");
            final int width = section.getInt("width");
            final int height = section.getInt("height");
            Location location = null;

            final UUID worldId = UUID.fromString(section.getString("world"));
            final World world = Bukkit.getWorld(worldId);

            final Set<Chunk> tempChunkSet = new HashSet<>();
            final Frame[][] frames = new Frame[width][height];
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

                if (location == null) {
                    location = itemFrame.getLocation().clone();
                }

                frames[Integer.parseInt(posSplit[0])][Integer.parseInt(posSplit[1])] = new Frame(
                        itemFrame.getWorld(),
                        itemFrame.getLocation().getBlockX(),
                        itemFrame.getLocation().getBlockY(),
                        itemFrame.getLocation().getBlockZ(),
                        itemFrame.getFacing(),
                        itemFrame.getEntityId(),
                        itemFrame.isVisible(),
                        itemFrame.getType().name().equals("GLOW_ITEM_FRAME")
                );
                tempChunkSet.add(chunk);
            }
            for (final Chunk chunk : tempChunkSet) {
                chunk.addPluginChunkTicket(plugin);
            }

            final MapScreen mapScreen = new MapScreen(id, versionAdapter, width, height);
            mapScreen.setLocation(location);
            mapScreen.setFrames(frames);
            if (id > highestId.get()) {
                highestId.set(id);
            }
            screenMap.put(id, mapScreen);
        }
    }

}
