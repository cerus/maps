package dev.cerus.maps.api.graphics;

/**
 * Graphics implementation that's completely standalone and versatile. Could be used for sprites for example.
 */
public class StandaloneMapGraphics extends MapGraphics<MapGraphics<?>> {

    private final int width;
    private final int height;
    private byte[] data;

    public StandaloneMapGraphics(int width, int height) {
        this(width, height, new byte[width * height]);
    }

    public StandaloneMapGraphics(int width, int height, byte[] data) {
        this.width = width;
        this.height = height;
        setDirectAccessData(data);
    }

    public static StandaloneMapGraphics copyOf(MapGraphics<?> graphics) {
        if (!graphics.hasDirectAccessCapabilities()) {
            throw new IllegalArgumentException("Graphics needs direct access capabilities");
        }

        StandaloneMapGraphics out = new StandaloneMapGraphics(graphics.getWidth(), graphics.getHeight());
        graphics.bufferLock().lock();
        for (int r = 0; r < out.height; r++) {
            int pos = out.index(0, r, out.width, out.height);
            System.arraycopy(
                    graphics.getDirectAccessData(),
                    pos,
                    out.data,
                    pos,
                    out.width
            );
        }
        graphics.bufferLock().unlock();
        return out;
    }

    @Override
    public byte setPixel(int x, int y, float alpha, byte color) {
        // Lots of bounds and alpha checking
        float normAlpha = this.normalizeAlpha(alpha);
        if (x < 0 || x >= this.width || y < 0 || y >= this.height || normAlpha == 0f) {
            return color;
        }

        // More compositing
        byte actualColor = normAlpha == 1f ? color : this.calculateComposite(color, this.getPixel(x, y), normAlpha);
        if (this.getPixel(x, y) == actualColor) {
            return color;
        }
        return this.setPixelInternal(x, y, actualColor);
    }

    private byte setPixelInternal(int x, int y, byte color) {
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
    }

    @Override
    public void draw() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MapGraphics<MapGraphics<?>> copy() {
        StandaloneMapGraphics copy = new StandaloneMapGraphics(this.width, this.height);
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                copy.setPixelInternal(x, y, this.getPixelDirect(x, y));
            }
        }
        return copy;
    }

    @Override
    public synchronized byte[] getDirectAccessData() {
        return this.data;
    }

    public void setDirectAccessData(byte[] data) {
        if (data.length != width * height) {
            throw new IllegalArgumentException("data.length != width * height");
        }
        this.data = data;
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
