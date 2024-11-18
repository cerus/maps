package dev.cerus.maps.util;

import java.util.Arrays;
import java.util.Objects;
import org.bukkit.Bukkit;

public class MinecraftVersion implements Comparable<MinecraftVersion> {

    public static final MinecraftVersion RELEASE_1_8 = parse("1.8");
    public static final MinecraftVersion RELEASE_1_12 = parse("1.12");
    public static final MinecraftVersion RELEASE_1_16 = parse("1.16");
    public static final MinecraftVersion RELEASE_1_16_5 = parse("1.16.5");
    public static final MinecraftVersion RELEASE_1_17 = parse("1.17");
    public static final MinecraftVersion RELEASE_1_17_1 = parse("1.17.1");
    public static final MinecraftVersion RELEASE_1_18 = parse("1.18");
    public static final MinecraftVersion RELEASE_1_18_1 = parse("1.18.1");
    public static final MinecraftVersion RELEASE_1_18_2 = parse("1.18.2");
    public static final MinecraftVersion RELEASE_1_19 = parse("1.19");
    public static final MinecraftVersion RELEASE_1_19_1 = parse("1.19.1");
    public static final MinecraftVersion RELEASE_1_19_2 = parse("1.19.2");
    public static final MinecraftVersion RELEASE_1_19_3 = parse("1.19.3");
    public static final MinecraftVersion RELEASE_1_19_4 = parse("1.19.4");
    public static final MinecraftVersion RELEASE_1_20 = parse("1.20");
    public static final MinecraftVersion RELEASE_1_20_1 = parse("1.20.1");
    public static final MinecraftVersion RELEASE_1_20_2 = parse("1.20.2");
    public static final MinecraftVersion RELEASE_1_20_3 = parse("1.20.3");
    public static final MinecraftVersion RELEASE_1_20_4 = parse("1.20.4");
    public static final MinecraftVersion RELEASE_1_20_5 = parse("1.20.5");
    public static final MinecraftVersion RELEASE_1_20_6 = parse("1.20.6");
    public static final MinecraftVersion RELEASE_1_21 = parse("1.21");
    public static final MinecraftVersion RELEASE_1_21_1 = parse("1.21.1");

    public static final MinecraftVersion[] VALUES = new MinecraftVersion[] {
            RELEASE_1_16_5, RELEASE_1_17, RELEASE_1_17_1, RELEASE_1_18,
            RELEASE_1_18_1, RELEASE_1_18_2, RELEASE_1_19, RELEASE_1_19_1,
            RELEASE_1_19_2, RELEASE_1_19_3, RELEASE_1_19_4, RELEASE_1_20,
            RELEASE_1_20_1, RELEASE_1_20_2, RELEASE_1_20_3, RELEASE_1_20_4,
            RELEASE_1_20_5, RELEASE_1_20_6, RELEASE_1_21, RELEASE_1_21_1
    };
    public static final MinecraftVersion MIN = Arrays.stream(VALUES)
            .min(MinecraftVersion::compareTo).orElseThrow();
    public static final MinecraftVersion MAX = Arrays.stream(VALUES)
            .max(MinecraftVersion::compareTo).orElseThrow();

    private final int major;
    private final int minor;
    private final int patch;

    public MinecraftVersion(int major, int minor, int patch) {
        if (major <= 0) {
            throw new IllegalArgumentException("Major version must be greater than zero");
        }
        if (minor <= 0) {
            throw new IllegalArgumentException("Minor version must be greater than zero");
        }
        if (patch < 0) {
            throw new IllegalArgumentException("Patch version must be greater than or equal to zero");
        }
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public static MinecraftVersion of(int major, int minor, int patch) {
        return new MinecraftVersion(major, minor, patch);
    }

    public static MinecraftVersion parse(String version) {
        if (!version.matches("\\d\\.\\d{1,2}(\\.\\d{1,2})?")) {
            throw new IllegalArgumentException("Invalid version: " + version);
        }
        String[] split = version.split("\\.");
        return of(Integer.parseInt(split[0]), Integer.parseInt(split[1]), split.length < 3 ? 0 : Integer.parseInt(split[2]));
    }

    public static MinecraftVersion current() {
        String version = Bukkit.getVersion();
        version = version.substring(version.indexOf("MC: ") + 4, version.lastIndexOf(')'));
        return parse(version);
    }

    public boolean lessThanEquals(MinecraftVersion other) {
        return compareTo(other) <= 0;
    }

    public boolean lessThan(MinecraftVersion other) {
        return compareTo(other) < 0;
    }

    public boolean greaterThanEquals(MinecraftVersion other) {
        return compareTo(other) >= 0;
    }

    public boolean greaterThan(MinecraftVersion other) {
        return compareTo(other) > 0;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    @Override
    public int compareTo(MinecraftVersion o) {
        if (major != o.major) {
            return major - o.major;
        }
        if (minor != o.minor) {
            return minor - o.minor;
        }
        return patch - o.patch;
    }

    @Override
    public String toString() {
        if (patch == 0) {
            return major + "." + minor;
        }
        return major + "." + minor + "." + patch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinecraftVersion that = (MinecraftVersion) o;
        return major == that.major && minor == that.minor && patch == that.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }
}
