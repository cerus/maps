package dev.cerus.maps.version;

import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.api.Frame;
import dev.cerus.maps.api.version.PacketListener;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.util.MinecraftVersion;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class VersionAdapter19R3 implements VersionAdapter {

    private final AtomicInteger entityIdHolder;
    private Field netManField;

    public VersionAdapter19R3() {
        Field entityIdField = Arrays.stream(Entity.class.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> field.getType() == AtomicInteger.class)
                .findFirst().orElseThrow();
        entityIdField.setAccessible(true);
        try {
            entityIdHolder = (AtomicInteger) entityIdField.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void spawnBarrierParticle(final Player player, final Location loc) {
        player.spawnParticle(Particle.BLOCK_MARKER, loc, 1, Material.BARRIER.createBlockData());
    }

    @Override
    public Object makeMapPacket(final boolean ignoreBounds, final ClientsideMap map) {
        final int x = ignoreBounds ? 0 : map.getBoundsX();
        final int y = ignoreBounds ? 0 : map.getBoundsY();
        final int w = ignoreBounds ? 128 : Math.max(1, map.getBoundsWidth());
        final int h = ignoreBounds ? 128 : Math.max(1, map.getBoundsHeight());

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
                                !cursor.hasCaption() ? null : IChatBaseComponent.ChatSerializer.a(cursor.getCaptionString())
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
        final org.bukkit.inventory.ItemStack mapItem = new org.bukkit.inventory.ItemStack(Material.FILLED_MAP, 1);
        final MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
        mapMeta.setMapId(map.getId());
        mapItem.setItemMeta(mapMeta);

        final List<DataWatcher.b<?>> dwItems = Arrays.asList(
                new DataWatcher.b<>(8, DataWatcherRegistry.h, CraftItemStack.asNMSCopy(mapItem)),
                new DataWatcher.b<>(0, DataWatcherRegistry.a, (byte) (visible ? 0 : 0x20))
        );
        return new PacketPlayOutEntityMetadata(frameId, dwItems);
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
                frame.isGlowing() ? EntityTypes.S : EntityTypes.af,
                switch (frame.getFacing()) {
                    case UP -> 1;
                    case NORTH -> 2;
                    case SOUTH -> 3;
                    case WEST -> 4;
                    case EAST -> 5;
                    default -> 0;
                },
                new Vec3D(0, 0, 0),
                switch (frame.getFacing()) {
                    case NORTH -> -180;
                    case EAST -> -90;
                    case WEST -> 90;
                    default -> 0;
                }
        );
    }

    @Override
    public Object makeFrameDespawnPacket(final Frame frame) {
        return new PacketPlayOutEntityDestroy(frame.getEntityId());
    }

    @Override
    public void sendPacket(final Player player, final Object packet) {
        ((CraftPlayer) player).getHandle().b.a((Packet<?>) packet);
    }

    @Override
    public void inject(final Player player, final PacketListener listener, final JavaPlugin plugin) {
        final NetworkManager networkManager;
        try {
            networkManager = this.getNetworkManager(((CraftPlayer) player).getHandle().b);
        } catch (final IllegalAccessException | NoSuchFieldException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to inject packet handler into player %s".formatted(player.getName()), e);
            return;
        }
        networkManager.m.pipeline().addBefore("packet_handler", "maps_listener", new PacketHandler19R3(player, listener, plugin));
    }

    private NetworkManager getNetworkManager(final PlayerConnection b) throws IllegalAccessException, NoSuchFieldException {
        if (this.netManField == null) {
            this.netManField = PlayerConnection.class.getDeclaredField("h");
            this.netManField.setAccessible(true);
        }
        return (NetworkManager) this.netManField.get(b);
    }

    @Override
    public int nextEntityId() {
        return entityIdHolder.incrementAndGet();
    }

    @Override
    public boolean supportsVersion(MinecraftVersion version) {
        return version.equals(MinecraftVersion.RELEASE_1_19_4);
    }
}
