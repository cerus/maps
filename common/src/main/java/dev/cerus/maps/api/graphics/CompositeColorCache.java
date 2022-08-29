package dev.cerus.maps.api.graphics;

import dev.cerus.maps.util.BitwiseUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CompositeColorCache {

    private static final Map<Long, Byte> COMPOSITIONS = new HashMap<>();
    private static boolean enabled;

    static {
        enabled = true;
    }

    public static byte getComposite(final byte color1, final byte color2, final float alpha) {
        return COMPOSITIONS.getOrDefault(key(color1, color2, alpha), (byte) -1);
    }

    public static byte getCompositeOrCompute(final byte color1, final byte color2, final float alpha, final Supplier<Byte> func) {
        return enabled ? COMPOSITIONS.computeIfAbsent(key(color1, color2, alpha), $ -> func.get()) : func.get();
    }

    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        COMPOSITIONS.clear();
        enabled = false;
    }

    public static void clear() {
        COMPOSITIONS.clear();
    }

    public static int size() {
        return COMPOSITIONS.size();
    }

    private static long key(final byte color1, final byte color2, final float alpha) {
        final long o1 = BitwiseUtil.pack(0L, Float.floatToIntBits(alpha), 0);
        final long o2 = BitwiseUtil.pack(0L, color1, 32);
        final long o3 = BitwiseUtil.pack(0L, color2, 40);
        return o1 | o2 | o3;
    }

}
