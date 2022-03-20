package dev.cerus.maps.api.graphics;

import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.api.MapScreen;

/**
 * Graphics implementation for map screens
 * <p>
 * Uses clientside map graphics under the hood
 */
public class MapScreenGraphics extends MapGraphics<MapScreen, ClientsideMap[][]> {

    private final int width;
    private final int height;
    private final ClientsideMapGraphics[][] graphicsArray;

    public MapScreenGraphics(final int w, final int h) {
        this.width = w;
        this.height = h;
        this.graphicsArray = new ClientsideMapGraphics[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                this.graphicsArray[x][y] = new ClientsideMapGraphics();
            }
        }
    }

    @Override
    public byte setPixel(final int x, final int y, final float alpha, final byte color) {
        if (x < 0 || y < 0) {
            return color;
        }

        final int arrX = x / 128;
        final int arrY = y / 128;

        // Check bounds
        if (arrX >= this.graphicsArray.length || arrY >= this.graphicsArray[arrX].length || alpha == 0f) {
            return (byte) 0;
        }

        // Get right graphics and set pixel
        final ClientsideMapGraphics graphics = this.graphicsArray[arrX][arrY];
        return graphics.setPixel(x % 128, y % 128, alpha, color);
    }

    @Override
    public byte getPixel(final int x, final int y) {
        if (x < 0 || y < 0) {
            return 0;
        }

        final int arrX = x / 128;
        final int arrY = y / 128;

        // Check bounds
        if (arrX >= this.graphicsArray.length || arrY >= this.graphicsArray[arrX].length) {
            return (byte) 0;
        }

        return this.graphicsArray[arrX][arrY].getPixel(x % 128, y % 128);
    }

    @Override
    public void renderOnto(final MapScreen renderTarget, final ClientsideMap[][] array) {
        for (int x = 0; x < renderTarget.getWidth(); x++) {
            for (int y = 0; y < renderTarget.getHeight(); y++) {
                array[x][y].draw(this.graphicsArray[x][y]);
            }
        }
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
