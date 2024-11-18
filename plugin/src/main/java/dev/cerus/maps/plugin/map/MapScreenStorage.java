package dev.cerus.maps.plugin.map;

import dev.cerus.maps.api.Frame;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.version.VersionAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
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

    private static final Pattern UUID_PATTERN = Pattern.compile("[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}", Pattern.CASE_INSENSITIVE);

    static Optional<Integer> determineVersion(ConfigurationSection section) {
        return section.contains("version") ? Optional.of(section.getInt("version")) : Optional.empty();
    }

    static void storeV1(ConfigurationSection configuration,
                        Map<Integer, MapScreen> screenMap) {
        configuration.set("screens", null);
        ConfigurationSection screensSection = configuration.createSection("screens");
        configuration.set("version", 1);
        for (Map.Entry<Integer, MapScreen> entry : screenMap.entrySet()) {
            int id = entry.getKey();
            MapScreen screen = entry.getValue();
            if (screen.getFrames() == null) {
                continue;
            }

            World world = screen.getFrames()[0][0].getWorld();
            List<String> frameList = new ArrayList<>();
            for (int x = 0; x < screen.getWidth(); x++) {
                for (int y = 0; y < screen.getHeight(); y++) {
                    Frame frame = screen.getFrames()[x][y];
                    frameList.add("%d;%d;%d/%s/%d;%d/%s/%s".formatted(frame.getPosX(), frame.getPosY(), frame.getPosZ(),
                            frame.getFacing().name(), x, y, frame.isVisible(), frame.isGlowing()));
                }
            }

            ConfigurationSection section = screensSection.createSection(String.valueOf(id));
            section.set("id", id);
            section.set("width", screen.getWidth());
            section.set("height", screen.getHeight());
            section.set("refresh", screen.getRefreshRate());
            section.set("world", world.getName());
            section.set("frames", frameList);
        }
    }

    static void loadV1(ConfigurationSection configuration,
                       VersionAdapter versionAdapter,
                       AtomicInteger highestId,
                       Map<Integer, MapScreen> screenMap) {
        ConfigurationSection screensSection = configuration.getConfigurationSection("screens");
        for (String key : screensSection.getKeys(false)) {
            ConfigurationSection section = screensSection.getConfigurationSection(key);
            int id = section.getInt("id");
            int width = section.getInt("width");
            int height = section.getInt("height");
            int refreshRate = section.getInt("refresh", 1);

            String worldStr = section.getString("world");
            World world;
            if (UUID_PATTERN.matcher(worldStr).matches()) {
                UUID worldId = UUID.fromString(worldStr);
                world = Bukkit.getWorld(worldId);
            } else {
                world = Bukkit.getWorld(worldStr);
            }

            List<String> framesList = section.getStringList("frames");
            Frame[][] frames = new Frame[width][height];
            // - "POSX;POSY;POSZ/FACING/IDX;IDY/VISIBLE"
            for (String frameStr : framesList) {
                String[] itemSplit = frameStr.split("/");
                String[] posSplit = itemSplit[0].split(";");
                String[] idSplit = itemSplit[2].split(";");
                BlockFace facing = BlockFace.valueOf(itemSplit[1]);
                int fx = Integer.parseInt(idSplit[0]);
                int fy = Integer.parseInt(idSplit[1]);
                frames[fx][fy] = new Frame(
                        world,
                        Integer.parseInt(posSplit[0]),
                        Integer.parseInt(posSplit[1]),
                        Integer.parseInt(posSplit[2]),
                        facing,
                        versionAdapter.nextEntityId(),
                        itemSplit.length < 4 || Boolean.parseBoolean(itemSplit[3]),
                        itemSplit.length >= 5 && Boolean.parseBoolean(itemSplit[4])
                );
            }

            MapScreen mapScreen = new MapScreen(id, versionAdapter, width, height);
            mapScreen.setRefreshRate(refreshRate);
            mapScreen.setFrames(frames);
            if (id > highestId.get()) {
                highestId.set(id);
            }
            screenMap.put(id, mapScreen);
        }
    }

    static void storeLegacy(ConfigurationSection configuration,
                            Map<Integer, MapScreen> screenMap) {
        configuration.set("screens", null);
        ConfigurationSection screensSection = configuration.createSection("screens");
        screenLoop:
        for (Map.Entry<Integer, MapScreen> entry : screenMap.entrySet()) {
            int id = entry.getKey();
            MapScreen screen = entry.getValue();
            if (screen.getFrameIds() == null) {
                continue;
            }

            Entity[][] frames = new Entity[screen.getWidth()][screen.getHeight()];
            for (int x = 0; x < screen.getWidth(); x++) {
                for (int y = 0; y < screen.getHeight(); y++) {
                    int fid = screen.getFrameIds()[x][y];
                    Entity frame = Bukkit.getWorlds().stream()
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
            World world = frames[0][0].getWorld();

            ConfigurationSection section = screensSection.createSection(String.valueOf(id));
            section.set("id", id);
            section.set("width", screen.getWidth());
            section.set("height", screen.getHeight());
            section.set("world", world.getUID().toString());

            ConfigurationSection framesSection = section.createSection("frames");
            for (int x = 0; x < screen.getWidth(); x++) {
                for (int y = 0; y < screen.getHeight(); y++) {
                    Entity entity = frames[x][y];
                    Chunk chunk = entity.getLocation().getChunk();
                    framesSection.set(entity.getUniqueId().toString(), chunk.getX() + ";" + chunk.getZ() + "|" + x + ";" + y);
                }
            }
        }
    }

    static void loadLegacy(ConfigurationSection configuration,
                           VersionAdapter versionAdapter,
                           JavaPlugin plugin,
                           AtomicInteger highestId,
                           Map<Integer, MapScreen> screenMap) {
        ConfigurationSection screensSection = configuration.getConfigurationSection("screens");
        screenLoop:
        for (String key : screensSection.getKeys(false)) {
            ConfigurationSection section = screensSection.getConfigurationSection(key);
            int id = section.getInt("id");
            int width = section.getInt("width");
            int height = section.getInt("height");
            Location location = null;

            UUID worldId = UUID.fromString(section.getString("world"));
            World world = Bukkit.getWorld(worldId);

            Set<Chunk> tempChunkSet = new HashSet<>();
            Frame[][] frames = new Frame[width][height];
            ConfigurationSection framesSection = section.getConfigurationSection("frames");
            for (String frameIdStr : framesSection.getKeys(false)) {
                String chunkIdAndPosStr = framesSection.getString(frameIdStr);
                String[] chunkIdSplit = chunkIdAndPosStr.split("\\|")[0].split(";");
                String[] posSplit = chunkIdAndPosStr.split("\\|")[1].split(";");

                Chunk chunk = world.getChunkAt(Integer.parseInt(chunkIdSplit[0]), Integer.parseInt(chunkIdSplit[1]));
                chunk.load();

                ItemFrame itemFrame = Arrays.stream(chunk.getEntities())
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
            for (Chunk chunk : tempChunkSet) {
                chunk.addPluginChunkTicket(plugin);
            }

            MapScreen mapScreen = new MapScreen(id, versionAdapter, width, height);
            mapScreen.setLocation(location);
            mapScreen.setFrames(frames);
            if (id > highestId.get()) {
                highestId.set(id);
            }
            screenMap.put(id, mapScreen);
        }
    }

}
