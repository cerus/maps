package dev.cerus.maps.api.graphics;

import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.util.Vec2;

public class ClientsideMapGraphics extends MapGraphics<ClientsideMap, Void> {

    private static final int WIDTH = 128;

    private final byte[] data = new byte[WIDTH * WIDTH];

    @Override
    public void fill(final byte color) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < WIDTH; y++) {
                this.setPixel(x, y, color);
            }
        }
    }

    @Override
    public byte setPixel(final int x, final int y, final byte color) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= WIDTH) {
            return color;
        }
        if (this.getPixel(x, y) == color) {
            return color;
        }
        return this.setPixelInternal(x, y, color);
    }

    private byte setPixelInternal(final int x, final int y, final byte color) {
        final byte bef = this.getPixel(x, y);
        this.data[x + y * WIDTH] = color;
        return bef;
    }

    private byte getPixel(final int x, final int y) {
        return this.data[x + y * WIDTH];
    }

    @Override
    public void draw(final ClientsideMap clientsideMap, final Void unused) {
        final Vec2 min = new Vec2(Integer.MAX_VALUE, Integer.MAX_VALUE);
        final Vec2 max = new Vec2(Integer.MIN_VALUE, Integer.MIN_VALUE);

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < WIDTH; y++) {
                if (this.getPixel(x, y) == clientsideMap.getData()[x + y * WIDTH]) {
                    continue;
                }

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

                clientsideMap.getData()[x + y * WIDTH] = this.getPixel(x, y);
            }
        }

        clientsideMap.setX(min.x == Integer.MAX_VALUE ? 0 : min.x);
        clientsideMap.setY(min.y == Integer.MAX_VALUE ? 0 : min.y);
        clientsideMap.setWidth(max.x == Integer.MIN_VALUE ? 0 : max.x - min.x);
        clientsideMap.setHeight(max.y == Integer.MIN_VALUE ? 0 : max.y - min.y);
    }

}
