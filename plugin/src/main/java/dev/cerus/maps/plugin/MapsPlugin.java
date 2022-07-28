package dev.cerus.maps.plugin;

import co.aikar.commands.BukkitCommandManager;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.plugin.command.MapsCommand;
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
            logger.severe("\"maps\" is compatible with 1.16.5 - 1.19.1");

            this.getPluginLoader().disablePlugin(this);
            return;
        }

        // Delayed map screen loading because loaded chunks do not
        // contain entities right after startup for some weird reason
        this.getServer().getScheduler().runTaskLater(this, () -> {
            final FileConfiguration config = this.getConfig();
            if (config.contains("screens")) {
                this.getLogger().info("Loading screens..");
                MapScreenRegistry.load(config, versionAdapter);
                this.getLogger().info(MapScreenRegistry.getScreenIds().size() + " screens were loaded");
            }
            this.areScreensLoaded = true;
        }, mapsConfig.getInt("loading-delay", 3) * 20L);

        final BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.registerDependency(VersionAdapter.class, versionAdapter);
        commandManager.registerCommand(new MapsCommand());
    }

    @Override
    public void onDisable() {
        if (this.areScreensLoaded) {
            MapScreenRegistry.store(this.getConfig());
        }
        this.saveConfig();
    }

}
