package dev.cerus.maps.version;

import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.util.ReflectionUtil;
import java.util.Collections;
import java.util.stream.Collectors;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.MapMeta;

public class VersionAdapter17R1 implements VersionAdapter {

    @Override
    public Object makeMapPacket(final boolean ignoreBounds, final ClientsideMap map) {
        return new PacketPlayOutMap(
                map.getId(),
                (byte) 0,
                true,
                map.getMarkers().stream()
                        .map(cursor -> new MapIcon(
                                MapIcon.Type.a(cursor.getType()),
                                cursor.getCompressedX(),
                                cursor.getCompressedY(),
                                cursor.getDirection(),
                                IChatBaseComponent.ChatSerializer.a(cursor.getCaptionString())
                        ))
                        .collect(Collectors.toList()),
                new WorldMap.b(
                        ignoreBounds ? 0 : map.getX(),
                        ignoreBounds ? 0 : map.getY(),
                        ignoreBounds ? 128 : map.getWidth(),
                        ignoreBounds ? 128 : map.getHeight(),
                        map.getData()
                )
        );
    }

    @Override
    public Object makeFramePacket(final int frameId, final ClientsideMap map) {
        final PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(frameId, new DataWatcher(null), false);

        final org.bukkit.inventory.ItemStack mapItem = new org.bukkit.inventory.ItemStack(Material.FILLED_MAP, 1);
        final MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
        mapMeta.setMapId(map.getId());
        mapItem.setItemMeta(mapMeta);

        try {
            final DataWatcherObject<ItemStack> itemObj = DataWatcherRegistry.g.a((byte) 8);
            final DataWatcher.Item<ItemStack> itemItem = new DataWatcher.Item<>(itemObj, CraftItemStack.asNMSCopy(mapItem));
            ReflectionUtil.set("b", packet.getClass(), packet, Collections.singletonList(itemItem));
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return packet;
    }

    @Override
    public void sendPacket(final Player player, final Object packet) {
        ((CraftPlayer) player).getHandle().b.sendPacket((Packet<?>) packet);
    }

}
