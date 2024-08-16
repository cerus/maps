<img width="25%" height="25%" align="left" src="https://i.ibb.co/0h1Z8M2/mc-map-item.png" alt="Minecraft map item">

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
• [Contributing](#Contributing)\
• [Sources](#Sources)

<hr>

### Features

> **Please note:** This is not a standalone plugin, it is a toolkit for other plugins. You will only be able to create and manage map screens with
> this plugin.

• Clientside maps\
• Map screens (arrangement of clientside maps)\
• Simple and reasonably lightweight\
• Easy to use developer api\
• Advanced engine features
like [alpha compositing](https://en.wikipedia.org/wiki/Alpha_compositing) ([Image](https://cerus.dev/img/maps_alpha_composition.png))\
• Efficient click handling\
• Supports 1.16.5 - 1.21.1

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
        <version>3.8.7</version>
        <scope>provided</scope> <!-- "provided" if the maps plugin is on the server, "compile" if not -->
    </dependency>

    <!-- You need the plugin module to access the map screen registry of the plugin. -->
    <!-- Don't add this dependency if you have your own storage solution. -->
    <dependency>
        <groupId>dev.cerus.maps</groupId>
        <artifactId>plugin</artifactId>
        <version>3.8.7</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**Quickstart**

```java
import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.graphics.ClientsideMapGraphics;
import dev.cerus.maps.api.graphics.ColorCache;
import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.version.VersionAdapterFactory;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MinecraftFont;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // This example depends on the "common" and "plugin" dependency.

        for (final MapScreen screen : MapScreenRegistry.getScreens()) {
            final MapGraphics<?, ?> graphics = screen.getGraphics();
            graphics.fillComplete(ColorCache.rgbToMap(255, 255, 255)); // Convert rgb(255, 255, 255) to map color and fill the screen
            graphics.drawText(5, 5, "There are " + Bukkit.getOnlinePlayers().size() + " players on the server", ColorCache.rgbToMap(0, 0, 0), 2);
            screen.spawnFrames(Bukkit.getOnlinePlayers().toArray(new Player[0])); // Send the screen frames to all online players
            screen.sendMaps(true); // Send map data to all online players
        }
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

        final ClientsideMap clientsideMap = new ClientsideMap(mapId); // Create clientside map with given id
        final ClientsideMapGraphics graphics = new ClientsideMapGraphics(); // Create graphics buffer

        graphics.fillComplete(ColorCache.rgbToMap(0, 0, 0)); // Fill with rgb(0, 0, 0)
        graphics.drawText(5, 5, "Hello,", ColorCache.rgbToMap(255, 255, 255), 1); // Draw text
        graphics.drawText(5, 5 + MinecraftFont.Font.getHeight() + 5, player.getName(), ColorCache.rgbToMap(255, 255, 255), 2);

        clientsideMap.draw(graphics); // Draw the buffer onto the map
        clientsideMap.sendTo(new VersionAdapterFactory().makeAdapter(), player); // Send the map to the player
        return true;
    }

}
```

<hr>

### Building

Requirements: Java 21, Git, Maven, CraftBukkit 1.16.5, 1.17.1, 1.18.1, 1.18.2, 1.19.1, 1.19.3, 1.19.4, 1.20, 1.20.2, 1.20.4, 1.20.6 and 1.21 installed in local
Maven repo

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

<hr>

### Sources

Lots of graphics features were implemented with the help of the following resources:

- [Alpha Compositing - Wikipedia](https://en.wikipedia.org/wiki/Alpha_compositing#Alpha_blending)
- [Alpha Compositing - Bartosz Ciechanowski](https://ciechanow.ski/alpha-compositing/)
- [So you want to write a GUI framework](http://www.cmyr.net/blog/gui-framework-ingredients.html)
- [Line drawing on a grid](https://www.redblobgames.com/grids/line-drawing.html)
- [Midpoint ellipse drawing algorithm - GeeksforGeeks](https://www.geeksforgeeks.org/midpoint-ellipse-drawing-algorithm/)
- [Flood fill - Wikipedia](https://en.wikipedia.org/wiki/Flood_fill)
- [Box blur - Wikipedia](https://en.wikipedia.org/wiki/Box_blur)
- [Porter/Duff Compositing and Blend Modes - Søren Sandmann Pedersen](http://ssp.impulsetrain.com/porterduff.html)
- StackOverflow