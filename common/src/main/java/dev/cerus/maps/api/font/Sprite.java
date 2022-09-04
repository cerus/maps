package dev.cerus.maps.api.font;

import org.bukkit.map.MapFont;

/**
 * Represents a single character / codepoint
 */
public class Sprite {

    private final int width;
    private final int height;
    private final boolean[] grid;

    public Sprite(final int width, final int height) {
        this(width, height, new boolean[width * height]);
    }

    public Sprite(final int width, final int height, final boolean[] grid) {
        if (grid.length != width * height) {
            throw new IllegalArgumentException();
        }
        this.width = width;
        this.height = height;
        this.grid = grid;
    }

    /**
     * Convert Bukkit sprite into maps sprite
     *
     * @param bukkitSprite The Bukkit sprite
     *
     * @return The converted sprite
     */
    public static Sprite fromBukkit(final MapFont.CharacterSprite bukkitSprite) {
        final boolean[] data = new boolean[bukkitSprite.getWidth() * bukkitSprite.getHeight()];
        for (int x = 0; x < bukkitSprite.getWidth(); x++) {
            for (int y = 0; y < bukkitSprite.getHeight(); y++) {
                data[y * bukkitSprite.getWidth() + x] = bukkitSprite.get(y, x);
            }
        }
        return new Sprite(bukkitSprite.getWidth(), bukkitSprite.getHeight(), data);
    }

    /**
     * Get a pixel
     *
     * @param row The row
     * @param col The column
     *
     * @return The pixel (true = visible, false = invisible)
     */
    public boolean get(final int row, final int col) {
        if (row < 0 || col < 0 || row >= this.height || col >= this.width) {
            return false;
        }
        return this.grid[row * this.width + col];
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean[] getGrid() {
        return this.grid;
    }

}
