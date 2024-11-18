package dev.cerus.maps.api.graphics;

import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.util.Vec2;

/**
 * Graphics implementation for the clientside map
 */
public class ClientsideMapGraphics extends MapGraphics<ClientsideMap> {

    // Will always be the same unless Mojang changes something (pls don't do that)
    private static final int WIDTH = 128;

    private final byte[] data = new byte[WIDTH * WIDTH];
    private final ClientsideMap parent;

    public ClientsideMapGraphics(ClientsideMap parent) {
        this.parent = parent;
    }

    @Override
    public byte setPixel(int x, int y, float alpha, byte color) {
        float normAlpha = this.normalizeAlpha(alpha);
        if (x < 0 || x >= WIDTH || y < 0 || y >= WIDTH || normAlpha == 0f) {
            return color;
        }

        // We do a little compositing
        byte actualColor = normAlpha == 1f ? color : this.calculateComposite(color, this.getPixel(x, y), normAlpha);
        if (this.getPixel(x, y) == actualColor) {
            return color;
        }
        return this.setPixelInternal(x, y, actualColor);
    }

    // Set pixel directly, skip any checks
    private byte setPixelInternal(int x, int y, byte color) {
        byte bef = this.getPixel(x, y);
        this.data[this.index(x, y, WIDTH, WIDTH)] = color;
        return bef;
    }

    @Override
    public byte getPixel(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= WIDTH) {
            return 0;
        }
        return this.getPixelDirect(x, y);
    }

    private byte getPixelDirect(int x, int y) {
        //return this.data[x * this.height + y];
        return this.data[this.index(x, y, WIDTH, WIDTH)];
    }

    @Override
    public void markAreaDirty(int x, int y, int w, int h) {
        parent.markDirty(x, y);
        parent.markDirty(x + w, y);
        parent.markDirty(x, y + h);
        parent.markDirty(x + w, y + h);
    }

    /**
     * Copies this buffer onto the buffer of a clientside map. Will also
     * calculate the bounds of the changed contents for optimal packet compression.
     * <p>
     * See {@link MapGraphics#draw()}
     */
    @Override
    public void draw() {
        bufferLock().lock();
        System.arraycopy(data, 0, parent.getData(), 0, data.length);
        bufferLock().unlock();
    }

    @Override
    public MapGraphics<ClientsideMap> copy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized byte[] getDirectAccessData() {
        //return this.data;
        return null;
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
