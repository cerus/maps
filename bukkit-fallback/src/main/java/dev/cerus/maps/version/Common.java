package dev.cerus.maps.version;

import dev.cerus.mirror.Mirrored;
import dev.cerus.mirror.annotation.Adapt;
import dev.cerus.mirror.annotation.Assign;
import dev.cerus.mirror.annotation.Reflect;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import java.lang.reflect.Modifier;
import org.bukkit.Bukkit;

public class Common {

    @Adapt("org.bukkit.craftbukkit.{{CB}}.entity.CraftPlayer")
    public static class WrappedCraftPlayer extends Mirrored {
        @Assign(method = "getHandle")
        private WrappedEntityPlayer handle;

        public void sendPacket(WrappedPacket packet) {
            handle.playerConnection.sendPacket(packet);
        }

        public ChannelPipeline getPipeline() {
            return handle.playerConnection.networkManager.channel.pipeline();
        }

        public WrappedEntityPlayer getHandle() {
            return handle;
        }
    }

    @Adapt("net.minecraft.server.level.EntityPlayer")
    public static class WrappedEntityPlayer {
        @Reflect(strategy = Reflect.Strategy.TYPE_ONLY)
        private WrappedPlayerConnection playerConnection;

        public WrappedPlayerConnection getPlayerConnection() {
            return playerConnection;
        }
    }

    @Adapt("net.minecraft.server.network.PlayerConnection")
    public static class WrappedPlayerConnection extends Mirrored {
        @Reflect(strategy = Reflect.Strategy.TYPE_ONLY)
        private WrappedNetworkManager networkManager;

        public void sendPacket(WrappedPacket packet) {
            Object nmsPacket = mirror.revert(packet);
            Bukkit.getLogger().info("DEBUG: unwrapped " + packet.getClass().getSimpleName() + " -> " + nmsPacket.getClass().getName());
            mirror.invoke(this, "sendPacket", nmsPacket);
        }
    }

    @Adapt("net.minecraft.network.NetworkManager")
    public static class WrappedNetworkManager {
        @Reflect(strategy = Reflect.Strategy.LENIENT)
        public Channel channel;
    }

    @Adapt("net.minecraft.network.protocol.Packet")
    public interface WrappedPacket {
    }
}
