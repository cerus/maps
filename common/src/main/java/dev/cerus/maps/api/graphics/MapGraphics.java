package dev.cerus.maps.api.graphics;

import dev.cerus.maps.api.MapColor;
import dev.cerus.maps.api.graphics.filter.BoxBlurFilter;
import dev.cerus.maps.api.graphics.filter.Filter;
import dev.cerus.maps.util.Vec2;
import java.awt.Color;
import java.awt.image.BufferedImage;
import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;

public abstract class MapGraphics<C, P> {

    public void place(final MapGraphics<?, ?> graphics, final int x, final int y) {
        this.place(graphics, x, y, 1f);
    }

    public void place(final MapGraphics<?, ?> graphics, final int x, final int y, final float alpha) {
        this.place(graphics, x, y, alpha, true);
    }

    public void place(final MapGraphics<?, ?> graphics, final int x, final int y, final float alpha, final boolean ignoreTransparent) {
        for (int ox = 0; ox < graphics.getWidth(); ox++) {
            for (int oy = 0; oy < graphics.getHeight(); oy++) {
                if (ignoreTransparent && graphics.getPixel(ox, oy) >= 0 && graphics.getPixel(ox, oy) <= 3) {
                    continue;
                }
                this.setPixel(x + ox, y + oy, alpha, graphics.getPixel(ox, oy));
            }
        }
    }

    public void boxBlur(final int x, final int y, final int width, final int height) {
        this.boxBlur(1, x, y, width, height);
    }

    public void boxBlur(final BoxBlurFilter.TransparencyHandling transparencyHandling, final int x, final int y, final int width, final int height) {
        this.boxBlur(1, transparencyHandling, x, y, width, height);
    }

    public void boxBlur(final int passes, final int x, final int y, final int width, final int height) {
        this.boxBlur(passes, BoxBlurFilter.TransparencyHandling.IGNORE, x, y, width, height);
    }

    public void boxBlur(final int passes, final BoxBlurFilter.TransparencyHandling transparencyHandling, final int x, final int y, final int width, final int height) {
        this.applyFilterToArea(
                new BoxBlurFilter(passes, transparencyHandling),
                x,
                y,
                width,
                height
        );
    }

    public void applyFilterToArea(final Filter filter, final int x, final int y, final int width, final int height) {
        for (int unused = 0; unused < filter.passes(); unused++) {
            for (int ax = 0; ax < width; ax++) {
                for (int ay = 0; ay < height; ay++) {
                    this.setPixel(ax + x, ay + y, filter.calculate(this, ax + x, ay + y, x, x + width, y, y + height));
                }
            }
        }
    }

    public void fill(final byte color) {
        for (int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                this.setPixel(x, y, color);
            }
        }
    }

    public void fillRect(final int x, final int y, final int w, final int h, final byte color) {
        for (int cx = x; cx < x + w; cx++) {
            for (int cy = y; cy < y + h; cy++) {
                this.setPixel(cx, cy, color);
            }
        }
    }

    public void drawRect(final int x, final int y, final int w, final int h, final byte color) {
        this.drawLineX(x, x + w, y, color);
        this.drawLineX(x, x + w, y + h, color);
        this.drawLineY(y, y + h, x, color);
        this.drawLineY(y, y + h + 1, x + w, color);
    }

    public void drawLine(final int x1, final int y1, final int x2, final int y2, final byte color) {
        if (x1 == x2) {
            this.drawLineY(y1, y2, x1, color);
            return;
        }
        if (y1 == y2) {
            this.drawLineX(x1, x2, y1, color);
            return;
        }
        this.drawLine(new Vec2(x1, y1), new Vec2(x2, y2), color);
    }

    private void drawLineX(final int x1, final int x2, final int y, final byte color) {
        for (int x = Math.min(x1, x2); x < Math.max(x1, x2); x++) {
            this.setPixel(x, y, color);
        }
    }

    private void drawLineY(final int y1, final int y2, final int x, final byte color) {
        for (int y = Math.min(y1, y2); y < Math.max(y1, y2); y++) {
            this.setPixel(x, y, color);
        }
    }

    public void drawLine(final Vec2 v1, final Vec2 v2, final byte color) {
        final Vec2[] arr = this.lerpVecArr(v1, v2);
        for (final Vec2 p : arr) {
            this.setPixel(p.x, p.y, color);
        }
    }

    // All the line math was taken from https://www.redblobgames.com/grids/line-drawing.html
    private Vec2[] lerpVecArr(final Vec2 start, final Vec2 end) {
        final int len = this.dist(start, end);
        final Vec2[] arr = new Vec2[len];

        for (int i = 0; i < len; i++) {
            arr[i] = this.lerpVec(start, end, (float) i / (float) len);
        }

        return arr;
    }

    private Vec2 lerpVec(final Vec2 start, final Vec2 end, final float t) {
        return new Vec2(this.round(this.lerp(start.x, end.x, t)), this.round(this.lerp(start.y, end.y, t)));
    }

    private float lerp(final int start, final int end, final float t) {
        return start + t * (end - start);
    }

    private int dist(final Vec2 v1, final Vec2 v2) {
        return (int) (Math.pow(v1.x - v2.x, 2) + Math.pow(v1.y - v2.y, 2));
    }

    private int round(final float f) {
        final int i = (int) f;
        final float rem = f - i;
        return rem < 0.5f ? i : i + 1;
    }

    public void drawImage(final BufferedImage img, final int x, final int y) {
        for (int ix = 0; ix < img.getWidth(); ix++) {
            for (int iy = 0; iy < img.getHeight(); iy++) {
                final Color color = new Color(img.getRGB(ix, iy), img.getColorModel().hasAlpha());
                final float alpha = img.getColorModel().hasAlpha() ? color.getAlpha() / 255f : 1f;
                final byte mapColor = ColorCache.rgbToMap(color.getRed(), color.getGreen(), color.getBlue());
                this.setPixel(x + ix, y + iy, alpha, mapColor);
            }
        }
    }

    public void drawText(int x, int y, final String text, final byte startColor, final int size) {
        final MapFont font = MinecraftFont.Font;

        final int xStart = x;
        byte color = startColor;
        if (!font.isValid(text)) {
            throw new IllegalArgumentException("text contains invalid characters");
        } else {
            int currentIndex = 0;

            while (true) {
                if (currentIndex >= text.length()) {
                    return;
                }

                final char ch = text.charAt(currentIndex);
                if (ch == '\n') {
                    // Increment z if the char is a line separator
                    x = xStart;
                    y += font.getHeight() + 1;
                } else if (ch == '\u00A7' /*-> §*/) {
                    // Get distance from current char to end char (';')
                    final int end = text.indexOf(';', currentIndex);
                    if (end < 0) {
                        break;
                    }

                    // Parse color
                    try {
                        color = Byte.parseByte(text.substring(currentIndex + 1, end));
                        currentIndex = end;
                    } catch (final NumberFormatException var12) {
                        break;
                    }
                } else {
                    // Draw text if the character is not a special character
                    final MapFont.CharacterSprite sprite = font.getChar(text.charAt(currentIndex));

                    for (int row = 0; row < font.getHeight(); ++row) {
                        for (int col = 0; col < sprite.getWidth(); ++col) {
                            if (sprite.get(row, col)) {
                                for (int eX = 0; eX < size; eX++) {
                                    for (int eY = 0; eY < size; eY++) {
                                        this.setPixel(x + (size * col) + (eX), y + (size * row) + (eY), color);
                                    }
                                }
                            }
                        }
                    }

                    // Increment x
                    x += (sprite.getWidth() + 1) * size;
                }

                ++currentIndex;
            }

            throw new IllegalArgumentException("Text contains unterminated color string");
        }
    }

    public byte setPixel(final int x, final int y, final byte color) {
        return this.setPixel(x, y, 1f, color);
    }

    public abstract byte setPixel(int x, int y, float alpha, byte color);

    public abstract byte getPixel(final int x, final int y);

    /**
     * Composite two colors together
     *
     * @param source The new color
     * @param dest   The old color (e.g. background)
     * @param alpha  The alpha
     *
     * @return The composited color
     */
    protected byte calculateComposite(final byte source, final byte dest, final float alpha) {
        if (alpha == 0f) {
            return dest;
        } else if (alpha == 1f) {
            return source;
        } else {
            if (source >= 0 && source <= 3) {
                return dest;
            }
            if (dest >= 0 && dest <= 3) {
                return source;
            }

            final Color newColor = MapColor.mapColorToRgb(source);
            final Color oldColor = MapColor.mapColorToRgb(dest);
            final int[] compositedColor = new int[] {
                    this.composite(newColor.getRed(), oldColor.getRed(), alpha),
                    this.composite(newColor.getGreen(), oldColor.getGreen(), alpha),
                    this.composite(newColor.getBlue(), oldColor.getBlue(), alpha)
            };
            return ColorCache.rgbToMap(compositedColor[0], compositedColor[1], compositedColor[2]);
        }
    }

    /**
     * Calculate the new component value for two color components with a specific alpha value.
     * A color component is either red, green or blue.
     * <p>
     * Example:
     * <pre>
     *     Color someColor = new Color(0, 0, 0);
     *     Color background = new Color(255, 255, 255);
     *     float alpha = 0.5f;
     *     Color newColor = new Color(
     *         composite(someColor.getRed(), background.getRed(), alpha),
     *         composite(someColor.getGreen(), background.getGreen(), alpha),
     *         composite(someColor.getBlue(), background.getBlue(), alpha)
     *     );
     *     // newColor ==> java.awt.Color[r=127,g=127,b=127]
     * </pre>
     *
     * @param comp1 First component
     * @param comp2 Second component
     * @param a     Alpha
     *
     * @return New component
     */
    protected int composite(final int comp1, final int comp2, final float a) {
        final float c1 = comp1 / 255f;
        final float c2 = comp2 / 255f;
        return (int) ((c1 * a + c2 * (1f - a)) * 255f);
    }

    public abstract void renderOnto(C c, P params);

    public abstract int getWidth();

    public abstract int getHeight();

}
