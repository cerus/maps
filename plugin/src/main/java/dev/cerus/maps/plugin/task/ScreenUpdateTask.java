package dev.cerus.maps.plugin.task;

import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ScreenUpdateTask extends BukkitRunnable {
    private int elapsedTicks;

    @Override
    public void run() {
        if (elapsedTicks == Integer.MAX_VALUE) {
            elapsedTicks = 0;
        }
        for (MapScreen screen : MapScreenRegistry.getScreens()) {
            int refreshRate = screen.getRefreshRate();
            if (refreshRate == 0) {
                continue;
            }
            if (elapsedTicks % refreshRate != 0) {
                continue;
            }
            screen.update();
            screen.clearDirty();
        }
        elapsedTicks++;
    }

    public void start(JavaPlugin plugin) {
        runTaskTimer(plugin, 0, 1);
    }
}
