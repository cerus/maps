package dev.cerus.maps.api.colormap.mapping;

import com.google.common.collect.Collections2;
import dev.cerus.maps.api.colormap.ColorMap;
import dev.cerus.maps.util.MinecraftVersion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.bukkit.Bukkit;

/**
 * A tree of color mappings, used to generate color maps for specific Minecraft versions
 */
public class MappingTree {

    private final Set<MappingEntry> entryList = new TreeSet<>();

    /**
     * Add mapping entries to the internal entry list
     *
     * @param entries The mappings to add
     */
    public void addEntries(Iterable<MappingEntry> entries) {
        for (MappingEntry entry : entries) {
            this.entryList.add(entry);
        }
    }

    /**
     * Create a color map for a specific Minecraft version
     *
     * @param version The Minecraft version to create the color map for
     *
     * @return a new color map
     */
    public ColorMap createColorMap(MinecraftVersion version) {
        List<ColorMap.Color> baseList = this.entryList.iterator().next().copyColorList();
        this.getAllForVer(version).forEach(entry -> entry.applyTo(baseList));

        ColorMap colorMap = new ColorMap();
        baseList.forEach(colorMap::putColor);
        return colorMap;
    }

    /**
     * Get all applicable mapping entries for a specific Minecraft version
     *
     * @param version The Minecraft version to get the mappings for
     *
     * @return all applicable mappings
     */
    private List<MappingEntry> getAllForVer(MinecraftVersion version) {
        List<MappingEntry> entries = new ArrayList<>();
        for (MappingEntry entry : this.entryList) {
            // entry.version() > version
            // Filters out any versions that are more recent than the specified version
            if (entry.version().greaterThan(version)) {
                continue;
            }
            entries.add(entry);
        }
        return entries;
    }

}
