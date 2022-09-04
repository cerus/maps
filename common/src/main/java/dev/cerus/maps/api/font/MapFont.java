package dev.cerus.maps.api.font;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.map.MinecraftFont;

/**
 * Represents a font
 */
public class MapFont {

    public static final MapFont MINECRAFT_FONT = fromBukkit(MinecraftFont.Font);

    private final Map<Integer, Sprite> codePointMap = new HashMap<>();
    private int maxHeight;

    /**
     * Convert Bukkit font to maps font
     *
     * @param bukkitFont The Bukkit font
     *
     * @return The converted font
     */
    public static MapFont fromBukkit(final org.bukkit.map.MapFont bukkitFont) {
        final MapFont mapFont = new MapFont();
        try {
            final Field charsField = org.bukkit.map.MapFont.class.getDeclaredField("chars");
            charsField.setAccessible(true);
            final Map<Character, org.bukkit.map.MapFont.CharacterSprite> spriteMap
                    = (Map<Character, org.bukkit.map.MapFont.CharacterSprite>) charsField.get(bukkitFont);
            spriteMap.forEach((character, characterSprite) ->
                    mapFont.set(String.valueOf(character).codePointAt(0), Sprite.fromBukkit(characterSprite)));
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to convert Bukkit font into maps font", e);
        }
        return mapFont;
    }

    /**
     * Register a codepoint
     *
     * @param codePoint The codepoint
     * @param sprite    The sprite
     */
    public void set(final int codePoint, final Sprite sprite) {
        this.codePointMap.put(codePoint, sprite);
        if (sprite.getHeight() > this.maxHeight) {
            this.maxHeight = sprite.getHeight();
        }
    }

    /**
     * Get a registered sprite
     *
     * @param codePoint The codepoint registered to the sprite
     *
     * @return The sprite
     *
     * @throws IllegalArgumentException if the codepoint was not registered
     */
    public Sprite get(final int codePoint) {
        if (!this.isValid(codePoint)) {
            throw new IllegalArgumentException("Invalid code point");
        }
        return this.codePointMap.get(codePoint);
    }

    /**
     * Get all sprites for the specified text
     *
     * @param text The text
     *
     * @return A list of sprites
     *
     * @throws IllegalArgumentException if the text is invalid
     */
    public List<Sprite> getSprites(final String text) {
        if (!this.isValid(text)) {
            throw new IllegalArgumentException("Unsupported chars");
        }
        return text.codePoints().mapToObj(this::get).toList();
    }

    /**
     * Get the width of a string
     *
     * @param text The string
     *
     * @return The width of the string
     *
     * @throws IllegalArgumentException if the text is invalid
     */
    public int getWidth(final String text) {
        if (text == null || text.length() == 0 || !this.isValid(text)) {
            throw new IllegalArgumentException("Invalid text");
        }
        return text.codePoints().map(cp -> this.codePointMap.get(cp).getWidth()).sum() + text.length() - 1;
    }

    /**
     * Get the maximum height of this font
     *
     * @return The max height
     */
    public int getHeight() {
        return this.getHeight(null);
    }

    /**
     * Get the height for a string
     *
     * @param text The string
     *
     * @return The height of the string
     *
     * @throws IllegalArgumentException if the text is invalid
     */
    public int getHeight(final String text) {
        if (text == null) {
            return this.maxHeight;
        }
        if (!this.isValid(text)) {
            throw new IllegalArgumentException("Unsupported characters");
        }
        return text.codePoints().map(cp -> this.codePointMap.get(cp).getHeight()).max().orElse(0);
    }

    /**
     * Checks if a codepoint was registered
     *
     * @param codePoint The codepoint
     *
     * @return True if valid
     */
    public boolean isValid(final int codePoint) {
        return this.codePointMap.containsKey(codePoint);
    }

    /**
     * Checks if a character was registered
     *
     * @param c The character
     *
     * @return True if valid
     */
    public boolean isValid(final char c) {
        return this.isValid(String.valueOf(c).codePointAt(0));
    }

    /**
     * Checks if a string is valid
     *
     * @param text The string
     *
     * @return True if valid
     */
    public boolean isValid(final String text) {
        return text.codePoints().allMatch(this::isValid);
    }

}
