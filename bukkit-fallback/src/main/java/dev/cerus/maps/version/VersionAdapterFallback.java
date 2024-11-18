package dev.cerus.maps.version;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.cerus.maps.api.ClientsideMap;
import dev.cerus.maps.api.Frame;
import dev.cerus.maps.api.version.PacketListener;
import dev.cerus.maps.api.version.VersionAdapter;
import dev.cerus.maps.util.MinecraftVersion;
import dev.cerus.mirror.Mirror;
import dev.cerus.mirror.Mirrored;
import dev.cerus.mirror.annotation.Adapt;
import dev.cerus.mirror.annotation.CtorParam;
import dev.cerus.mirror.annotation.EnumConstant;
import dev.cerus.mirror.annotation.Reflect;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class VersionAdapterFallback implements VersionAdapter {

    private final WrappedCraftEntityType craftEntityType;
    private final WrappedCraftItemStack craftItemStack;
    private final Object dwsByte;
    private final Object dwsItem;

    private final Mirror mirror;
    private Field netManField;

    public VersionAdapterFallback(Mirror mirror) {
        this.mirror = mirror;

        craftEntityType = new WrappedCraftEntityType(mirror);
        craftItemStack = new WrappedCraftItemStack(mirror);
        WrappedDataWatcherRegistry dataWatcherRegistry = new WrappedDataWatcherRegistry(mirror);
        dwsByte = dataWatcherRegistry.getSerializer(0);
        dwsItem = dataWatcherRegistry.getSerializer(7);
    }

    @Override
    public void spawnBarrierParticle(final Player player, final Location loc) {
        Particle particle = (Particle) mirror.revert(WrappedParticle.BLOCK_MARKER);
        if (particle.getDataType() != Void.class) {
            player.spawnParticle(particle, loc, 1, Material.BARRIER.createBlockData());
        } else {
            player.spawnParticle(particle, loc, 1);
        }
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

        return new WrappedPacketOutMap(
                new WrappedMapId(map.getId()),
                (byte) 0,
                true,
                map.getMarkers().stream()
                        .map(cursor -> new WrappedMapIcon(
                                WrappedBuiltInRegistries.MAP_DECORATION_TYPES.get(cursor.getType()),
                                cursor.getCompressedX(),
                                cursor.getCompressedY(),
                                cursor.getDirection(),
                                !cursor.hasCaption() ? Optional.empty() : parse(cursor.getCaptionString())
                        ))
                        .collect(Collectors.toList()),
                new WrappedMapSlice(x, y, w, h, data)
        );
    }

    private Optional<?> parse(String s) {
        if (s == null) {
            return Optional.empty();
        }
        JsonElement element = JsonParser.parseString(s);
        if (element == null) {
            return Optional.empty();
        }
        return WrappedComponentSerialization.COMPONENTS.parse(JsonOps.INSTANCE, element).result();
    }

    @Override
    public Object makeFramePacket(final int frameId, final boolean visible, final ClientsideMap map) {
        final org.bukkit.inventory.ItemStack mapItem = new org.bukkit.inventory.ItemStack(Material.FILLED_MAP, 1);
        final MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
        mapMeta.setMapId(map.getId());
        mapItem.setItemMeta(mapMeta);

        Bukkit.getLogger().info("HELLOOOOO???");

        Object mapItemNms = craftItemStack.asNMSCopy(mapItem);
        Bukkit.getLogger().info("DEBUG: mapItemNms null = " + (mapItemNms == null) + "; dwsItem null = " + (dwsItem == null) + "; dwsByte null = " + (dwsByte == null));

        List<Object> dwItems = Arrays.asList(
                mirror.revert(new WrappedDataWatcherObject(8, dwsItem, mapItemNms)),
                mirror.revert(new WrappedDataWatcherObject(0, dwsByte, (byte) (visible ? 0 : 0x20)))
        );
        return new WrappedPacketOutEntityMetadata(frameId, dwItems);
    }

    @Override
    public Object makeFrameSpawnPacket(final Frame frame) {
        EntityType frameType = /*(EntityType) mirror.revert(frame.isGlowing() ? WrappedEntityType.GLOW_ITEM_FRAME : WrappedEntityType.ITEM_FRAME)*/EntityType.valueOf("GLOW_ITEM_FRAME");
        Bukkit.getLogger().info(mirror.revert(WrappedEntityType.GLOW_ITEM_FRAME).toString());
        Bukkit.getLogger().info(mirror.revert(WrappedEntityType.ITEM_FRAME).toString());
        Bukkit.getLogger().info("DEBUG: spawning frame of type " + frameType + " (maps to " + craftEntityType.bukkitToMinecraft(frameType) + ")");
        return new WrappedPacketOutSpawnEntity(
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
                craftEntityType.bukkitToMinecraft(frameType),
                switch (frame.getFacing()) {
                    case UP -> 1;
                    case NORTH -> 2;
                    case SOUTH -> 3;
                    case WEST -> 4;
                    case EAST -> 5;
                    default -> 0;
                },
                new WrappedVec3D(0, 0, 0),
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
        return new WrappedPacketOutEntityDestroy(frame.getEntityId());
    }

    @Override
    public void sendPacket(final Player player, final Object packet) {
        if (!(packet instanceof Common.WrappedPacket wp)) {
            throw new IllegalArgumentException("packet must be an instance of WrappedPacket");
        }
        Common.WrappedCraftPlayer wcp = (Common.WrappedCraftPlayer) mirror.adapt(player);
        wcp.sendPacket(wp);
    }

    @Override
    public void inject(final Player player, final PacketListener listener, final JavaPlugin plugin) {
        Common.WrappedCraftPlayer wcp = (Common.WrappedCraftPlayer) mirror.adapt(player);
        wcp.getPipeline().addBefore("packet_handler", "maps_listener", new PacketHandlerFallback(player, listener, plugin, mirror));
    }

    @Override
    public int nextEntityId() {
        return 0;
    }

    @Override
    public boolean supportsVersion(MinecraftVersion version) {
        return false;
    }

    @Adapt("net.minecraft.network.syncher.DataWatcherRegistry")
    public static class WrappedDataWatcherRegistry {
        private final Mirror mirror;

        private WrappedDataWatcherRegistry() {
            this(null);
        }

        public WrappedDataWatcherRegistry(Mirror mirror) {
            this.mirror = mirror;
        }

        public Object getSerializer(int id) {
            return mirror.invoke(this, new String[]{"getSerializer", "a"}, (int) id);
        }
    }

    @Adapt("net.minecraft.network.syncher.DataWatcher$c")
    public static class WrappedDataWatcherObject {
        private final int id;
        private final Object serializer;
        private final Object data;

        private WrappedDataWatcherObject() {
            this(0, null, null);
        }

        public WrappedDataWatcherObject(int id, Object serializer, Object data) {
            this.id = id;
            this.serializer = serializer;
            this.data = data;
        }

        // int, serializer, object
        @CtorParam
        private Object[] ctor() {
            return new Object[]{id, serializer, data};
        }
    }

    @Adapt("org.bukkit.craftbukkit.{{CB}}.inventory.CraftItemStack")
    public static class WrappedCraftItemStack {
        private final Mirror mirror;

        private WrappedCraftItemStack() {
            this(null);
        }

        public WrappedCraftItemStack(Mirror mirror) {
            this.mirror = mirror;
        }

        public Object asNMSCopy(ItemStack original) {
            return mirror.invoke(this, "asNMSCopy", original);
        }
    }

    @Adapt("org.bukkit.Particle")
    public enum WrappedParticle {
        @EnumConstant(constant = {"BLOCK_MARKER", "BARRIER"})
        BLOCK_MARKER
    }

    @Adapt("org.bukkit.entity.EntityType")
    public enum WrappedEntityType {
        @EnumConstant(constant = {"GLOW_ITEM_FRAME", "ITEM_FRAME"})
        GLOW_ITEM_FRAME,
        @EnumConstant(constant = "ITEM_FRAME")
        ITEM_FRAME
    }

    @Adapt("org.bukkit.craftbukkit.{{CB}}.entity.CraftEntityType")
    public static class WrappedCraftEntityType {
        private final Mirror mirror;

        private WrappedCraftEntityType() {
            this(null);
        }

        public WrappedCraftEntityType(Mirror mirror) {
            this.mirror = mirror;
        }

        public Object bukkitToMinecraft(EntityType type) {
            return mirror.invoke(this, "bukkitToMinecraft", type);
        }
    }

    @Adapt("net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity")
    public static class WrappedPacketOutEntityDestroy implements Common.WrappedPacket {
        @CtorParam
        private final int[] ids;

        private WrappedPacketOutEntityDestroy() {
            this(new int[0]);
        }

        public WrappedPacketOutEntityDestroy(int... ids) {
            this.ids = ids;
        }
    }

    @Adapt("net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata")
    public static class WrappedPacketOutEntityMetadata implements Common.WrappedPacket {
        private final int entityId;
        private final List<Object> dataWatcherObjects;

        private WrappedPacketOutEntityMetadata() {
            this(0, null);
        }

        public WrappedPacketOutEntityMetadata(int entityId, List<Object> dataWatcherObjects) {
            this.entityId = entityId;
            this.dataWatcherObjects = dataWatcherObjects;
        }

        @CtorParam
        private Object[] ctor() {
            return new Object[] {entityId, dataWatcherObjects};
        }
    }

    @Adapt("net.minecraft.world.phys.Vec3D")
    public static class WrappedVec3D {

        @Reflect(strategy = Reflect.Strategy.STRICT, nameOverride = "c", isFinal = true)
        @CtorParam
        public double x;
        @Reflect(strategy = Reflect.Strategy.STRICT, nameOverride = "d", isFinal = true)
        @CtorParam
        public double y;
        @Reflect(strategy = Reflect.Strategy.STRICT, nameOverride = "e", isFinal = true)
        @CtorParam
        public double z;

        public WrappedVec3D() {
        }

        public WrappedVec3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    @Adapt("net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity")
    public static class WrappedPacketOutSpawnEntity implements Common.WrappedPacket {
        private int entityId;
        private UUID entityUuid;
        private double posX;
        private double posY;
        private double posZ;
        private float pitch;
        private float yaw;
        private Object entityType;
        private int facing;
        private WrappedVec3D motion;
        private double extraData;

        private WrappedPacketOutSpawnEntity() {
        }

        public WrappedPacketOutSpawnEntity(int entityId, UUID entityUuid, double posX, double posY, double posZ, float pitch, float yaw, Object entityType, int facing, WrappedVec3D motion, int extraData) {
            this.entityId = entityId;
            this.entityUuid = entityUuid;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.pitch = pitch;
            this.yaw = yaw;
            this.entityType = entityType;
            this.facing = facing;
            this.motion = motion;
            this.extraData = extraData;
        }

        // int, UUID, double, double, double, float, float, EntityTypes<?>, int, Vec3D, double
        @CtorParam
        private Object[] ctor() {
            return new Object[] {entityId, entityUuid, posX, posY, posZ, pitch, yaw, entityType, facing, motion, extraData};
        }
    }

    @Adapt("net.minecraft.network.chat.ComponentSerialization")
    public static class WrappedComponentSerialization {
        @Reflect(strategy = Reflect.Strategy.LENIENT, isFinal = true)
        public static Codec<?> COMPONENTS;
    }

    @Adapt("net.minecraft.core.registries.BuiltInRegistries")
    public static class WrappedBuiltInRegistries {
        @Reflect(strategy = Reflect.Strategy.STRICT, nameOverride = {"MAP_DECORATION_TYPE", "at"}, isFinal = true)
        public static WrappedIRegistry MAP_DECORATION_TYPES;
    }

    @Adapt("net.minecraft.core.IRegistry")
    public static class WrappedIRegistry extends Mirrored {
        public Object get(int x) {
            return mirror.invoke(this, "c", x);
        }
    }

    @Adapt("net.minecraft.network.protocol.game.PacketPlayOutMap")
    public static class WrappedPacketOutMap extends Mirrored implements Common.WrappedPacket {
        private final WrappedMapId mapId;
        private final byte scale;
        private final boolean locked;
        private final List<WrappedMapIcon> icons;
        private final WrappedMapSlice slice;

        private WrappedPacketOutMap() {
            this(null, (byte) 0, false, null, null);
        }

        public WrappedPacketOutMap(WrappedMapId mapId, byte scale, boolean locked, List<WrappedMapIcon> icons, WrappedMapSlice slice) {
            this.mapId = mapId;
            this.scale = scale;
            this.locked = locked;
            this.icons = icons;
            this.slice = slice;
        }

        @CtorParam
        private Object[] ctor() {
            List<Object> icons = this.icons.stream()
                    .map(mirror::revert)
                    .toList();
            Object[] arr = new Object[] {mapId, scale, locked, icons.isEmpty() ? Optional.empty() : Optional.of(icons), Optional.of(slice)};
            Bukkit.getLogger().info("DEBUG: Map packet ctor param types");
            for (Object o : arr) {
                Bukkit.getLogger().info("DEBUG: " + o.getClass().getName() + " > " + o);
            }
            return arr;
        }
    }

    @Adapt("net.minecraft.world.level.saveddata.maps.MapId")
    public static class WrappedMapId {
        @CtorParam
        private final int id;

        private WrappedMapId() {
            this(0);
        }

        public WrappedMapId(int id) {
            this.id = id;
        }
    }

    @Adapt("net.minecraft.world.level.saveddata.maps.MapIcon")
    public static class WrappedMapIcon {
        private Object decorationType;
        private byte x;
        private byte z;
        private byte direction;
        private Object text;

        private WrappedMapIcon() {
        }

        public WrappedMapIcon(Object decorationType, byte x, byte z, byte direction, Object text) {
            this.decorationType = decorationType;
            this.x = x;
            this.z = z;
            this.direction = direction;
            this.text = text;
        }

        // Holder<MapDecorationType>, byte, byte, byte, Optional<IChatBaseComponent>
        @CtorParam
        Object[] ctor() {
            return new Object[] {decorationType, x, z, direction, text instanceof Optional<?> ? text : Optional.ofNullable(text)};
        }
    }

    @Adapt("net.minecraft.world.level.saveddata.maps.WorldMap$b")
    public static class WrappedMapSlice {

        @CtorParam
        private final int startX;
        @CtorParam
        private final int startY;
        @CtorParam
        private final int width;
        @CtorParam
        private final int height;
        @CtorParam
        private final byte[] mapColors;

        private WrappedMapSlice() {
            this(0, 0, 0, 0, null);
        }

        public WrappedMapSlice(int startX, int startY, int width, int height, byte[] mapColors) {
            this.startX = startX;
            this.startY = startY;
            this.width = width;
            this.height = height;
            this.mapColors = mapColors;
        }
    }
}
