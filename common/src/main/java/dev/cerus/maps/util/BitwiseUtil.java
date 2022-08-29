package dev.cerus.maps.util;

public class BitwiseUtil {

    private BitwiseUtil() {
    }

    public static long pack(final long out, final byte value, final int pos) {
        return out | ((long) value << pos);
    }

    public static long pack(final long out, final int value, final int pos) {
        return out | ((long) value << pos);
    }

    public static long pack(final long out, final float value, final int pos) {
        return out | ((long) value << pos);
    }

    public static long unpack(final long v, final int pos, final long max) {
        return (v >> pos) & max;
    }

}
