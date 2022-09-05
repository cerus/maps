package dev.cerus.maps.api.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Converts regular Java fonts into MapFonts
 */
public class FontConverter {

    /**
     * All Ascii chars
     */
    public static final String ASCII = IntStream.range(26, 127)
            .mapToObj(v -> (char) v)
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString();

    /**
     * All German umlauts
     */
    public static final String UMLAUTS = "ÄäÖöÜü";

    /**
     * Sharp s ("Eszett", "scharfes S")
     */
    public static final String SHARP_S = "ẞß";

    private FontConverter() {
        throw new UnsupportedOperationException();
    }

    /**
     * Convert the specified font into a MapFont. Since we can't get a list of supported codepoints from
     * the font object you have to specify each individual character that you want us to convert.
     * <p>
     * Unsupported codepoints will be ignored.
     *
     * @param font        The Java font
     * @param textToCheck The characters that you want us to convert
     *
     * @return The converted font
     */
    public static MapFont convert(final Font font, final String textToCheck) {
        final List<Integer> supportedCodepoints = textToCheck.codePoints()
                .filter(font::canDisplay)
                .boxed()
                .toList();
        return convert(font, supportedCodepoints);
    }

    /**
     * Convert the specified font into a MapFont. Since we can't get a list of supported codepoints from
     * the font object you have to specify each individual character that you want us to convert.
     * <p>
     * Unsupported codepoints will be ignored.
     *
     * @param font       The Java font
     * @param codepoints The codepoints that you want us to convert
     *
     * @return The converted font
     */
    public static MapFont convert(final Font font, final Collection<Integer> codepoints) {
        final List<Integer> supportedCodepoints = codepoints.stream()
                .filter(font::canDisplay)
                .toList();

        final MapFont mapFont = new MapFont();
        for (final int cp : supportedCodepoints) {
            final BufferedImage img = toImage(font, cp);
            if (img == null) {
                continue;
            }
            final Sprite sprite = makeSprite(img);
            mapFont.set(cp, sprite);
        }
        return mapFont;
    }

    /**
     * Convert the specified font into a MapFont. Since we can't get a list of supported codepoints from
     * the font object you have to specify each individual character that you want us to convert.
     * <p>
     * Unsupported codepoints will be ignored.
     *
     * @param font    The Java font
     * @param lowest  The lowest codepoint
     * @param highest The highest codepoint
     *
     * @return The converted font
     */
    public static MapFont convert(final Font font, final int lowest, final int highest) {
        final MapFont mapFont = new MapFont();
        for (int cp = lowest; cp <= highest; cp++) {
            final BufferedImage img = toImage(font, cp);
            if (img == null) {
                continue;
            }
            final Sprite sprite = makeSprite(img);
            mapFont.set(cp, sprite);
        }
        return mapFont;
    }

    /**
     * Convert the specified font into a MapFont. This method will attempt to convert
     * all unicode characters.
     * <p>
     * Unsupported codepoints will be ignored.
     *
     * @param font The Java font
     *
     * @return The converted font
     */
    public static MapFont convertAllUnicode(final Font font) {
        return convert(font, 0x00000, 0x10FFFF);
    }

    /**
     * Draw a single codepoint on an image
     *
     * @param font The parent font
     * @param cp   The codepoint
     *
     * @return The image
     */
    private static BufferedImage toImage(final Font font, final int cp) {
        // Get bounds of the codepoint (why does this have to be so complicated)
        BufferedImage image = newImg(1, 1);
        Graphics2D graphics = image.createGraphics();
        final Rectangle2D bounds = font.getStringBounds(new String(Character.toChars(cp)), graphics.getFontMetrics().getFontRenderContext());
        graphics.dispose();
        if (bounds.getWidth() <= 0 || bounds.getHeight() <= 0) {
            return null;
        }

        // Create image with correct size
        image = newImg((int) Math.ceil(bounds.getWidth()), (int) Math.ceil(bounds.getHeight()));
        graphics = image.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.setFont(font);
        graphics.drawString(new String(Character.toChars(cp)), 0, graphics.getFontMetrics().getAscent());
        graphics.dispose();
        return image;
    }

    /**
     * Create a new image with the specified bounds
     *
     * @param w The width of the image
     * @param h The height of the image
     *
     * @return A new image
     */
    private static BufferedImage newImg(final int w, final int h) {
        return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Convert an image into a MapFont sprite
     *
     * @param image The image
     *
     * @return The sprite
     */
    private static Sprite makeSprite(final BufferedImage image) {
        final boolean[] data = new boolean[image.getWidth() * image.getHeight()];
        final Sprite sprite = new Sprite(image.getWidth(), image.getHeight(), data);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                final Color color = new Color(image.getRGB(x, y), true);
                data[y * image.getWidth() + x] = color.getAlpha() >= 1;
            }
        }

        return sprite;
    }

}
