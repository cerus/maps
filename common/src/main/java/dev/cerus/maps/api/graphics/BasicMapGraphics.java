package dev.cerus.maps.api.graphics;

import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.api.MapAccess;
import dev.cerus.maps.util.Vec2;

/**
 * Graphics implementation for map screens
 * <p>
 * Uses clientside map graphics under the hood
 */
public class BasicMapGraphics extends MapGraphics<MapAccess> {

    private final MapAccess parent;
    private final byte[] data;
    private final int width;
    private final int height;

    public BasicMapGraphics(MapAccess parent) {
        this.parent = parent;
        this.width = parent.getWidth() * 128;
        this.height = parent.getHeight() * 128;
        this.data = new byte[width * height];
    }

    @Override
    public byte setPixel(int x, int y, float alpha, byte color) {
        float normAlpha = this.normalizeAlpha(alpha);
        if (x < 0 || x >= this.width || y < 0 || y >= this.height || normAlpha == 0f) {
            return color;
        }

        // We do a little compositing
        byte actualColor = normAlpha == 1f ? color : this.calculateComposite(color, this.getPixel(x, y), normAlpha);
        if (this.getPixelDirect(x, y) == actualColor) {
            return color;
        }
        return this.setPixelInternal(x, y, actualColor);
    }

    // Set pixel directly, skip any checks
    private byte setPixelInternal(int x, int y, byte color) {
        parent.markDirty(x, y);
        byte bef = this.getPixel(x, y);
        this.data[this.index(x, y, this.width, this.height)] = color;
        return bef;
    }

    @Override
    public byte getPixel(int x, int y) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            return 0;
        }
        return this.getPixelDirect(x, y);
    }

    private byte getPixelDirect(int x, int y) {
        //return this.data[x * this.height + y];
        return this.data[this.index(x, y, this.width, this.height)];
    }

    @Override
    public void markAreaDirty(int x, int y, int w, int h) {
        parent.markDirty(x, y);
        parent.markDirty(x + w, y);
        parent.markDirty(x, y + h);
        parent.markDirty(x + w, y + h);
    }

    @Override
    public void draw() {
        bufferLock().lock();
        long before = System.nanoTime();
        for (int col = 0; col < parent.getWidth(); col++) {
            for (int row = 0; row < parent.getHeight(); row++) {
                ClientsideMap renderTarget = parent.getMap(col, row);
                byte[] targetArr = renderTarget.getData();
                for (int y = 0; y < 128; y++) {
                    System.arraycopy(data, index(col * 128, row * 128 + y), targetArr, y * 128, 128);
                }
            }
        }
        long after = System.nanoTime();
        long diff = after - before;
        bufferLock().unlock();
        //Logger.getLogger("Maps Debug").info("Took " + diff + " ns (" + TimeUnit.NANOSECONDS.toMillis(diff) + ") [" + TimeUnit.NANOSECONDS.toMicros(diff) + "]");
    }

    @Override
    public MapGraphics<MapAccess> copy() {
        BasicMapGraphics copy = new BasicMapGraphics(parent);
        bufferLock().lock();
        System.arraycopy(
                this.getDirectAccessData(),
                0,
                copy.getDirectAccessData(),
                0,
                this.getDirectAccessData().length
        );
        bufferLock().unlock();
        return copy;
    }

    @Override
    public synchronized byte[] getDirectAccessData() {
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
