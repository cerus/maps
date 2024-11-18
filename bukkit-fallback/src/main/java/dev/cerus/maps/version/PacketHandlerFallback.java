package dev.cerus.maps.version;

import dev.cerus.maps.api.version.PacketListener;
import dev.cerus.mirror.Mirror;
import dev.cerus.mirror.Mirrored;
import dev.cerus.mirror.annotation.Adapt;
import dev.cerus.mirror.annotation.Assign;
import dev.cerus.mirror.annotation.CtorParam;
import dev.cerus.mirror.annotation.Depend;
import dev.cerus.mirror.annotation.EnumConstant;
import dev.cerus.mirror.annotation.Handle;
import dev.cerus.mirror.annotation.MirrorInstance;
import dev.cerus.mirror.annotation.Reflect;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Modifier;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PacketHandlerFallback extends ChannelDuplexHandler {

    private final Player player;
    private final PacketListener listener;
    private final JavaPlugin plugin;
    private final Mirror mirror;

    public PacketHandlerFallback(final Player player, final PacketListener listener, final JavaPlugin plugin, Mirror mirror) {
        this.player = player;
        this.listener = listener;
        this.plugin = plugin;
        this.mirror = mirror;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (mirror.instanceOf(msg, WrappedPacketInBlockPlace.class, packet -> listener.handlePlayerRightClick(player))) {
            return;
        }
        if (mirror.instanceOf(msg, WrappedPacketInUseEntity.class, packet -> {
           if (packet.getHand() == WrappedEnumHand.SECONDARY) {
               return false;
           }
           return packet.action instanceof WrappedEntityUseActionAttack ? listener.handlePlayerLeftClick(player) : listener.handlePlayerRightClick(player);
        })) {
            return;
        }
        if (mirror.instanceOf(msg, WrappedPacketInUseItem.class, packet -> {
            if (packet.hand == WrappedEnumHand.SECONDARY) {
                return false;
            }
            if (!listener.handlePlayerRightClick(player)) {
                return false;
            }
            acknowledgeAndRevert(packet.mop, packet.sequenceId);
            return true;
        })) {
            return;
        }
        if (mirror.instanceOf(msg, WrappedPacketInBlockDig.class, packet -> {
            if (!listener.handlePlayerLeftClick(player)) {
                return false;
            }
            acknowledgeAndRevert(packet.blockPosition, null, packet.sequenceId);
            return true;
        })) {
            return;
        }
        super.channelRead(ctx, msg);
    }

    private void acknowledgeAndRevert(WrappedMovingObjectPositionBlock mop, int sequenceId) {
        if (mop == null || mop.blockPosition == null) {
            return;
        }
        acknowledgeAndRevert(mop.blockPosition, mop.direction, sequenceId);
    }

    private void acknowledgeAndRevert(WrappedBlockPosition blockPos, WrappedEnumDirection dir, int sequenceId) {
        if (blockPos == null) {
            return;
        }
        Location location = new Location(
                this.player.getWorld(),
                blockPos.getX(),
                blockPos.getY(),
                blockPos.getZ()
        );
        if (dir != null) {
            location = location.getBlock().getRelative(dir.blockFace).getLocation();
        }
        // If we don't acknowledge the client's block change it won't accept further block change packets
        Common.WrappedCraftPlayer wcp = (Common.WrappedCraftPlayer) mirror.adapt(player);
        wcp.sendPacket(new WrappedPacketOutBlockChangedAckPacket(sequenceId));

        //((CraftPlayer) this.player).getHandle().c.a(new ClientboundBlockChangedAckPacket(useItem.f()));
        //Bukkit.getScheduler().runTask(this.plugin, () -> this.player.sendBlockChange(location, location.getBlock().getBlockData()));
    }

    @Adapt("net.minecraft.world.EnumHand")
    public enum WrappedEnumHand {
        @EnumConstant(ordinal = 0)
        PRIMARY,
        @EnumConstant(ordinal = 1)
        SECONDARY
    }

    @Adapt("net.minecraft.core.EnumDirection")
    public enum WrappedEnumDirection {
        @EnumConstant(ordinal = 0)
        DOWN(BlockFace.DOWN),
        @EnumConstant(ordinal = 1)
        UP(BlockFace.UP),
        @EnumConstant(ordinal = 2)
        NORTH(BlockFace.NORTH),
        @EnumConstant(ordinal = 3)
        SOUTH(BlockFace.SOUTH),
        @EnumConstant(ordinal = 4)
        WEST(BlockFace.WEST),
        @EnumConstant(ordinal = 5)
        EAST(BlockFace.EAST);

        private final BlockFace blockFace;

        WrappedEnumDirection(BlockFace blockFace) {
            this.blockFace = blockFace;
        }

        public BlockFace getBlockFace() {
            return blockFace;
        }
    }

    @Adapt("net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket")
    public static class WrappedPacketOutBlockChangedAckPacket implements Common.WrappedPacket {
        @Reflect(strategy = Reflect.Strategy.LENIENT, isFinal = true)
        @CtorParam
        private int sequenceId;

        public WrappedPacketOutBlockChangedAckPacket() {
        }

        public WrappedPacketOutBlockChangedAckPacket(int sequenceId) {
            this.sequenceId = sequenceId;
        }
    }

    @Adapt("net.minecraft.network.protocol.game.PacketPlayInBlockPlace")
    public static class WrappedPacketInBlockPlace {
    }

    @Adapt("net.minecraft.network.protocol.game.PacketPlayInBlockDig")
    public static class WrappedPacketInBlockDig {
        @Reflect(strategy = Reflect.Strategy.LENIENT, isFinal = true)
        private WrappedBlockPosition blockPosition;
        @Reflect(strategy = Reflect.Strategy.LENIENT, isFinal = true)
        private int sequenceId;
    }

    @Adapt("net.minecraft.core.BlockPosition")
    public static class WrappedBlockPosition {
        @Reflect
        private int a;
        @Reflect
        private int b;
        @Reflect
        private int c;

        public int getX() {
            return a;
        }

        public int getY() {
            return b;
        }

        public int getZ() {
            return c;
        }
    }

    @Adapt("net.minecraft.world.phys.MovingObjectPositionBlock")
    public static class WrappedMovingObjectPositionBlock {
        @Reflect(strategy = Reflect.Strategy.LENIENT, isFinal = true)
        private WrappedBlockPosition blockPosition;
        @Reflect(strategy = Reflect.Strategy.LENIENT, isFinal = true)
        private WrappedEnumDirection direction;
    }

    @Adapt("net.minecraft.network.protocol.game.PacketPlayInUseItem")
    public static class WrappedPacketInUseItem {
        @Reflect(strategy = Reflect.Strategy.LENIENT, isFinal = true)
        private WrappedMovingObjectPositionBlock mop;
        @Reflect(strategy = Reflect.Strategy.LENIENT, isFinal = true)
        private WrappedEnumHand hand;
        @Reflect(strategy = Reflect.Strategy.LENIENT, isFinal = true)
        private int sequenceId;
    }

    @Adapt("net.minecraft.network.protocol.game.PacketPlayInUseEntity")
    public static class WrappedPacketInUseEntity {
        @Reflect(strategy = Reflect.Strategy.LENIENT, isFinal = true)
        static WrappedEntityUseAction ATTACK;
        @Reflect(strategy = Reflect.Strategy.LENIENT, isFinal = true)
        private WrappedEntityUseAction action;

        public WrappedEnumHand getHand() {
            return action.getHand();
        }
    }

    @Adapt("net.minecraft.network.protocol.game.PacketPlayInUseEntity$EnumEntityUseAction")
    @Depend({WrappedEntityUseActionAttack.class, WrappedEntityUseActionInteract.class, WrappedEntityUseActionInteractAt.class})
    public interface WrappedEntityUseAction {
        WrappedEnumHand getHand();
    }

    @Adapt("net.minecraft.network.protocol.game.PacketPlayInUseEntity$1")
    public static class WrappedEntityUseActionAttack implements WrappedEntityUseAction {
        @Override
        public WrappedEnumHand getHand() {
            return null;
        }
    }

    @Adapt("net.minecraft.network.protocol.game.PacketPlayInUseEntity$d")
    public static class WrappedEntityUseActionInteract implements WrappedEntityUseAction {
        @Reflect(strategy = Reflect.Strategy.LENIENT, isFinal = true)
        private WrappedEnumHand hand;

        @Override
        public WrappedEnumHand getHand() {
            return hand;
        }
    }

    @Adapt("net.minecraft.network.protocol.game.PacketPlayInUseEntity$e")
    public static class WrappedEntityUseActionInteractAt implements WrappedEntityUseAction {
        @Reflect(strategy = Reflect.Strategy.LENIENT, isFinal = true)
        private WrappedEnumHand hand;

        @Override
        public WrappedEnumHand getHand() {
            return hand;
        }
    }
}
