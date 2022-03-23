package dev.cerus.maps.api.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.IntStream;
import org.bukkit.map.MapFont;

public class FontConverter {

    public static final List<Character> ASCII = List.copyOf(IntStream.range(26, 127)
            .mapToObj(v -> (char) v)
            .toList());

    private FontConverter() {
        throw new UnsupportedOperationException();
    }

    public static MapFont convert(final Font font, final List<Character> charsToCheck) {
        final List<Character> supportedChars = charsToCheck.stream()
                .filter(font::canDisplay)
                .toList();

        final MapFont mapFont = new MapFont();
        for (final char c : supportedChars) {
            final BufferedImage img = toImage(font, c);
            if (img == null) {
                continue;
            }
            final MapFont.CharacterSprite sprite = makeSprite(img);
            mapFont.setChar(c, sprite);
        }
        return mapFont;
    }

    private static BufferedImage toImage(final Font font, final char c) {
        BufferedImage image = newImg(1, 1);
        Graphics2D graphics = image.createGraphics();
        final Rectangle2D bounds = font.getStringBounds(String.valueOf(c), graphics.getFontMetrics().getFontRenderContext());
        graphics.dispose();
        if (bounds.getWidth() <= 0 || bounds.getHeight() <= 0) {
            return null;
        }

        image = newImg((int) bounds.getWidth(), (int) bounds.getHeight());
        graphics = image.createGraphics();
        graphics.setColor(new Color(0, 0, 0, 0));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.setColor(Color.BLACK);
        graphics.setFont(font);
        graphics.drawString(String.valueOf(c), 0, image.getHeight() - 1);
        graphics.dispose();
        return image;
    }

    private static BufferedImage newImg(final int w, final int h) {
        return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

    private static MapFont.CharacterSprite makeSprite(final BufferedImage image) {
        final boolean[] data = new boolean[image.getWidth() * image.getHeight()];
        final MapFont.CharacterSprite sprite = new MapFont.CharacterSprite(image.getWidth(), image.getHeight(), data);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                final Color color = new Color(image.getRGB(x, y), true);
                data[y * image.getWidth() + x] = color.getAlpha() >= 1;
            }
        }

        return sprite;
    }

}
