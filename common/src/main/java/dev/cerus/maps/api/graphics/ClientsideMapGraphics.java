package dev.cerus.maps.api.graphics;

import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.util.Vec2;

/**
 * Graphics implementation for the clientside map
 */
public class ClientsideMapGraphics extends MapGraphics<ClientsideMap, Void> {

    // Will always be the same unless Mojang changes something (pls don't do that)
    private static final int WIDTH = 128;

    private final byte[] data = new byte[WIDTH * WIDTH];

    @Override
    public byte setPixel(final int x, final int y, final float alpha, final byte color) {
        final float normAlpha = this.normalizeAlpha(alpha);
        if (x < 0 || x >= WIDTH || y < 0 || y >= WIDTH || normAlpha == 0f) {
            return color;
        }

        // We do a little compositing
        final byte actualColor = normAlpha == 1f ? color : this.calculateComposite(color, this.getPixel(x, y), normAlpha);
        if (this.getPixel(x, y) == actualColor) {
            return color;
        }
        return this.setPixelInternal(x, y, actualColor);
    }

    // Set pixel directly, skip any checks
    private byte setPixelInternal(final int x, final int y, final byte color) {
        final byte bef = this.getPixel(x, y);
        this.data[x + y * WIDTH] = color;
        return bef;
    }

    @Override
    public byte getPixel(final int x, final int y) {
        return this.data[x + y * WIDTH];
    }

    /**
     * Copies this buffer onto the buffer of a clientside map. Will also
     * calculate the bounds of the changed contents for optimal packet compression.
     * <p>
     * See {@link MapGraphics#renderOnto(Object, Object)}
     */
    @Override
    public void renderOnto(final ClientsideMap renderTarget, final Void unused) {
        final Vec2 min = new Vec2(Integer.MAX_VALUE, Integer.MAX_VALUE);
        final Vec2 max = new Vec2(Integer.MIN_VALUE, Integer.MIN_VALUE);

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < WIDTH; y++) {
                // No need to set the pixel if it's already the same color
                if (this.getPixel(x, y) == renderTarget.getData()[x + y * WIDTH]) {
                    continue;
                }

                // Check and set bounds accordingly
                if (x < min.x) {
                    min.x = x;
                }
                if (y < min.y) {
                    min.y = y;
                }
                if (x + 1 > max.x) {
                    max.x = x + 1;
                }
                if (y + 1 > max.y) {
                    max.y = y + 1;
                }

                renderTarget.getData()[x + y * WIDTH] = this.getPixel(x, y);
            }
        }

        // Set the calculated bounds
        renderTarget.setX(min.x == Integer.MAX_VALUE ? 0 : min.x);
        renderTarget.setY(min.y == Integer.MAX_VALUE ? 0 : min.y);
        renderTarget.setWidth(max.x == Integer.MIN_VALUE ? 0 : max.x - min.x);
        renderTarget.setHeight(max.y == Integer.MIN_VALUE ? 0 : max.y - min.y);
    }

    @Override
    public MapGraphics<ClientsideMap, Void> copy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return WIDTH;
    }

}
