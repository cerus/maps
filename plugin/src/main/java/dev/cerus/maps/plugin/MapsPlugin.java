package dev.cerus.maps.plugin;

import co.aikar.commands.BukkitCommandManager;
import dev.cerus.maps.api.colormap.ColorMaps;
import dev.cerus.maps.api.font.MapFont;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.plugin.command.MapsCommand;
import dev.cerus.maps.plugin.dev.DevContext;
import dev.cerus.maps.plugin.dev.DevListener;
import dev.cerus.maps.plugin.dev.MapsDevCommand;
import dev.cerus.maps.plugin.listener.PlayerListener;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.plugin.task.ScreenUpdateTask;
import dev.cerus.maps.version.VersionAdapterFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.FileUtil;

public class MapsPlugin extends JavaPlugin {

    private boolean areScreensLoaded = false;
    private File screensConfigFile;
    private FileConfiguration screensConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        /*Mirror mirror = Mirror.builder()
                .withNameResolver(new NameResolver() {
                    private String ver;

                    {
                        String[] split = Bukkit.getServer().getClass().getName().split("\\.");
                        if (split.length != 5) {
                            ver = null;
                        } else {
                            ver = split[split.length - 2];
                        }
                    }

                    @Override
                    public String resolveClassName(Mirror mirror, String s) {
                        if (!s.contains("{{CB}}")) {
                            return s;
                        }
                        if (ver == null) {
                            return s.replace("{{CB}}", "").replace("..", ".");
                        }
                        return s.replace("{{CB}}", ver);
                    }

                    @Override
                    public String resolveMethodName(Mirror mirror, AdaptedClass adaptedClass, String s) {
                        return s;
                    }
                })
                .build();

        mirror.register(Common.class);
        mirror.register(VersionAdapterFallback.class);
        mirror.register(PacketHandlerFallback.class);*/

        VersionAdapter versionAdapter = new VersionAdapterFactory().makeAdapter();
        //VersionAdapter versionAdapter = new VersionAdapterFallback(mirror);
        if (versionAdapter == null) {
            Logger logger = this.getLogger();
            logger.severe("Invalid server version");
            logger.severe("This plugin is not compatible with this server version.");
            logger.severe("\"maps\" is compatible with %s - %s".formatted(VersionAdapterFactory.MIN_VER, VersionAdapterFactory.MAX_VER));

            this.getPluginLoader().disablePlugin(this);
            return;
        }

        FileConfiguration mapsConfig = this.getConfig();
        screensConfigFile = new File(getDataFolder(), "screens.yml");
        screensConfig = YamlConfiguration.loadConfiguration(screensConfigFile);

        Runnable load = () -> {
            if (screensConfig.contains("screens")) {
                this.getLogger().info("Loading screens..");
                MapScreenRegistry.load(screensConfig, versionAdapter);
                this.getLogger().info(MapScreenRegistry.getScreenIds().size() + " screens were loaded");
            }
            new ScreenUpdateTask().start(this);
            this.areScreensLoaded = true;
        };
        if (mapsConfig.contains("screens")) {
            FileUtil.copy(new File(getDataFolder(), "config.yml"), new File(getDataFolder(), "config.yml.backup"));

            // Migrate to new config
            if (mapsConfig.contains("version")) {
                screensConfig.set("version", mapsConfig.get("version"));
            }
            ConfigurationSection screensSection = mapsConfig.getConfigurationSection("screens");
            screensConfig.set("screens", screensSection);
            try {
                screensConfig.save(new File(getDataFolder(), "screens.yml"));
                getLogger().info("Migrated screens from config.yml to screens.yml");
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed to migrate screens", e);
            }

            mapsConfig.getKeys(false).forEach(k -> mapsConfig.set(k, null));
            File oldMapsConfFile = new File(getDataFolder(), "config.yml");
            FileConfiguration oldMapsConf = YamlConfiguration.loadConfiguration(oldMapsConfFile);
            for (String key : oldMapsConf.getKeys(false)) {
                if (key.equals("screens")) {
                    continue;
                }
                mapsConfig.set(key, oldMapsConf.get(key));
            }
            saveConfig();
        }
        if (!screensConfig.contains("version")) {
            // Delayed map screen loading because loaded chunks do not
            // contain entities right after startup for some weird reason
            this.getServer().getScheduler().runTaskLater(this, load,
                    mapsConfig.getInt("loading-delay", 3) * 20L);
        } else {
            load.run();
        }

        BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.registerDependency(VersionAdapter.class, versionAdapter);
        commandManager.registerCommand(new MapsCommand());

        // Workaround for acf locale errors
        try {
            Field loggerField = commandManager.getClass().getDeclaredField("logger");
            loggerField.setAccessible(true);
            Logger acfLogger = (Logger) loggerField.get(commandManager);
            acfLogger.setLevel(Level.OFF);
            acfLogger.setFilter(record -> false);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            this.getLogger().log(Level.WARNING, "Failed to disable ACF logger", ex);
        }

        if (mapsConfig.getBoolean("enable-click-listener", false)) {
            boolean useTriangulation = mapsConfig.getBoolean("use-triangulation", true);
            this.getServer().getPluginManager().registerEvents(new PlayerListener(
                    versionAdapter,
                    useTriangulation,
                    mapsConfig.getDouble("max-click-dist", 10d)
            ), this);

            String clickStrategy = useTriangulation ? "Triangulation" : "Raycasting";
            this.getLogger().info("Strategy used for screen click handling: " + clickStrategy);
        }

        if (DevContext.ENABLED) {
            this.getLogger().info("You are running maps in a development environment");
            commandManager.registerCommand(new MapsDevCommand());
            this.getServer().getPluginManager().registerEvents(new DevListener(this), this);
        }

        // Force classes to initialize now
        this.doNothing(ColorMaps.class, MapFont.class);
    }

    @Override
    public void onDisable() {
        if (!this.areScreensLoaded) {
            return;
        }
        MapScreenRegistry.store(screensConfig);
        try {
            screensConfig.save(screensConfigFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to save screens", e);
        }
    }

    private void doNothing(Class<?>... unused) {
        // This method does nothing. Its only purpose is to trick the JVM into
        // initializing the passed classes.
    }

}
