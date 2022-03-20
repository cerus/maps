package dev.cerus.maps.version;

import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.util.ReflectionUtil;
import java.util.Collections;
import java.util.stream.Collectors;
import net.minecraft.server.v1_16_R3.DataWatcher;
import net.minecraft.server.v1_16_R3.DataWatcherObject;
import net.minecraft.server.v1_16_R3.DataWatcherRegistry;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.MapIcon;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PacketPlayOutMap;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.MapMeta;

public class VersionAdapter16R3 implements VersionAdapter {

    @Override
    public Object makeMapPacket(final boolean ignoreBounds, final ClientsideMap map) {
        return new PacketPlayOutMap(
                map.getId(),
                (byte) 0,
                true,
                true,
                map.getMarkers().stream()
                        .map(cursor -> new MapIcon(
                                MapIcon.Type.a(cursor.getType()),
                                cursor.getCompressedX(),
                                cursor.getCompressedY(),
                                cursor.getDirection(),
                                !cursor.hasCaption() ? null
                                        : IChatBaseComponent.ChatSerializer.a(cursor.getCaptionString())
                        ))
                        .collect(Collectors.toList()),
                map.getData(),
                ignoreBounds ? 0 : map.getX(),
                ignoreBounds ? 0 : map.getY(),
                ignoreBounds ? 128 : map.getWidth(),
                ignoreBounds ? 128 : map.getHeight()
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
            final DataWatcherObject<ItemStack> itemObj = DataWatcherRegistry.g.a((byte) 7);
            final DataWatcher.Item<ItemStack> itemItem = new DataWatcher.Item<>(itemObj, CraftItemStack.asNMSCopy(mapItem));
            ReflectionUtil.set("b", packet.getClass(), packet, Collections.singletonList(itemItem));
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return packet;
    }

    @Override
    public void sendPacket(final Player player, final Object packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket((Packet<?>) packet);
    }

}
