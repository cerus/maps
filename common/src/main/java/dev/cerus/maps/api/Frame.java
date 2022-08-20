package dev.cerus.maps.api;

import org.bukkit.World;
import org.bukkit.block.BlockFace;

public class Frame {

    private final World world;
    private final int posX;
    private final int posY;
    private final int posZ;
    private final BlockFace facing;
    private final int entityId;
    private final boolean visible;

    public Frame(final World world,
                 final int posX,
                 final int posY,
                 final int posZ,
                 final BlockFace facing,
                 final int entityId,
                 final boolean visible) {
        this.world = world;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.facing = facing;
        this.entityId = entityId;
        this.visible = visible;
    }

    public World getWorld() {
        return this.world;
    }

    public int getPosX() {
        return this.posX;
    }

    public int getPosY() {
        return this.posY;
    }

    public int getPosZ() {
        return this.posZ;
    }

    public BlockFace getFacing() {
        return this.facing;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public boolean isVisible() {
        return this.visible;
    }

}
