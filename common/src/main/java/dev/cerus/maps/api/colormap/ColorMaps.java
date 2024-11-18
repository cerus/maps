package dev.cerus.maps.api.colormap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.cerus.maps.api.colormap.mapping.MappingEntry;
import dev.cerus.maps.api.colormap.mapping.MappingTree;
import dev.cerus.maps.util.MinecraftVersion;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for color maps
 */
public class ColorMaps {

    private static final MappingTree MAPPING_TREE = loadMappings();
    private static final ColorMap LATEST = newColorMap(MinecraftVersion.MAX);
    private static final ColorMap CURRENT = newColorMap(MinecraftVersion.current());

    private ColorMaps() {
    }

    /**
     * Get a color map for the latest Minecraft version
     *
     * @return Color map for latest version
     */
    public static ColorMap latest() {
        return LATEST;
    }

    /**
     * Get a color map for the Minecraft version this server is running
     *
     * @return Color map for current version
     */
    public static ColorMap current() {
        return CURRENT;
    }

    /**
     * Create a new color map for the specified Minecraft version
     *
     * @param version the Minecraft version to create a color map for
     *
     * @return a new color map
     */
    public static ColorMap newColorMap(MinecraftVersion version) {
        return MAPPING_TREE.createColorMap(version);
    }

    /**
     * Load the current color mappings into memory
     *
     * @return a mapping tree
     */
    private static MappingTree loadMappings() {
        List<MappingEntry> mappingEntries = new ArrayList<>();
        try (InputStream in = ColorMaps.class.getClassLoader().getResourceAsStream("colormap.json");
             InputStreamReader reader = new InputStreamReader(in)) {
            JsonObject obj = new JsonParser().parse(reader).getAsJsonObject();

            // Iterate versions
            for (Map.Entry<String, JsonElement> verEntry : obj.entrySet()) {
                String versionString = verEntry.getKey();
                MinecraftVersion version = MinecraftVersion.parse(versionString);

                // Iterate colors
                List<ColorMap.Color> colorList = new ArrayList<>();
                JsonObject verObj = verEntry.getValue().getAsJsonObject();
                for (Map.Entry<String, JsonElement> colorEntry : verObj.entrySet()) {
                    int baseId = Integer.parseInt(colorEntry.getKey());
                    JsonObject item = colorEntry.getValue().getAsJsonObject();
                    JsonArray colors = item.getAsJsonArray("colors");

                    int i = 0;
                    for (JsonElement element : colors) {
                        int colorRaw = element.getAsInt();
                        colorList.add(new ColorMap.Color(
                                baseId * 4 + i++,
                                new Color(colorRaw, false)
                        ));
                    }
                }
                mappingEntries.add(new MappingEntry(version, colorList));
            }
        } catch (IOException | NullPointerException e) {
            System.err.println("Failed to load colors");
        }

        MappingTree tree = new MappingTree();
        tree.addEntries(mappingEntries);
        return tree;
    }
}
