package dev.cerus.maps.api.graphics;

import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.api.MapScreen;

public class MapScreenGraphics extends MapGraphics<MapScreen, ClientsideMap[][]> {

    private final ClientsideMapGraphics[][] graphicsArray;

    public MapScreenGraphics(final int w, final int h) {
        this.graphicsArray = new ClientsideMapGraphics[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                this.graphicsArray[x][y] = new ClientsideMapGraphics();
            }
        }
    }

    @Override
    public void fill(final byte color) {
        for (final ClientsideMapGraphics[] arr : this.graphicsArray) {
            for (final ClientsideMapGraphics graphics : arr) {
                graphics.fill(color);
            }
        }
    }

    @Override
    public byte setPixel(final int x, final int y, final byte color) {
        final int arrX = x / 128;
        final int arrY = y / 128;

        if (arrX >= this.graphicsArray.length || arrY >= this.graphicsArray[arrX].length) {
            return (byte) -1;
        }

        final ClientsideMapGraphics graphics = this.graphicsArray[arrX][arrY];
        return graphics.setPixel(x % 128, y % 128, color);
    }

    @Override
    public void draw(final MapScreen screen, final ClientsideMap[][] array) {
        for (int x = 0; x < screen.getWidth(); x++) {
            for (int y = 0; y < screen.getHeight(); y++) {
                array[x][y].draw(this.graphicsArray[x][y]);
            }
        }
    }

}
