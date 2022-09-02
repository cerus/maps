package dev.cerus.maps.version;

import dev.cerus.maps.api.version.PacketListener;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_16_R3.EnumHand;
import net.minecraft.server.v1_16_R3.MovingObjectPositionBlock;
import net.minecraft.server.v1_16_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_16_R3.PacketPlayInBlockPlace;
import net.minecraft.server.v1_16_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_16_R3.PacketPlayInUseItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PacketHandler16R3 extends ChannelDuplexHandler {

    private final Player player;
    private final PacketListener listener;
    private final JavaPlugin plugin;

    public PacketHandler16R3(final Player player, final PacketListener listener, final JavaPlugin plugin) {
        this.player = player;
        this.listener = listener;
        this.plugin = plugin;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if ((((msg instanceof PacketPlayInUseItem useItem && useItem.b() != EnumHand.OFF_HAND) || msg instanceof PacketPlayInBlockPlace)
                && this.listener.handlePlayerRightClick(this.player)) || (msg instanceof PacketPlayInUseEntity useEntity && useEntity.d() == null
                && (useEntity.b() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK ? this.listener.handlePlayerLeftClick(this.player)
                : this.listener.handlePlayerRightClick(this.player))) || (msg instanceof PacketPlayInBlockDig
                && this.listener.handlePlayerLeftClick(this.player))) {
            if (msg instanceof PacketPlayInBlockDig dig) {
                // To prevent de-syncs we need to tell the client that the block has not changed
                final Location location = new Location(
                        this.player.getWorld(),
                        dig.b().getX(),
                        dig.b().getY(),
                        dig.b().getZ()
                );
                Bukkit.getScheduler().runTask(this.plugin, () -> this.player.sendBlockChange(location, location.getBlock().getBlockData()));
            }
            if (msg instanceof PacketPlayInUseItem useItem) {
                // To prevent de-syncs we need to tell the client that the block has not changed
                final MovingObjectPositionBlock pos = useItem.c();
                if (pos != null && pos.getBlockPosition() != null) {
                    final Location location = new Location(
                            this.player.getWorld(),
                            pos.getBlockPosition().getX(),
                            pos.getBlockPosition().getY(),
                            pos.getBlockPosition().getZ()
                    ).getBlock().getRelative(CraftBlock.notchToBlockFace(pos.getDirection())).getLocation();
                    Bukkit.getScheduler().runTask(this.plugin, () -> this.player.sendBlockChange(location, location.getBlock().getBlockData()));
                }
            }
            return;
        }
        super.channelRead(ctx, msg);
    }

}
