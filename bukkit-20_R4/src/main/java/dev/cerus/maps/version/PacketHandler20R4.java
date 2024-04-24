package dev.cerus.maps.version;

import dev.cerus.maps.api.version.PacketListener;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Field;
import java.util.Arrays;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.PacketPlayInBlockDig;
import net.minecraft.network.protocol.game.PacketPlayInBlockPlace;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.network.protocol.game.PacketPlayInUseItem;
import net.minecraft.world.EnumHand;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R4.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PacketHandler20R4 extends ChannelDuplexHandler {

    private static final Field actionField;
    private static final Object attackAction;

    static {
        try {
            actionField = PacketPlayInUseEntity.class.getDeclaredField("c");
            actionField.setAccessible(true);
            final Field attackActionField = PacketPlayInUseEntity.class.getDeclaredField("e");
            attackActionField.setAccessible(true);
            attackAction = attackActionField.get(null);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final Player player;
    private final PacketListener listener;
    private final JavaPlugin plugin;

    public PacketHandler20R4(final Player player, final PacketListener listener, final JavaPlugin plugin) {
        this.player = player;
        this.listener = listener;
        this.plugin = plugin;
    }

    private static Object getAction(final PacketPlayInUseEntity packet) {
        try {
            return actionField.get(packet);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static EnumHand getHand(final PacketPlayInUseEntity packet) {
        try {
            final Object action = getAction(packet);
            final Field handField = Arrays.stream(action.getClass().getDeclaredFields())
                    .filter(field -> field.getType() == EnumHand.class)
                    .findAny().orElse(null);
            if (handField == null) {
                return null;
            }
            handField.setAccessible(true);
            return (EnumHand) handField.get(action);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if ((((msg instanceof final PacketPlayInUseItem useItem && useItem.b() != EnumHand.b) || msg instanceof PacketPlayInBlockPlace) && this.listener.handlePlayerRightClick(this.player))
            || (msg instanceof final PacketPlayInUseEntity useEntity && getHand(useEntity) != EnumHand.b && (getAction(useEntity) == attackAction ? this.listener.handlePlayerLeftClick(this.player) : this.listener.handlePlayerRightClick(this.player)))
            || (msg instanceof PacketPlayInBlockDig && this.listener.handlePlayerLeftClick(this.player))) {
            if (msg instanceof final PacketPlayInBlockDig dig) {
                // To prevent de-syncs we need to tell the client that the block has not changed
                final Location location = new Location(
                        this.player.getWorld(),
                        dig.b().u(),
                        dig.b().v(),
                        dig.b().w()
                );
                // If we don't acknowledge the client's block change it won't accept further block change packets
                ((CraftPlayer) this.player).getHandle().c.a(new ClientboundBlockChangedAckPacket(dig.g()));
                Bukkit.getScheduler().runTask(this.plugin, () -> this.player.sendBlockChange(location, location.getBlock().getBlockData()));
            }
            if (msg instanceof final PacketPlayInUseItem useItem) {
                // To prevent de-syncs we need to tell the client that the block has not changed
                final MovingObjectPositionBlock pos = useItem.e();
                if (pos != null && pos.a() != null) {
                    final Location location = new Location(
                            this.player.getWorld(),
                            pos.a().u(),
                            pos.a().v(),
                            pos.a().w()
                    ).getBlock().getRelative(CraftBlock.notchToBlockFace(pos.b())).getLocation();
                    // If we don't acknowledge the client's block change it won't accept further block change packets
                    ((CraftPlayer) this.player).getHandle().c.a(new ClientboundBlockChangedAckPacket(useItem.f()));
                    Bukkit.getScheduler().runTask(this.plugin, () -> this.player.sendBlockChange(location, location.getBlock().getBlockData()));
                }
            }
            return;
        }
        super.channelRead(ctx, msg);
    }

}
