package dev.cerus.maps.api.graphics;

import dev.cerus.maps.api.MapAccess;

/**
 * Graphics implementation for map screens
 * <p>
 * Uses clientside map graphics under the hood
 * @deprecated Superseded by {@link BasicMapGraphics}
 */
@Deprecated(forRemoval = true)
public class MapScreenGraphics extends MapGraphics<MapAccess> {

    private final int width;
    private final int height;
    private final ClientsideMapGraphics[][] graphicsArray;

    public MapScreenGraphics(int w, int h) {
        this.width = w;
        this.height = h;
        this.graphicsArray = new ClientsideMapGraphics[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                this.graphicsArray[x][y] = new ClientsideMapGraphics(null);
            }
        }
    }

    @Override
    public void fillBuffer(byte color) {
        for (ClientsideMapGraphics[] array : this.graphicsArray) {
            for (ClientsideMapGraphics g : array) {
                g.fillBuffer(color);
            }
        }
    }

    @Override
    public byte setPixel(int x, int y, float alpha, byte color) {
        if (x < 0 || y < 0) {
            return color;
        }

        int arrX = x / 128;
        int arrY = y / 128;

        // Check bounds
        if (arrX >= this.graphicsArray.length || arrY >= this.graphicsArray[arrX].length || alpha == 0f) {
            return (byte) 0;
        }

        // Get right graphics and set pixel
        ClientsideMapGraphics graphics = this.graphicsArray[arrX][arrY];
        return graphics.setPixel(x % 128, y % 128, alpha, color);
    }

    @Override
    public byte getPixel(int x, int y) {
        if (x < 0 || y < 0) {
            return 0;
        }

        int arrX = x / 128;
        int arrY = y / 128;

        // Check bounds
        if (arrX >= this.graphicsArray.length || arrY >= this.graphicsArray[arrX].length) {
            return (byte) 0;
        }

        return this.graphicsArray[arrX][arrY].getPixel(x % 128, y % 128);
    }

    @Override
    public void markAreaDirty(int x, int y, int w, int h) {
    }

    @Override
    public void draw() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MapGraphics<MapAccess> copy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getWidth() {
        return this.width * 128;
    }

    @Override
    public int getHeight() {
        return this.height * 128;
    }

}
