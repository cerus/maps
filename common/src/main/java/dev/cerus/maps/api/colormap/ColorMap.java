package dev.cerus.maps.api.colormap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * A container for map colors
 */
public class ColorMap {

    // Color array, capped at 256 because that's the maximum amount of possible colors at the moment
    private final Color[] colors = new Color[256];

    public void putColor(final Color color) {
        this.colors[this.b2i(color.mapColor)] = color;
    }

    /**
     * Find color by id
     *
     * @param id Map color id
     *
     * @return Color or null
     */
    public Color fromId(final int id) {
        return this.getById(id);
    }

    /**
     * Find color by id
     *
     * @param id Map color id
     *
     * @return Color or null
     */
    public Color getById(int id) {
        id = this.b2i(id);
        return (id < 0 || id >= this.colors.length) ? null : this.colors[id];
    }

    /**
     * Find java color by id
     *
     * @param color Map color id
     *
     * @return Java color or java color id 0 (or null)
     */
    public java.awt.Color mapColorToRgb(final byte color) {
        return Optional.ofNullable(this.getById(color)).map(c -> c.javaColor)
                .orElse(Optional.ofNullable(getById(0)).map(c -> c.javaColor)
                        .orElse(null));
    }

    /**
     * Find color by RGB
     *
     * @param r Red
     * @param g Green
     * @param b Blue
     *
     * @return Color or transparent color (0) (or null)
     */
    public Color rgbToMapColor(final int r, final int g, final int b) {
        return Arrays.stream(this.colors)
                .filter(Objects::nonNull)
                .filter(color -> color.mapColor() > 3)
                .min(Comparator.comparingDouble(value -> this.calcDist(value, r, g, b)))
                .orElse(this.getById(0));
    }

    private double calcDist(final Color clr, final int r, final int g, final int b) {
        final java.awt.Color color = clr.javaColor();
        return Math.sqrt(Math.pow(r - color.getRed(), 2) + Math.pow(g - color.getGreen(), 2) + Math.pow(b - color.getBlue(), 2));
    }

    private int b2i(int b) {
        if (b < 0) {
            b += 256;
        }
        return b;
    }

    public Color[] getColors() {
        return this.colors;
    }

    public record Color(byte mapColor, java.awt.Color javaColor) {

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final Color color = (Color) o;
            return this.mapColor() == color.mapColor() && Objects.equals(this.javaColor(), color.javaColor());
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.mapColor(), this.javaColor());
        }

    }

}
