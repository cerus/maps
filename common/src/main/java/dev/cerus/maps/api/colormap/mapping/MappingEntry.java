package dev.cerus.maps.api.colormap.mapping;

import dev.cerus.maps.api.colormap.ColorMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single version mapping
 *
 * @param version The version this mapping applies to
 * @param colors  The colors for this version
 */
public record MappingEntry(
        Version version,
        List<ColorMap.Color> colors
) implements Comparable<MappingEntry> {

    public void applyTo(final List<ColorMap.Color> otherColorList) {
        otherColorList.addAll(this.colors());
    }

    public List<ColorMap.Color> copyColorList() {
        return new ArrayList<>(this.colors());
    }

    @Override
    public int compareTo(final MappingEntry other) {
        if (other == null) {
            throw new NullPointerException();
        }
        return this.version().compareTo(other.version());
    }

}
