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
import dev.cerus.maps.version.VersionAdapterFactory;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MapsPlugin extends JavaPlugin {

    private boolean areScreensLoaded = false;

    @Override
    public void onEnable() {
        this.saveResource("maps_config.yml", false);
        final YamlConfiguration mapsConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "maps_config.yml"));

        final VersionAdapter versionAdapter = new VersionAdapterFactory().makeAdapter();
        if (versionAdapter == null) {
            final Logger logger = this.getLogger();
            logger.severe("Invalid server version");
            logger.severe("This plugin is not compatible with this server version.");
            logger.severe("\"maps\" is compatible with %s - %s".formatted(VersionAdapterFactory.MIN_VER, VersionAdapterFactory.MAX_VER));

            this.getPluginLoader().disablePlugin(this);
            return;
        }

        final FileConfiguration config = this.getConfig();
        final Runnable load = () -> {
            if (config.contains("screens")) {
                this.getLogger().info("Loading screens..");
                MapScreenRegistry.load(config, versionAdapter);
                this.getLogger().info(MapScreenRegistry.getScreenIds().size() + " screens were loaded");
            }
            this.areScreensLoaded = true;
        };
        if (!config.contains("version")) {
            // Delayed map screen loading because loaded chunks do not
            // contain entities right after startup for some weird reason
            this.getServer().getScheduler().runTaskLater(this, load,
                    mapsConfig.getInt("loading-delay", 3) * 20L);
        } else {
            load.run();
        }

        final BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.registerDependency(VersionAdapter.class, versionAdapter);
        commandManager.registerCommand(new MapsCommand());

        if (mapsConfig.getBoolean("enable-click-listener", false)) {
            final boolean useTriangulation = mapsConfig.getBoolean("use-triangulation", true);
            this.getServer().getPluginManager().registerEvents(new PlayerListener(
                    versionAdapter,
                    useTriangulation,
                    mapsConfig.getDouble("max-click-dist", 10d)
            ), this);

            final String clickStrategy = useTriangulation ? "Triangulation" : "Raycasting";
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
        if (this.areScreensLoaded) {
            MapScreenRegistry.store(this.getConfig());
        }
        this.saveConfig();
    }

    private void doNothing(final Class<?>... unused) {
        // This method does nothing. Its only purpose is to trick the JVM into
        // initializing the passed classes.
    }

}
