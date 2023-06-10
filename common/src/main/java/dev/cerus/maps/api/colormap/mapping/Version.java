package dev.cerus.maps.api.colormap.mapping;

/**
 * Represents a Minecraft version
 * <p>
 * See also <a href="https://semver.org">semver.org</a>
 *
 * @param major First part of the version
 * @param minor Middle part of the version
 * @param patch Last part of the version
 */
public record Version(int major, int minor, int patch) implements Comparable<Version> {

    public static final Version MAX = new Version(999, 999, 999);
    public static final Version ZERO = new Version(0, 0, 0);

    @Override
    public int compareTo(final Version other) {
        if (other == null) {
            throw new NullPointerException();
        }
        return Integer.compare(this.toInt(), other.toInt());
    }

    private int toInt() {
        return (this.major * 1_000_000) + (this.minor * 1_000) + this.patch;
    }

}
