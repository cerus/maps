package dev.cerus.maps.api.graphics;

public class ClientsideMapGraphics extends MapGraphics {

    private static final int W = 128;

    private final byte[] data = new byte[W * W];
    private boolean dirty;

    public ClientsideMapGraphics() {
        this.dirty = false;
    }

    @Override
    public void fill(final byte color) {
        this.fillRect(0, 0, W, W, color);
    }

    @Override
    public byte setPixel(final int x, final int z, final byte color) {
        if (x < 0 || z < 0 || x >= W || z >= W) {
            return (byte) -1;
        }

        final byte current = this.data[x + z * W];
        this.data[x + z * W] = color;
        if (current != color) {
            this.markDirty();
        }
        return current;
    }

    public byte[] getData() {
        return this.data;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void markClean() {
        this.dirty = false;
    }

    public boolean isDirty() {
        return this.dirty;
    }

}
