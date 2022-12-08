package dev.cerus.maps.version;

import dev.cerus.maps.api.version.VersionAdapter;
import org.bukkit.Bukkit;

public class VersionAdapterFactory {

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
            default -> null;
        };
    }

}
