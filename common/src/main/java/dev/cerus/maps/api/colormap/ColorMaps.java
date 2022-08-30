package dev.cerus.maps.api.colormap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import org.bukkit.Bukkit;

/**
 * Utility class for color maps
 */
public class ColorMaps {

    private static final ColorVersion FALLBACK = ColorVersion.MC_1_12;
    private static final ColorMap latest = newColorMap(ColorVersion.latest());
    private static final ColorMap current = newColorMap(determineVersion());

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
     * Get a new color map for the specified color version
     *
     * @param version The version
     *
     * @return A new color map
     */
    public static ColorMap newColorMap(final ColorVersion version) {
        final ColorMap colorMap = new ColorMap();
        if (version == ColorVersion.latest()) {
            // Load colors from jar
            try (final InputStream in = ColorMaps.class.getClassLoader().getResourceAsStream("latest_colors.json");
                 final InputStreamReader reader = new InputStreamReader(in)) {
                final JsonObject obj = new JsonParser().parse(reader).getAsJsonObject();
                for (final Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                    final int baseId = Integer.parseInt(entry.getKey());
                    final JsonObject item = entry.getValue().getAsJsonObject();
                    final JsonArray colors = item.getAsJsonArray("colors");

                    int i = 0;
                    for (final JsonElement element : colors) {
                        final int colorRaw = element.getAsInt();
                        colorMap.putColor(new ColorMap.Color(
                                (byte) (baseId * 4 + i++),
                                new Color(colorRaw, false)
                        ));
                    }
                }
            } catch (final IOException | NullPointerException e) {
                System.err.println("Failed to load colors");
            }
        } else if (latest != null) {
            // Copy the colors from the latest color map
            for (final ColorMap.Color color : latest.getColors()) {
                if (color != null && ((int) color.mapColor()) / 4 <= version.getLastId()) {
                    colorMap.putColor(color);
                }
            }
        }
        return colorMap;
    }

    /**
     * Determine an appropriate color version for this server
     *
     * @return A color version
     */
    public static ColorVersion determineVersion() {
        String version = Bukkit.getVersion();
        version = version.substring(version.indexOf("MC:") + 4, version.length() - 1).trim();
        if (!version.matches("\\d+\\.\\d+(\\.\\d+)")) {
            return FALLBACK;
        }

        final String[] split = version.split("\\.");
        final int major = Integer.parseInt(split[0]);
        final int minor = Integer.parseInt(split[1]);
        if (major != 1) {
            // FUTUREEEEE
            return FALLBACK;
        }
        if (minor < 12) {
            return ColorVersion.MC_1_8;
        } else if (minor < 16) {
            return ColorVersion.MC_1_12;
        } else if (minor < 17) {
            return ColorVersion.MC_1_16;
        } else {
            return ColorVersion.MC_1_17;
        }
    }


}
