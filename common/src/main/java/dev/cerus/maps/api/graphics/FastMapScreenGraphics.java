package dev.cerus.maps.api.graphics;

import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.util.Vec2;

/**
 * Graphics implementation for map screens
 * <p>
 * Uses clientside map graphics under the hood
 */
public class FastMapScreenGraphics extends MapGraphics<MapScreen, ClientsideMap[][]> {

    private final byte[] data;
    private final int width;
    private final int height;

    public FastMapScreenGraphics(final int w, final int h) {
        this.width = w * 128;
        this.height = h * 128;
        this.data = new byte[Math.max(this.width, this.height) * Math.max(this.width, this.height)];
    }

    @Override
    public byte setPixel(final int x, final int y, final float alpha, final byte color) {
        final float normAlpha = this.normalizeAlpha(alpha);
        if (x < 0 || x >= this.width || y < 0 || y >= this.height || normAlpha == 0f) {
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
        this.data[this.index(x, y, this.width, this.height)] = color;
        return bef;
    }

    @Override
    public byte getPixel(final int x, final int y) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            return 0;
        }
        return this.getPixelDirect(x, y);
    }

    private byte getPixelDirect(final int x, final int y) {
        //return this.data[x * this.height + y];
        return this.data[this.index(x, y, this.width, this.height)];
    }

    @Override
    public void renderOnto(final MapScreen caller, final ClientsideMap[][] array) {
        for (int col = 0; col < array.length; col++) {
            for (int row = 0; row < array[col].length; row++) {
                final ClientsideMap renderTarget = array[col][row];
                final Vec2 min = new Vec2(Integer.MAX_VALUE, Integer.MAX_VALUE);
                final Vec2 max = new Vec2(Integer.MIN_VALUE, Integer.MIN_VALUE);

                for (int x = 0; x < 128; x++) {
                    final int bigX = col * 128 + x;
                    for (int y = 0; y < 128; y++) {
                        final int bigY = row * 128 + y;

                        // No need to set the pixel if it's already the same color
                        if (this.getPixel(bigX, bigY) == renderTarget.getData()[x + y * 128]) {
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

                        renderTarget.getData()[x + y * 128] = this.getPixel(bigX, bigY);
                    }
                }

                // Set the calculated bounds
                renderTarget.setX(min.x == Integer.MAX_VALUE ? 0 : min.x);
                renderTarget.setY(min.y == Integer.MAX_VALUE ? 0 : min.y);
                renderTarget.setWidth(max.x == Integer.MIN_VALUE ? 0 : max.x - min.x);
                renderTarget.setHeight(max.y == Integer.MIN_VALUE ? 0 : max.y - min.y);
            }
        }
    }

    @Override
    public MapGraphics<MapScreen, ClientsideMap[][]> copy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getDirectAccessData() {
        return this.data;
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
