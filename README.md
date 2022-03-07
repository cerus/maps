<img width="25%" height="25%" align="left" src="https://cerus.dev/img/mc_map_item.png" alt="Minecraft map item">

<h2 align="center">maps</h2>
<p align="center">maps is a simple Spigot plugin and api for creating clientside maps and map screens. It is the successor
of <a href="https://github.com/cerus/packet-maps">packet-maps</a>.</p>

<hr>
<p align="center"><sub><sup>Made with ♥ by <a href="https://github.com/cerus">Cerus</a></sup></sub></p>
<br>

### Navigation

• [Features](#Features)\
• [Quick start for developers](#Quick-start-for-developers)\
• [Building](#Building)\
• [FAQ](#FAQ)\
• [Contributing](#Contributing)

<hr>

### Features

> **Please note:** This is not a standalone plugin, it is a toolkit for other plugins. You will only be able to create and manage map screens with this plugin.

• Clientside maps\
• Map screens (arrangement of clientside maps)\
• Simple and reasonably lightweight\
• Easy to use developer api\
• Supports 1.16.5, 1.17.1, 1.18.1 and 1.18.2

**What is the point of the plugin module?**\
See [FAQ](#FAQ)

<hr>

### Quick start for developers

> Please take a look at the wiki for an in-depth explanation of the api.

**Maven setup**

```xml

<dependencies>
    <dependency>
        <groupId>dev.cerus.maps</groupId>
        <artifactId>common</artifactId>
        <version>1.0.7</version>
        <scope>provided</scope> <!-- "provided" if the maps plugin is on the server, "compile" if not -->
    </dependency>

    <!-- You need the plugin module to access the map screen registry of the plugin. -->
    <!-- Don't add this dependency if you have your own storage solution. -->
    <dependency>
        <groupId>dev.cerus.maps</groupId>
        <artifactId>plugin</artifactId>
        <version>1.0.7</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**Quickstart**

```java
public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // This example depends on the "common" and "plugin" dependency.

        // Something important to keep in mind when using the plugin for storage:
        // The plugin loads the map screens 3 seconds after startup. (Check out
        // the MapsPlugin.java file for an explanation)

        this.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (final MapScreen screen : MapScreenRegistry.getScreens()) {
                final MapScreenGraphics graphics = screen.getGraphics();
                graphics.fill((byte) MapColor.WHITE_2.getId());
                graphics.drawText(5, 5, "There are " + Bukkit.getOnlinePlayers().size() + " players on the server", (byte) MapColor.BLACK_2.getId(), 2);
                screen.update(MapScreen.DirtyHandlingPolicy.IGNORE); // Send map screen to all online players
            }
        }, 4 * 20, 20);
        getCommand("mapstest").setExecutor(this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        // It's been ages since I've used the normal Bukkit command system 
        // so please forgive me if I use any bad practices here
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!command.getName().equals("mapstest")) {
            return true;
        }

        // This is really unsafe. Always do proper checks before casting 
        // things, but this will do for the sake of this quick start.
        final ItemStack item = player.getInventory().getItemInMainHand();
        final MapMeta mapMeta = (MapMeta) item.getItemMeta();
        final int mapId = mapMeta.getMapView().getId();

        final ClientsideMap clientsideMap = new ClientsideMap(mapId);
        final ClientsideMapGraphics graphics = clientsideMap.getGraphics();
        graphics.fill((byte) MapColor.BLACK_2.getId());
        graphics.drawText(5, 5, "Hello,", (byte) MapColor.BLACK_2.getId(), 1);
        graphics.drawText(5, 5 + MinecraftFont.Font.getHeight() + 5, player.getName(), (byte) MapColor.WHITE_2.getId(), 2);
        return true;
    }

}
```

<hr>

### Building

Requirements: Java 16, Git, Maven, Craftbukkit 1.16.5, 1.17.1, 1.18.1 and 1.18.2 installed in local Maven repo

Simply clone the repository, navigate into the directory and run `mvn clean package`. The plugin will be in `plugin/target` and the api
in `common/target`.

<hr>

### FAQ

**Why is there a plugin module if maps is not a standalone plugin?**\
The plugin handles the creation, management and storage of map screens. You do not need the plugin if you make your own creation, management and
storage solution.

Please feel free to open an issue or contact me if you have any questions that were not answered here.

<hr>

### Contributing

Thank you for your interest in contributing to this project! Before you do anything though please read the [contribution guidelines](CONTRIBUTING.md)
thoroughly. Contributions that do not conform to the guidelines might be rejected.