package dev.cerus.maps.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EntityUtil {

    public static boolean isLookingAt(final Player player, final Entity entity) {
        final Location eye = player.getEyeLocation();
        final Vector toEntity = entity.getLocation().toVector().subtract(eye.toVector());
        final double dot = toEntity.normalize().dot(eye.getDirection());

        return dot > 0.99D;
    }

    public static ItemFrameResult getNearbyItemFrames(final ItemFrame startingFrame, final BlockFace facing, final int widthCap, final int heightCap) {
        final List<ItemFrame> tempList = new ArrayList<>();

        final Vector widthVector;
        final Vector heightVector = new Vector(0, 1, 0);
        switch (facing) {
            case NORTH:
                widthVector = new Vector(-1, 0, 0);
                break;
            case EAST:
                widthVector = new Vector(0, 0, -1);
                break;
            case SOUTH:
                widthVector = new Vector(1, 0, 0);
                break;
            case WEST:
                widthVector = new Vector(0, 0, 1);
                break;
            default:
                return new ItemFrameResult(new ItemFrame[0][0], 0, 0);
        }
        tempList.add(startingFrame);

        final Location startingLocation = startingFrame.getLocation();
        final World world = startingLocation.getWorld();

        int height = 1;
        while (true) {
            final Location location = startingLocation.clone().add(heightVector.clone().multiply(height));
            final ItemFrame itemFrame = getItemFrameAt(world, location.getBlockX(), location.getBlockY(), location.getBlockZ(), facing);
            if (itemFrame == null || height >= heightCap) {
                break;
            }
            tempList.add(itemFrame);
            height++;
        }

        int width = 1;
        loop:
        while (true) {
            for (int i = 0; i < height; i++) {
                final Location location = startingLocation.clone().add(widthVector.clone().multiply(width))
                        .add(i == 0 ? new Vector(0, 0, 0) : heightVector.clone().multiply(i));
                final ItemFrame itemFrame = getItemFrameAt(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), facing);
                if (itemFrame == null) {
                    break loop;
                }
                tempList.add(itemFrame);
            }
            width++;

            if (width >= widthCap) {
                break;
            }
        }

        final ItemFrame[][] resultArr = new ItemFrame[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                resultArr[i][j] = tempList.remove(0);
            }
        }

        /*list.sort(Comparator.comparingInt(value -> ((Entity) value).getLocation().getBlockX()).reversed()
                .thenComparing(Comparator.comparingInt(value -> ((Entity) value).getLocation().getBlockY()).reversed()));*/
        return new ItemFrameResult(resultArr, width, height);
    }

    private static ItemFrame getItemFrameAt(final World world, final int x, final int y, final int z, final BlockFace facing) {
        final Optional<Entity> optional = world.getNearbyEntities(new Location(world, x, y, z), 1, 1, 1, entity ->
                        entity instanceof ItemFrame && entity.getLocation().getBlockX() == x && entity.getLocation().getBlockY() == y
                                && entity.getLocation().getBlockZ() == z && entity.getFacing() == facing).stream()
                .filter(entity -> {
                    final Location location = entity.getLocation();
                    return location.getBlockX() == x && location.getBlockY() == y && location.getBlockZ() == z;
                })
                .findAny();
        return (ItemFrame) optional.orElse(null);
    }

    public static class ItemFrameResult {

        private final ItemFrame[][] frames;
        private final int width;
        private final int height;

        public ItemFrameResult(final ItemFrame[][] frames, final int width, final int height) {
            this.frames = frames;
            this.width = width;
            this.height = height;
        }

        public ItemFrame[][] getFrames() {
            return this.frames;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

    }

}
