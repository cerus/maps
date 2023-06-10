package dev.cerus.maps.api.colormap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.cerus.maps.api.colormap.mapping.MappingEntry;
import dev.cerus.maps.api.colormap.mapping.MappingTree;
import dev.cerus.maps.api.colormap.mapping.Version;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;

/**
 * Utility class for color maps
 */
public class ColorMaps {

    private static final MappingTree MAPPING_TREE = loadMappings();
    private static final ColorMap latest = newColorMap(Version.MAX);
    private static final ColorMap current = newColorMap(currentVersion());

    private ColorMaps() {
    }

    /**
     * Get a color map for the latest Minecraft version
     *
     * @return Color map for latest version
     */
    public static ColorMap latest() {
        return latest;
    }

    /**
     * Get a color map for the Minecraft version this server is running
     *
     * @return Color map for current version
     */
    public static ColorMap current() {
        return current;
    }

    /**
     * Create a new color map for the specified Minecraft version
     *
     * @param version the Minecraft version to create a color map for
     *
     * @return a new color map
     */
    public static ColorMap newColorMap(final Version version) {
        return MAPPING_TREE.createColorMap(version);
    }

    /**
     * Load the current color mappings into memory
     *
     * @return a mapping tree
     */
    private static MappingTree loadMappings() {
        final List<MappingEntry> mappingEntries = new ArrayList<>();
        try (final InputStream in = ColorMaps.class.getClassLoader().getResourceAsStream("colormap.json");
             final InputStreamReader reader = new InputStreamReader(in)) {
            final JsonObject obj = new JsonParser().parse(reader).getAsJsonObject();

            // Iterate versions
            for (final Map.Entry<String, JsonElement> verEntry : obj.entrySet()) {
                final String versionString = verEntry.getKey();
                final String[] versionSplit = versionString.split("\\.");
                final Version version = new Version(
                        Integer.parseInt(versionSplit[0]),
                        Integer.parseInt(versionSplit[1]),
                        versionSplit.length > 2 ? Integer.parseInt(versionSplit[2]) : 0
                );

                // Iterate colors
                final List<ColorMap.Color> colorList = new ArrayList<>();
                final JsonObject verObj = verEntry.getValue().getAsJsonObject();
                for (final Map.Entry<String, JsonElement> colorEntry : verObj.entrySet()) {
                    final int baseId = Integer.parseInt(colorEntry.getKey());
                    final JsonObject item = colorEntry.getValue().getAsJsonObject();
                    final JsonArray colors = item.getAsJsonArray("colors");

                    int i = 0;
                    for (final JsonElement element : colors) {
                        final int colorRaw = element.getAsInt();
                        colorList.add(new ColorMap.Color(
                                (byte) (baseId * 4 + i++),
                                new Color(colorRaw, false)
                        ));
                    }
                }
                mappingEntries.add(new MappingEntry(version, colorList));
            }
        } catch (final IOException | NullPointerException e) {
            System.err.println("Failed to load colors");
        }

        final MappingTree tree = new MappingTree();
        tree.addEntries(mappingEntries);
        return tree;
    }

    /**
     * Get the currently running server version
     *
     * @return current server version
     */
    private static Version currentVersion() {
        String version = Bukkit.getVersion();
        version = version.substring(version.indexOf("MC:") + 4, version.length() - 1).trim();
        if (!version.matches("\\d+\\.\\d+(\\.\\d+)")) {
            return Version.ZERO;
        }

        final String[] split = version.split("\\.");
        final int major = Integer.parseInt(split[0]);
        final int minor = Integer.parseInt(split[1]);
        final int patch = split.length > 2 ? Integer.parseInt(split[2]) : 0;
        return new Version(major, minor, patch);
    }

    /**
     * Get a new color map for the specified color version
     *
     * @param version The version
     *
     * @return A new color map
     *
     * @deprecated use {@link ColorMaps#newColorMap(Version)} instead
     */
    @Deprecated(forRemoval = true)
    public static ColorMap newColorMap(final ColorVersion version) {
        return switch (version) {
            case MC_1_8 -> newColorMap(new Version(1, 8, 0));
            case MC_1_12 -> newColorMap(new Version(1, 12, 0));
            case MC_1_16 -> newColorMap(new Version(1, 16, 0));
            case MC_1_17 -> newColorMap(new Version(1, 17, 0));
        };
    }

    /**
     * Determine an appropriate color version for this server
     *
     * @return A color version
     *
     * @deprecated ColorVersion is no longer used and marked for removal
     */
    @Deprecated(forRemoval = true)
    public static ColorVersion determineVersion() {
        final Version version = currentVersion();
        if (version.major() != 1) {
            return ColorVersion.MC_1_12;
        }
        if (version.minor() < 12) {
            return ColorVersion.MC_1_8;
        } else if (version.minor() < 16) {
            return ColorVersion.MC_1_12;
        } else if (version.minor() < 17) {
            return ColorVersion.MC_1_16;
        } else {
            return ColorVersion.MC_1_17;
        }
    }


}
