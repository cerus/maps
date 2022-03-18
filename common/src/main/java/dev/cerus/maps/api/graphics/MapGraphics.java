package dev.cerus.maps.api.graphics;

import dev.cerus.maps.util.Vec2;
import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;

public abstract class MapGraphics<T, P> {

    public abstract void fill(byte color);

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

    public abstract byte setPixel(int x, int y, byte color);

    public abstract void draw(T t, P params);

}
