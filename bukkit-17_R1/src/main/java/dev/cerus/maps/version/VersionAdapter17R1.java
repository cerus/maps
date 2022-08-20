package dev.cerus.maps.version;

import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.api.Frame;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.util.ReflectionUtil;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.MapMeta;

public class VersionAdapter17R1 implements VersionAdapter {

    @Override
    public Object makeMapPacket(final boolean ignoreBounds, final ClientsideMap map) {
        final int x = ignoreBounds ? 0 : map.getX();
        final int y = ignoreBounds ? 0 : map.getY();
        final int w = ignoreBounds ? 128 : map.getWidth();
        final int h = ignoreBounds ? 128 : map.getHeight();

        final byte[] data;
        if (ignoreBounds) {
            data = map.getData();
        } else {
            data = new byte[w * h];
            for (int xx = 0; xx < w; ++xx) {
                for (int yy = 0; yy < h; ++yy) {
                    data[xx + yy * w] = map.getData()[x + xx + (y + yy) * 128];
                }
            }
        }

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
                        x,
                        y,
                        w,
                        h,
                        data
                )
        );
    }

    @Override
    public Object makeFramePacket(final int frameId, final boolean visible, final ClientsideMap map) {
        final PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(frameId, new DataWatcher(null), false);

        final org.bukkit.inventory.ItemStack mapItem = new org.bukkit.inventory.ItemStack(Material.FILLED_MAP, 1);
        final MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
        mapMeta.setMapId(map.getId());
        mapItem.setItemMeta(mapMeta);

        try {
            final DataWatcherObject<ItemStack> itemObj = DataWatcherRegistry.g.a((byte) 8);
            final DataWatcher.Item<ItemStack> itemItem = new DataWatcher.Item<>(itemObj, CraftItemStack.asNMSCopy(mapItem));
            final DataWatcherObject<Byte> flagsObj = DataWatcherRegistry.a.a((byte) 0);
            final DataWatcher.Item<Byte> flagsItem = new DataWatcher.Item<>(flagsObj, (byte) (visible ? 0 : 0x20));
            ReflectionUtil.set("b", packet.getClass(), packet, Arrays.asList(
                    itemItem,
                    flagsItem
            ));
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return packet;
    }

    @Override
    public Object makeFrameSpawnPacket(final Frame frame) {
        return new PacketPlayOutSpawnEntity(
                frame.getEntityId(),
                UUID.randomUUID(),
                frame.getPosX(),
                frame.getPosY(),
                frame.getPosZ(),
                frame.getFacing() == BlockFace.DOWN ? 90 : frame.getFacing() == BlockFace.UP ? -90 : 0,
                switch (frame.getFacing()) {
                    case NORTH -> -180;
                    case EAST -> -90;
                    case WEST -> 90;
                    default -> 0;
                },
                EntityTypes.R,
                switch (frame.getFacing()) {
                    case UP -> 1;
                    case NORTH -> 2;
                    case SOUTH -> 3;
                    case WEST -> 4;
                    case EAST -> 5;
                    default -> 0;
                },
                new Vec3D(0, 0, 0)
        );
    }

    @Override
    public Object makeFrameDespawnPacket(final Frame frame) {
        return new PacketPlayOutEntityDestroy(frame.getEntityId());
    }

    @Override
    public void sendPacket(final Player player, final Object packet) {
        ((CraftPlayer) player).getHandle().b.sendPacket((Packet<?>) packet);
    }

}
