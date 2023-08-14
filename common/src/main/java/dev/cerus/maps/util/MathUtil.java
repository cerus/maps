package dev.cerus.maps.util;

public final class MathUtil {

    private MathUtil() {
    }

    public static double triSideA(final double sideB, final double angleAlpha) {
        final double c = triHypotenuseB(sideB, angleAlpha);
        return fastSqrt((c * c) - (sideB * sideB));
    }

    public static double triHypotenuseB(final double sideB, final double angleAlpha) {
        return sideB / Math.cos(Math.toRadians(angleAlpha));
    }

    public static double fastSqrt(final double d) {
        final double sqrt = veryFastSqrt(d);
        return (sqrt + d / sqrt) * 0.5d;
    }

    public static double veryFastSqrt(final double d) {
        return Double.longBitsToDouble(((Double.doubleToLongBits(d) - (1L << 52)) >> 1) + (1L << 61));
    }

}
