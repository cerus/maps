package dev.cerus.maps.version;

import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.util.MinecraftVersion;
import java.util.Set;
import org.bukkit.Bukkit;
import org.reflections.Reflections;
import static org.reflections.scanners.Scanners.SubTypes;

public class VersionAdapterFactory {

    public static final String MIN_VER = "1.16.5";
    public static final String MAX_VER = "1.21";

    public VersionAdapter makeAdapter() {
        String version = Bukkit.getVersion();
        version = version.substring(version.indexOf("MC: ") + 4, version.lastIndexOf(')'));

        return switch (version) {
            case "1.16.5" -> new VersionAdapter16R3();
            case "1.17", "1.17.0", "1.17.1" -> new VersionAdapter17R1();
            case "1.18", "1.18.1" -> new VersionAdapter18R1();
            case "1.18.2" -> new VersionAdapter18R2();
            case "1.19", "1.19.1", "1.19.2" -> new VersionAdapter19R1();
            case "1.19.3" -> new VersionAdapter19R2();
            case "1.19.4" -> new VersionAdapter19R3();
            case "1.20", "1.20.1" -> new VersionAdapter20R1();
            case "1.20.2" -> new VersionAdapter20R2();
            case "1.20.3", "1.20.4" -> new VersionAdapter20R3();
            case "1.20.5", "1.20.6" -> new VersionAdapter20R4();
            case "1.21", "1.21.1" -> new VersionAdapter21R1();
            default -> null;
        };
    }

}
