package dev.cerus.maps.api.colormap.mapping;

import dev.cerus.maps.api.colormap.ColorMap;
import dev.cerus.maps.util.MinecraftVersion;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single version mapping
 *
 * @param version The version this mapping applies to
 * @param colors  The colors for this version
 */
public record MappingEntry(
        MinecraftVersion version,
        List<ColorMap.Color> colors
) implements Comparable<MappingEntry> {

    public void applyTo(List<ColorMap.Color> otherColorList) {
        this.colors().forEach(color -> {
            if (!otherColorList.contains(color)) {
                otherColorList.add(color);
            }
        });
    }

    public List<ColorMap.Color> copyColorList() {
        return new ArrayList<>(this.colors());
    }

    @Override
    public int compareTo(MappingEntry other) {
        if (other == null) {
            throw new NullPointerException();
        }
        return this.version().compareTo(other.version());
    }

}
