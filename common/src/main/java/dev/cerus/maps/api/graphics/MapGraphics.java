package dev.cerus.maps.api.graphics;

import dev.cerus.maps.api.MapColor;
import dev.cerus.maps.api.graphics.filter.BoxBlurFilter;
import dev.cerus.maps.api.graphics.filter.Filter;
import dev.cerus.maps.api.graphics.filter.GrayscaleFilter;
import dev.cerus.maps.util.Vec2;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;

/**
 * The 2D rendering engine for maps. Implementations only need to take care of pixel setting and retrieving.
 * <p>
 * This class is huge and should maybe be split into multiple classes, but I haven't found an elegant solution yet.
 *
 * @param <C> The render target (e.g. ClientsideMap)
 * @param <P> The render params
 */
public abstract class MapGraphics<C, P> {

    /**
     * Creates a new standalone graphics instance
     *
     * @param width  Width of the graphics
     * @param height Height of the graphics
     *
     * @return New standalone graphics
     */
    public static MapGraphics<MapGraphics<?, ?>, ?> standalone(final int width, final int height) {
        return newGraphicsObject(width, height);
    }

    /**
     * Creates a new standalone graphics instance
     *
     * @param width  Width of the graphics
     * @param height Height of the graphics
     *
     * @return New standalone graphics
     */
    public static MapGraphics<MapGraphics<?, ?>, ?> newGraphicsObject(final int width, final int height) {
        return new StandaloneMapGraphics(width, height);
    }

    /**
     * Perform an 'over' alpha composition.
     * See <a href="https://cerus.dev/img/maps_alpha_composition.png">https://cerus.dev/img/maps_alpha_composition.png</a>
     * <p>
     * <pre>
     * # = Non transparent pixel with color from this instance
     * % = Non transparent pixel with color from the other instance
     *
     *                *  *
     *            * %%%%%%%% *
     *          * %% OTHER %%% *
     *          * %%%%%%%%%%%% *
     *     +----------+ %%%% *
     *     | # THIS # |  *
     *     | ######## |
     *     +----------+
     * </pre>
     *
     * @param graphics The graphics instance to composite
     * @param atX      The x coordinate where the composition should start
     * @param atY      The y coordinate where the composition should start
     */
    public void compositeOver(final MapGraphics<?, ?> graphics, final int atX, final int atY) {
        for (int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                if (x >= atX && y >= atY && x - atX < graphics.getWidth() && y - atY < graphics.getHeight()
                        && this.isTransparent(this.getPixel(x, y))
                        && !graphics.isTransparent(graphics.getPixel(x - atX, y - atY))) {
                    this.setPixel(x, y, 1F, graphics.getPixel(x - atX, y - atY));
                }
            }
        }
    }

    /**
     * Perform an 'in' alpha composition.
     * See <a href="https://cerus.dev/img/maps_alpha_composition.png">https://cerus.dev/img/maps_alpha_composition.png</a>
     * <p>
     * <pre>
     * # = Non transparent pixel with color from this instance
     * % = Non transparent pixel with color from the other instance
     *
     *                *  *
     *            *          *
     *          *    OTHER     *
     *          *              *
     *     +-------####      *
     *     |   THIS  ##  *
     *     |          |
     *     +----------+
     * </pre>
     *
     * @param graphics The graphics instance to composite
     * @param atX      The x coordinate where the composition should start
     * @param atY      The y coordinate where the composition should start
     */
    public void compositeIn(final MapGraphics<?, ?> graphics, final int atX, final int atY) {
        for (int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                if ((x - atX >= graphics.getWidth() || y - atY >= graphics.getHeight())
                        || (x < atX || y < atY)
                        || (this.isTransparent(this.getPixel(x, y))
                        || graphics.isTransparent(graphics.getPixel(x - atX, y - atY)))) {
                    this.setPixel(x, y, 1F, (byte) 0);
                }
            }
        }
    }

    /**
     * Perform an 'out' alpha composition.
     * See <a href="https://cerus.dev/img/maps_alpha_composition.png">https://cerus.dev/img/maps_alpha_composition.png</a>
     * <p>
     * <pre>
     * # = Non transparent pixel with color from this instance
     * % = Non transparent pixel with color from the other instance
     *
     *                *  *
     *            *          *
     *          *    OTHER     *
     *          *              *
     *     +------*          *
     *     | # THIS # *  *
     *     | ######## |
     *     +----------+
     * </pre>
     *
     * @param graphics The graphics instance to composite
     * @param atX      The x coordinate where the composition should start
     * @param atY      The y coordinate where the composition should start
     */
    public void compositeOut(final MapGraphics<?, ?> graphics, final int atX, final int atY) {
        for (int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                if (x >= atX && y >= atY && x - atX < graphics.getWidth() && y - atY < graphics.getHeight()
                        && !this.isTransparent(graphics.getPixel(x - atX, y - atY))) {
                    this.setPixel(x, y, 1F, (byte) 0);
                }
            }
        }
    }

    /**
     * Perform an 'atop' alpha composition.
     * See <a href="https://cerus.dev/img/maps_alpha_composition.png">https://cerus.dev/img/maps_alpha_composition.png</a>
     * <p>
     * <pre>
     * # = Non transparent pixel with color from this instance
     * % = Non transparent pixel with color from the other instance
     *
     *                *  *
     *            * %%%%%%%% *
     *          * %% OTHER %%% *
     *          * %%%%%%%%%%%% *
     *     +-------####%%%%% *
     *     |   THIS  ##% *
     *     |          |
     *     +----------+
     * </pre>
     *
     * @param graphics The graphics instance to composite
     * @param atX      The x coordinate where the composition should start
     * @param atY      The y coordinate where the composition should start
     */
    public void compositeAtop(final MapGraphics<?, ?> graphics, final int atX, final int atY) {
        for (int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                if (x >= atX && y >= atY && x - atX < graphics.getWidth() && y - atY < graphics.getHeight()) {
                    if (!this.isTransparent(this.getPixel(x, y))
                            && graphics.isTransparent(graphics.getPixel(x - atX, y - atY))) {
                        this.setPixel(x, y, (byte) 0);
                    } else if (this.isTransparent(this.getPixel(x, y))
                            && !graphics.isTransparent(graphics.getPixel(x - atX, y - atY))) {
                        this.setPixel(x, y, graphics.getPixel(x - atX, y - atY));
                    }
                } else if (!this.isTransparent(this.getPixel(x, y))) {
                    this.setPixel(x, y, (byte) 0);
                }
            }
        }
    }

    /**
     * Perform an 'xor' alpha composition.
     * See <a href="https://cerus.dev/img/maps_alpha_composition.png">https://cerus.dev/img/maps_alpha_composition.png</a>
     * <p>
     * <pre>
     * # = Non transparent pixel with color from this instance
     * % = Non transparent pixel with color from the other instance
     *
     *                *  *
     *            * %%%%%%%% *
     *          * %% OTHER %%% *
     *          * %%%%%%%%%%%% *
     *     +----------+ %%%% *
     *     | # THIS   |  *
     *     | ######## |
     *     +----------+
     * </pre>
     *
     * @param graphics The graphics instance to composite
     * @param atX      The x coordinate where the composition should start
     * @param atY      The y coordinate where the composition should start
     */
    public void compositeXor(final MapGraphics<?, ?> graphics, final int atX, final int atY) {
        for (int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                if (x >= atX && y >= atY && x - atX < graphics.getWidth() && y - atY < graphics.getHeight()) {
                    if (!graphics.isTransparent(graphics.getPixel(x - atX, y - atY))
                            && !this.isTransparent(this.getPixel(x, y))) {
                        this.setPixel(x, y, 1F, (byte) 0);
                    } else if (!graphics.isTransparent(graphics.getPixel(x - atX, y - atY))) {
                        this.setPixel(x, y, 1F, graphics.getPixel(x - atX, y - atY));
                    }
                }
            }
        }
    }

    public void fillWithBuffer(final MapGraphics<?, ?> graphics, final float alpha, final boolean ignoreTransparent) {
        int x = 0;
        int y = 0;
        while (y < this.getHeight()) {
            this.place(graphics, x, y, alpha, ignoreTransparent);
            x += graphics.getWidth();

            if (x >= this.getWidth()) {
                x = 0;
                y += graphics.getHeight();
            }
        }
    }

    /**
     * Copy the contents of the specified graphics instance onto the buffer of
     * this graphics instance at the specified position
     *
     * @param graphics The graphics instance to copy from
     * @param x        X coordinate
     * @param y        Y coordinate
     */
    public void place(final MapGraphics<?, ?> graphics, final int x, final int y) {
        this.place(graphics, x, y, 1f);
    }

    /**
     * Copy the contents of the specified graphics instance onto the buffer of
     * this graphics instance at the specified position
     *
     * @param graphics The graphics instance to copy from
     * @param x        X coordinate
     * @param y        Y coordinate
     * @param alpha    The alpha value of the contents
     */
    public void place(final MapGraphics<?, ?> graphics, final int x, final int y, final float alpha) {
        this.place(graphics, x, y, alpha, true);
    }

    /**
     * Copy the contents of the specified graphics instance onto the buffer of
     * this graphics instance at the specified position
     *
     * @param graphics          The graphics instance to copy from
     * @param x                 X coordinate
     * @param y                 Y coordinate
     * @param alpha             The alpha value of the contents
     * @param ignoreTransparent Should transparent pixels not be copied?
     */
    public void place(final MapGraphics<?, ?> graphics, final int x, final int y, final float alpha, final boolean ignoreTransparent) {
        if (x >= this.getWidth() || y >= this.getHeight() || x + graphics.getWidth() < 0 || y + graphics.getWidth() < 0) {
            return;
        }
        if (this.hasDirectAccessCapabilities()
                && graphics.hasDirectAccessCapabilities()
                && !ignoreTransparent) {
            for (int r = y < 0 ? -y : 0; r < (y + graphics.getHeight() >= this.getHeight()
                    ? this.getHeight() - y
                    : graphics.getHeight()); r++) {
                final int srcX = x < 0 ? -x : 0;
                final int srcW = graphics.getWidth();
                final int srcH = graphics.getHeight();
                final int len;
                if (x + graphics.getWidth() >= this.getWidth()) {
                    len = this.getWidth() - x;
                } else if (x < 0) {
                    len = graphics.getWidth() - (-x);
                } else {
                    len = graphics.getWidth();
                }

                System.arraycopy(
                        graphics.getDirectAccessData(),
                        this.index(srcX, r, srcW, srcH) /*r * graphics.getWidth()*/,
                        this.getDirectAccessData(),
                        this.index(Math.max(0, x), r + y, this.getWidth(), this.getHeight()) /*x + (r + y) * this.getWidth()*/,
                        len
                );
            }
        } else {
            for (int ox = 0; ox < graphics.getWidth(); ox++) {
                for (int oy = 0; oy < graphics.getHeight(); oy++) {
                    if (!ignoreTransparent || !this.isTransparent(graphics.getPixel(ox, oy))) {
                        this.setPixel(x + ox, y + oy, alpha, graphics.getPixel(ox, oy));
                    }
                }
            }
        }
    }

    /**
     * Perform box blur on a rectangular area
     *
     * @param x      X coordinate
     * @param y      Y coordinate
     * @param width  Area width
     * @param height Area height
     */
    public void grayscale(final int x, final int y, final int width, final int height) {
        this.applyFilterToArea(new GrayscaleFilter(), x, y, width, height);
    }

    /**
     * Perform box blur on a rectangular area
     *
     * @param x      X coordinate
     * @param y      Y coordinate
     * @param width  Area width
     * @param height Area height
     */
    public void boxBlur(final int x, final int y, final int width, final int height) {
        this.boxBlur(1, x, y, width, height);
    }

    /**
     * Perform box blur on a rectangular area
     *
     * @param transparencyHandling How transparent pixels should be handled
     * @param x                    X coordinate
     * @param y                    Y coordinate
     * @param width                Area width
     * @param height               Area height
     */
    public void boxBlur(final BoxBlurFilter.TransparencyHandling transparencyHandling, final int x, final int y, final int width, final int height) {
        this.boxBlur(1, transparencyHandling, x, y, width, height);
    }

    /**
     * Perform box blur on a rectangular area
     *
     * @param passes The intensity of the blur (2-5 for best effect)
     * @param x      X coordinate
     * @param y      Y coordinate
     * @param width  Area width
     * @param height Area height
     */
    public void boxBlur(final int passes, final int x, final int y, final int width, final int height) {
        this.boxBlur(passes, BoxBlurFilter.TransparencyHandling.IGNORE, x, y, width, height);
    }

    /**
     * Perform box blur on a rectangular area
     *
     * @param passes               The intensity of the blur (2-5 for best effect)
     * @param transparencyHandling How transparent pixels should be handled
     * @param x                    X coordinate
     * @param y                    Y coordinate
     * @param width                Area width
     * @param height               Area height
     */
    public void boxBlur(final int passes,
                        final BoxBlurFilter.TransparencyHandling transparencyHandling,
                        final int x,
                        final int y,
                        final int width,
                        final int height) {
        this.applyFilterToArea(
                new BoxBlurFilter(passes, transparencyHandling),
                x,
                y,
                width,
                height
        );
    }

    /**
     * Apply a filter to a rectangular area
     *
     * @param filter The filter to apply
     * @param x      X coordinate
     * @param y      Y coordinate
     * @param width  Area width
     * @param height Area height
     */
    public void applyFilterToArea(final Filter filter, final int x, final int y, final int width, final int height) {
        for (int unused = 0; unused < filter.passes(); unused++) {
            for (int ax = 0; ax < width; ax++) {
                for (int ay = 0; ay < height; ay++) {
                    this.setPixel(ax + x, ay + y, filter.apply(this, ax + x, ay + y, x, x + width, y, y + height));
                }
            }
        }
    }

    /**
     * Perform a flood fill operation at a specific coordinate
     * <p>
     * Will do nothing if the color at the starting coordinate is the same as the fill color.
     *
     * @param x     The starting x coordinate
     * @param y     The starting y coordinate
     * @param color The color to fill with
     * @param alpha The alpha of the filling color
     */
    public void fill(final int x, final int y, final byte color, final float alpha) {
        final Deque<Vec2> queue = new ArrayDeque<>();
        queue.add(new Vec2(x, y));
        final byte colorToReplace = this.getPixel(x, y);
        if (colorToReplace == color) {
            return;
        }

        while (!queue.isEmpty()) {
            final Vec2 n = queue.pop();
            if (n.x >= 0 && n.y >= 0 && n.x < this.getWidth() && n.y < this.getHeight()
                    && this.getPixel(n.x, n.y) == colorToReplace) {
                this.setPixel(n.x, n.y, alpha, color);
                queue.add(new Vec2(n.x - 1, n.y));
                queue.add(new Vec2(n.x + 1, n.y));
                queue.add(new Vec2(n.x, n.y - 1));
                queue.add(new Vec2(n.x, n.y + 1));
            }
        }
    }

    /**
     * Fills the whole buffer with a specific color
     *
     * @param color The color to fill the buffer with
     */
    public void fillComplete(final byte color) {
        if (this.hasDirectAccessCapabilities()) {
            Arrays.fill(this.getDirectAccessData(), color);
        } else {
            for (int x = 0; x < this.getWidth(); x++) {
                for (int y = 0; y < this.getHeight(); y++) {
                    this.setPixel(x, y, color);
                }
            }
        }
    }

    /**
     * Draws and fills a rectangle
     *
     * @param x     The x coordinate
     * @param y     The y coordinate
     * @param w     The width
     * @param h     The height
     * @param color The fill color
     * @param alpha The alpha of the rectangle
     */
    public void fillRect(final int x, final int y, final int w, final int h, final byte color, final float alpha) {
        if (alpha == 0f) {
            return;
        }
        if (this.hasDirectAccessCapabilities() && alpha == 1f) {
            for (int r = 0; r < h; r++) {
                Arrays.fill(this.getDirectAccessData(),
                        Math.max(0, this.index(x, r + y, this.getWidth(), this.getHeight())) /*x + (r + y) * this.getWidth()*/,
                        Math.min(this.getDirectAccessData().length - 1, this.index(Math.min(this.getWidth(), x + w), r + y, this.getWidth(), this.getHeight())) /*(x + w) + (r + y) * this.getWidth()*/,
                        color);
            }
        } else {
            for (int cx = x; cx < x + w; cx++) {
                for (int cy = y; cy < y + h; cy++) {
                    this.setPixel(cx, cy, alpha, color);
                }
            }
        }
    }

    /**
     * Outlines a rectangle
     *
     * @param x     The x coordinate
     * @param y     The y coordinate
     * @param w     The width
     * @param h     The height
     * @param color The outline color
     * @param alpha The alpha of the rectangle
     */
    public void drawRect(final int x, final int y, final int w, final int h, final byte color, final float alpha) {
        this.drawLineX(x, x + w, y, color, alpha);
        this.drawLineX(x, x + w, y + h, color, alpha);
        this.drawLineY(y, y + h, x, color, alpha);
        this.drawLineY(y, y + h, x + w, color, alpha);
    }

    /**
     * Draws a line
     *
     * @param x1    The starting x coordinate
     * @param y1    The starting y coordinate
     * @param x2    The finishing x coordinate
     * @param y2    The finishing y coordinate
     * @param color The outline color
     * @param alpha The alpha of the rectangle
     */
    public void drawLine(final int x1, final int y1, final int x2, final int y2, final byte color, final float alpha) {
        if (x1 == x2) {
            this.drawLineY(y1, y2, x1, color, alpha);
            return;
        }
        if (y1 == y2) {
            this.drawLineX(x1, x2, y1, color, alpha);
            return;
        }
        this.drawLine(new Vec2(x1, y1), new Vec2(x2, y2), color, alpha);
    }

    protected void drawLineX(final int x1, final int x2, final int y, final byte color, final float alpha) {
        if (this.hasDirectAccessCapabilities() && alpha == 1f) {
            Arrays.fill(this.getDirectAccessData(),
                    this.index(Math.min(x1, x2), y, this.getWidth(), this.getHeight()) /*Math.min(x1, x2) + y * this.getWidth()*/,
                    this.index(Math.max(x1, x2), y, this.getWidth(), this.getHeight()) /*Math.max(x1, x2) + y * this.getWidth()*/,
                    color);
        } else {
            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
                this.setPixel(x, y, alpha, color);
            }
        }
    }

    protected void drawLineY(final int y1, final int y2, final int x, final byte color, final float alpha) {
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
            this.setPixel(x, y, alpha, color);
        }
    }

    /**
     * Draws a line
     *
     * @param v1    The starting coordinate
     * @param v2    The finishing coordinate
     * @param color The outline color
     * @param alpha The alpha of the rectangle
     */
    public void drawLine(final Vec2 v1, final Vec2 v2, final byte color, final float alpha) {
        final Vec2[] arr = this.lerpVecArr(v1, v2);
        for (final Vec2 p : arr) {
            this.setPixel(p.x, p.y, alpha, color);
        }
    }

    /**
     * Perform linear interpolation calculations
     * <p>
     * All the line math was taken from https://www.redblobgames.com/grids/line-drawing.html
     *
     * @param start Start
     * @param end   End
     *
     * @return Array of points between start and end
     */
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

    // 2D distance
    private int dist(final Vec2 v1, final Vec2 v2) {
        return (int) (Math.pow(v1.x - v2.x, 2) + Math.pow(v1.y - v2.y, 2));
    }

    // Pretty much useless
    private int round(final float f) {
        final int i = (int) f;
        final float rem = f - i;
        return rem < 0.5f ? i : i + 1;
    }

    /**
     * Draws an image onto the graphics buffer
     *
     * @param img The image to draw
     * @param x   The x coordinate where the image should be drawn
     * @param y   The y coordinate where the image should be drawn
     */
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

    /**
     * Draws text
     * <p>
     * Stolen from Bukkit, sorry
     *
     * @param x          X coordinate
     * @param y          Y coordinate
     * @param text       The text
     * @param startColor The color
     * @param size       The size multiplier (1 = normal)
     */
    public void drawText(final int x, final int y, final String text, final byte startColor, final int size) {
        this.drawText(x, y, text, MinecraftFont.Font, startColor, size);
    }

    public void drawText(int x, int y, final String text, final MapFont font, final byte startColor, final int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }

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

    public void drawEllipse(final int atX, final int atY, final int widthRad, final int heightRad, final byte color) {
        this.drawEllipse(atX, atY, widthRad, heightRad, color, 1f);
    }

    /**
     * Outlines an ellipse using the midpoint ellipse algorithm
     * <p>
     * <a href="https://www.geeksforgeeks.org/midpoint-ellipse-drawing-algorithm/">https://www.geeksforgeeks.org/midpoint-ellipse-drawing-algorithm/</a>
     *
     * @param atX       Center x of the ellipse
     * @param atY       Center y of the ellipse
     * @param widthRad  Horizontal radius
     * @param heightRad Vertical radius
     * @param color     Color of the ellipse
     * @param alpha     Alpha of the ellipse
     */
    public void drawEllipse(final int atX,
                            final int atY,
                            final int widthRad,
                            final int heightRad,
                            final byte color,
                            final float alpha) {
        float dx, dy, d1, d2, x, y;
        x = 0;
        y = heightRad;

        d1 = (heightRad * heightRad) - (widthRad * widthRad * heightRad) +
                (0.25f * widthRad * widthRad);
        dx = 2 * heightRad * heightRad * x;
        dy = 2 * widthRad * widthRad * y;

        while (dx < dy) {
            this.drawEllipseInternal(atX, atY, color, x, y, alpha);

            x++;
            if (d1 < 0) {
                dx = dx + (2 * heightRad * heightRad);
                d1 = d1 + dx + (heightRad * heightRad);
            } else {
                y--;
                dx = dx + (2 * heightRad * heightRad);
                dy = dy - (2 * widthRad * widthRad);
                d1 = d1 + dx - dy + (heightRad * heightRad);
            }
        }

        d2 = ((heightRad * heightRad) * ((x + 0.5f) * (x + 0.5f)))
                + ((widthRad * widthRad) * ((y - 1) * (y - 1)))
                - (widthRad * widthRad * heightRad * heightRad);

        while (y >= 0) {
            this.drawEllipseInternal(atX, atY, color, x, y, alpha);

            y--;
            if (d2 > 0) {
                dy = dy - (2 * widthRad * widthRad);
                d2 = d2 + (widthRad * widthRad) - dy;
            } else {
                x++;
                dx = dx + (2 * heightRad * heightRad);
                dy = dy - (2 * widthRad * widthRad);
                d2 = d2 + dx - dy + (widthRad * widthRad);
            }
        }
    }

    private void drawEllipseInternal(final int atX, final int atY, final byte color, final float x, final float y, final float alpha) {
        this.setPixel((int) (x + atX), (int) (y + atY), alpha, color);
        this.setPixel((int) (-x + atX), (int) (y + atY), alpha, color);
        this.setPixel((int) (x + atX), (int) (-y + atY), alpha, color);
        this.setPixel((int) (-x + atX), (int) (-y + atY), alpha, color);
    }

    /**
     * Replace a color with another color across the whole buffer
     *
     * @param colorToReplace The color to replace
     * @param color          The color to replace the other color with
     */
    public void replace(final byte colorToReplace, final byte color) {
        for (int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                if (this.getPixel(x, y) == colorToReplace) {
                    this.setPixel(x, y, 1f, color);
                }
            }
        }
    }

    /**
     * Set a pixel
     *
     * @param x     The x coordinate
     * @param y     The y coordinate
     * @param color The pixel color
     *
     * @return The old pixel color. Might return the specified color if the method does not end up setting the pixel.
     */
    public byte setPixel(final int x, final int y, final byte color) {
        return this.setPixel(x, y, 1f, color);
    }

    /**
     * Set a pixel
     *
     * @param x     The x coordinate
     * @param y     The y coordinate
     * @param alpha The pixel alpha
     * @param color The pixel color
     *
     * @return The old pixel color. Might return the specified color if the method does not end up setting the pixel.
     */
    public abstract byte setPixel(int x, int y, float alpha, byte color);

    /**
     * Get a pixel
     *
     * @param x The x coordinate
     * @param y The y coordinate
     *
     * @return The pixels color
     */
    public abstract byte getPixel(final int x, final int y);

    /**
     * Composite two colors together
     * <p>
     * See <a href="https://en.wikipedia.org/wiki/Alpha_compositing">https://en.wikipedia.org/wiki/Alpha_compositing</a>
     *
     * @param source The new color
     * @param dest   The old color (e.g. background)
     * @param alpha  The alpha
     *
     * @return The composited color
     */
    protected byte calculateComposite(final byte source, final byte dest, final float alpha) {
        if (alpha <= 0f) {
            return dest;
        } else if (alpha >= 1f) {
            return source;
        } else {
            if (this.isTransparent(source)) {
                return dest;
            }
            if (this.isTransparent(dest)) {
                return source;
            }

            return CompositeColorCache.getCompositeOrCompute(source, dest, alpha, () -> {
                final Color newColor = MapColor.mapColorToRgb(source);
                final Color oldColor = MapColor.mapColorToRgb(dest);
                final int[] compositedColor = new int[] {
                        this.composite(newColor.getRed(), oldColor.getRed(), alpha),
                        this.composite(newColor.getGreen(), oldColor.getGreen(), alpha),
                        this.composite(newColor.getBlue(), oldColor.getBlue(), alpha)
                };
                return ColorCache.rgbToMap(compositedColor[0], compositedColor[1], compositedColor[2]);
            });
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

    /**
     * Returns true when the specified color is transparent.
     * <p>
     * Technically there are 4 transparent colors (0 - 3), but we only return true on 0. This is
     * because we use 0 to mark the absence of a color. This allows us to use 1 - 3 as transparent
     * colors in other processing methods (blending for example).
     * <p>
     * Implementations of this class are allowed to modify this to suit their needs, but that could
     * affect the functionality of other default features.
     *
     * @param color The color to check
     *
     * @return True if transparent
     */
    public boolean isTransparent(final byte color) {
        return color == 0;
    }

    protected float normalizeAlpha(final float a) {
        return Math.max(0f, Math.min(1f, a));
    }

    /**
     * Basically copies this buffer onto the buffer of the specified render target
     *
     * @param renderTarget The render target
     */
    public void renderOnto(final C renderTarget) {
        this.renderOnto(renderTarget, null);
    }

    /**
     * Basically copies this buffer onto the buffer of the specified render target
     *
     * @param renderTarget The render target
     * @param params       The render parameters
     */
    public abstract void renderOnto(C renderTarget, P params);

    /**
     * Make a copy of this buffer
     *
     * @return A copy of this buffer
     */
    public abstract MapGraphics<C, P> copy();

    /**
     * Get the width in pixels of this buffer
     *
     * @return The width
     */
    public abstract int getWidth();

    /**
     * Get the height in pixels of this buffer
     *
     * @return The height
     */
    public abstract int getHeight();

    public boolean hasDirectAccessCapabilities() {
        return this.getDirectAccessData() != null;
    }

    public byte[] getDirectAccessData() {
        return null;
    }

    public int index(final int x, final int y) {
        return this.index(x, y, this.getWidth(), this.getHeight());
    }

    public int index(final int x, final int y, final int w, final int h) {
        return x + y * Math.max(w, h);
    }

}
