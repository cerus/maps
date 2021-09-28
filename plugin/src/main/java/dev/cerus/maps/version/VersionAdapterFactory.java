package dev.cerus.maps.version;

import dev.cerus.maps.api.version.VersionAdapter;
import org.bukkit.Bukkit;

public class VersionAdapterFactory {

    public VersionAdapter makeAdapter() {
        String version = Bukkit.getVersion();
        version = version.substring(version.indexOf("MC: ") + 4, version.lastIndexOf(')'));

        switch (version) {
            case "1.17":
            case "1.17.0":
            case "1.17.1":
                return new VersionAdapter17R1();
            default:
                return null;
        }
    }

}
