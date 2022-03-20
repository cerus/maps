package dev.cerus.maps.api.graphics;

import dev.cerus.maps.util.Vec2;

/**
 * Graphics implementation that's completely standalone and versatile. Could be used for sprites for example.
 */
public class StandaloneMapGraphics extends MapGraphics<MapGraphics<?, ?>, Vec2> {

    private final int width;
    private final int height;
    private final byte[] data;

    public StandaloneMapGraphics(final int width, final int height) {
        this.width = width;
        this.height = height;
        this.data = new byte[width * height];
    }

    @Override
    public byte setPixel(final int x, final int y, final float alpha, final byte color) {
        // Lots of bounds and alpha checking
        final float normAlpha = this.normalizeAlpha(alpha);
        if (x < 0 || x >= this.width || y < 0 || y >= this.height || normAlpha == 0f) {
            return color;
        }

        // More compositing
        final byte actualColor = normAlpha == 1f ? color : this.calculateComposite(color, this.getPixel(x, y), normAlpha);
        if (this.getPixel(x, y) == actualColor) {
            return color;
        }
        return this.setPixelInternal(x, y, actualColor);
    }

    private byte setPixelInternal(final int x, final int y, final byte color) {
        final byte bef = this.getPixel(x, y);
        this.data[x * this.height + y] = color;
        return bef;
    }

    @Override
    public byte getPixel(final int x, final int y) {
        return this.data[x * this.height + y];
    }

    @Override
    public void renderOnto(final MapGraphics<?, ?> renderTarget, final Vec2 params) {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                renderTarget.setPixel(params.x + x, params.y + y, this.getPixel(x, y));
            }
        }
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

}
