package dev.cerus.maps.plugin.dev;

import dev.cerus.maps.plugin.MapsPlugin;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Static context for development operations, do not use in production
 */
public class DevContext {

    public static final Set<Integer> DEV_SCREENS = new HashSet<>();
    public static final boolean ENABLED = new File(JavaPlugin.getPlugin(MapsPlugin.class).getDataFolder(), "DEVENV").exists();

    private DevContext() {
    }

}
