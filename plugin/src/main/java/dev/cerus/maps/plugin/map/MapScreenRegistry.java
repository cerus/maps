package dev.cerus.maps.plugin.map;

import dev.cerus.maps.api.Frame;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.plugin.MapsPlugin;
import dev.cerus.maps.util.EntityIdUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public class MapScreenRegistry {

    private static final JavaPlugin plugin = JavaPlugin.getPlugin(MapsPlugin.class);
    private static final Map<Integer, MapScreen> screenMap = new HashMap<>();
    private static int highestId = 0;

    public static void store(final ConfigurationSection configuration) {
        MapScreenStorage.storeV1(configuration, screenMap);
    }

    public static void load(final ConfigurationSection configuration, final VersionAdapter versionAdapter) {
        final Optional<Integer> versionOpt = MapScreenStorage.determineVersion(configuration);
        if (versionOpt.isEmpty()) {
            final AtomicInteger highestIdAtomic = new AtomicInteger(highestId);
            MapScreenStorage.loadLegacy(configuration, versionAdapter, plugin, highestIdAtomic, screenMap);
            highestId = highestIdAtomic.get();

            // Remove old frames
            for (final MapScreen screen : screenMap.values()) {
                Arrays.stream(screen.getFrameIds())
                        .flatMapToInt(Arrays::stream)
                        .mapToObj(i -> Bukkit.getWorlds().stream()
                                .flatMap(world -> world.getEntities().stream())
                                .filter(entity -> entity.getEntityId() == i)
                                .findAny()
                                .orElse(null))
                        .filter(Objects::nonNull)
                        .filter(e -> e.isValid() && !e.isDead())
                        .forEach(Entity::remove);
                for (int x = 0; x < screen.getWidth(); x++) {
                    for (int y = 0; y < screen.getHeight(); y++) {
                        final Frame frame = screen.getFrames()[x][y];
                        screen.getFrames()[x][y] = new Frame(
                                frame.getWorld(),
                                frame.getPosX(),
                                frame.getPosY(),
                                frame.getPosZ(),
                                frame.getFacing(),
                                EntityIdUtil.next(),
                                frame.isVisible(),
                                frame.isGlowing()
                        );
                    }
                }
            }

            store(configuration);
            plugin.saveConfig();
        } else {
            final int ver = versionOpt.get();
            if (ver != 1) {
                throw new IllegalStateException("Unknown data version " + ver);
            }

            final AtomicInteger highestIdAtomic = new AtomicInteger(highestId);
            MapScreenStorage.loadV1(configuration, versionAdapter, highestIdAtomic, screenMap);
            highestId = highestIdAtomic.get();
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
        /*if (screen.getFrameIds() != null) {
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
        }*/
        if (screen.getId() > highestId) {
            highestId = screen.getId();
        }
    }

    public static void removeScreen(final int id) {
        final MapScreen screen = screenMap.remove(id);
        /*if (screen != null && screen.getFrameIds() != null) {
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
        }*/
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
