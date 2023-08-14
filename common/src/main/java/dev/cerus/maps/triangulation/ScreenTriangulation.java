package dev.cerus.maps.triangulation;

import dev.cerus.maps.api.Frame;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.util.MathUtil;
import dev.cerus.maps.util.Vec2;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * Utility class for triangulating screen positions based on locations
 */
public final class ScreenTriangulation {

    private static final float ITEM_FRAME_THICKNESS = 0.0625f;

    private ScreenTriangulation() {
    }

    /**
     * Calculate the screen coordinates of a MapScreen that an entity is looking at
     * <p>
     * Will return null if the coordinates can not be calculated
     *
     * @param entity The entity to use for the calculations
     * @param screen The screen to calculate the coordinates for
     *
     * @return a Vec2 containing the calculated coordinates or null
     *
     * @throws IllegalArgumentException if entity or screen is null
     */
    public static Vec2 triangulateScreenCoords(final Entity entity, final MapScreen screen) {
        if (entity == null || screen == null) {
            throw new IllegalArgumentException("entity / screen can not be null");
        }

        final Location loc = entity instanceof final LivingEntity le ? le.getEyeLocation() : entity.getLocation();
        return triangulateScreenCoords(loc, screen);
    }

    /**
     * Calculate the screen coordinates of a MapScreen that a location is looking at
     * <p>
     * Will return null if the coordinates can not be calculated
     *
     * @param location The location to use for the calculations
     * @param screen   The screen to calculate the coordinates for
     *
     * @return a Vec2 containing the calculated coordinates or null
     *
     * @throws IllegalArgumentException if location or screen is null
     */
    public static Vec2 triangulateScreenCoords(final Location location, final MapScreen screen) {
        if (location == null || screen == null) {
            throw new IllegalArgumentException("location / screen can not be null");
        }

        final Location screenLoc = screen.getLocation();
        final Frame referenceFrame = screen.getFrames()[0][0];
        final BlockFace orientation = referenceFrame.getFacing();
        final float playerYaw = location.getYaw();
        final float playerPitch = location.getPitch();

        // Prevent useless calculations if the player is not looking at the screen
        if (!isInFrontOfScreen(orientation, location, screenLoc)
            || !isFacingScreen(orientation, playerYaw)) {
            return null;
        }

        // Distance from screen to player
        final float frameThickness = referenceFrame.isVisible() ? ITEM_FRAME_THICKNESS : 0f;
        final double dist = distance(location, screen) - frameThickness;

        // First triangle: Yaw (vertical)
        // Map player yaw to degrees
        final double yawAlpha = yawDeg(orientation, playerYaw);
        final double yawAlphaMod = yawDegMod(orientation, playerYaw);
        // Calculate the hypotenuse using side b and angle alpha (yaw)
        final double hyp = MathUtil.triHypotenuseB(dist, yawAlpha);
        // Calculate side a using the hypotenuse and side b (dist)
        final double ya = MathUtil.fastSqrt((hyp * hyp) - (dist * dist));
        // Negate side a if required
        final double yaMod = ya * yawAlphaMod;

        // Second triangle: Pitch (horizontal)
        // Calculate side a using side b (hypotenuse from 1st triangle) and angle alpha (pitch)
        final double pa = MathUtil.triSideA(hyp, pitchDeg(playerPitch));
        // Negate side a if required
        final double paMod = pa * pitchDegMod(playerPitch);

        // Calculate the resulting location
        final Location resultLoc = location.clone();
        resultLoc.setY(resultLoc.getY() + paMod);
        switch (orientation) {
            case NORTH -> {
                resultLoc.setX(resultLoc.getX() - yaMod);
                resultLoc.setZ(screenLoc.getZ());
            }
            case EAST -> {
                resultLoc.setZ(resultLoc.getZ() - yaMod);
                resultLoc.setX(screenLoc.getX());
            }
            case SOUTH -> {
                resultLoc.setX(resultLoc.getX() + yaMod);
                resultLoc.setZ(screenLoc.getZ());
            }
            case WEST -> {
                resultLoc.setZ(resultLoc.getZ() + yaMod);
                resultLoc.setX(screenLoc.getX());
            }
        }

        // Return if the result is outside the screens hit-box
        if (!screen.getHitBox().contains(resultLoc)) {
            return null;
        }

        // Calculate the screen coordinates using the resulting location
        final Location relPos = screen.getHitBox().bottomLeft().clone().subtract(resultLoc);
        return switch (orientation) {
            case NORTH -> {
                final int x = (int) (relPos.getX() * 128d);
                final int y = (int) ((-relPos.getY()) * 128d);
                yield new Vec2(x, y);
            }
            case EAST -> {
                final int x = (int) (relPos.getZ() * 128d);
                final int y = (int) ((-relPos.getY()) * 128d);
                yield new Vec2(x, y);
            }
            case SOUTH -> {
                final int x = (int) ((-relPos.getX()) * 128d);
                final int y = (int) ((-relPos.getY()) * 128d);
                yield new Vec2(x, y);
            }
            case WEST -> {
                final int x = (int) ((-relPos.getZ()) * 128d);
                final int y = (int) ((-relPos.getY()) * 128d);
                yield new Vec2(x, y);
            }
            default -> new Vec2(0, 0);
        };
    }

    /**
     * Calculate the distance from an entity to a MapScreen on either the X or the Z axis (depending on the direction the screen is facing)
     * <p>
     * This will not calculate the Euclidean distance, use {@link Location#distance(Location)} for that.
     *
     * @param entity The entity to calculate the distance for
     * @param screen The screen to calculate the distance to
     *
     * @return The distance on the Z (north & south) or X (east & west) axis
     */
    public static double distance(final Entity entity, final MapScreen screen) {
        if (entity == null || screen == null) {
            throw new IllegalArgumentException("entity / screen can not be null");
        }
        return distance(entity.getLocation(), screen);
    }

    /**
     * Calculate the distance from a location to a MapScreen on either the X or the Z axis (depending on the direction the screen is facing)
     * <p>
     * This will not calculate the Euclidean distance, use {@link Location#distance(Location)} for that.
     *
     * @param loc    The location to calculate the distance for
     * @param screen The screen to calculate the distance to
     *
     * @return The distance on the Z (north & south) or X (east & west) axis
     */
    public static double distance(final Location loc, final MapScreen screen) {
        if (loc == null || screen == null) {
            throw new IllegalArgumentException("loc / screen can not be null");
        }

        final Location screenLoc = screen.getLocation();
        final BlockFace orientation = screen.getFrames()[0][0].getFacing();
        return (orientation == BlockFace.NORTH || orientation == BlockFace.SOUTH)
                ? Math.max(loc.getZ(), screenLoc.getZ()) - Math.min(loc.getZ(), screenLoc.getZ())
                : Math.max(loc.getX(), screenLoc.getX()) - Math.min(loc.getX(), screenLoc.getX());
    }

    /**
     * Pitch to degrees
     *
     * @param pitch The pitch value to convert
     *
     * @return the pitch in degrees
     */
    private static double pitchDeg(final float pitch) {
        return Math.abs(pitch);
    }

    /**
     * Modifier for pitch degrees (-1 if looking down, 1 if looking up)
     *
     * @param pitch The pitch value to use
     *
     * @return -1 or 1
     */
    private static double pitchDegMod(final float pitch) {
        return pitch >= 0 ? -1f : 1f;
    }

    /**
     * Yaw to degrees
     *
     * @param orientation The orientation of the relevant MapScreen
     * @param yaw         The yaw value to convert
     *
     * @return the yaw in degrees
     */
    private static double yawDeg(final BlockFace orientation, final float yaw) {
        return switch (orientation) {
            case NORTH -> Math.abs(yaw);
            case EAST -> yaw < 90f ? 90f - yaw : yaw - 90f;
            case SOUTH -> 90 - (Math.abs(yaw) - 90);
            case WEST -> yaw < -90f ? Math.abs(yaw) - 90f : 90f - Math.abs(yaw);
            default -> 0f;
        };
    }

    /**
     * Modifier for yaw degrees (-1 if looking left, 1 if looking right)
     *
     * @param orientation The orientation of the relevant MapScreen
     * @param yaw         The yaw value to use
     *
     * @return -1 or 1
     */
    private static double yawDegMod(final BlockFace orientation, final float yaw) {
        return switch (orientation) {
            case NORTH -> yaw < 0 ? -1f : 1f;
            case EAST -> yaw < 90 ? -1f : 1f;
            case SOUTH -> yaw > 0 ? -1f : 1f;
            case WEST -> yaw < -90 ? -1f : 1f;
            default -> 0f;
        };
    }

    /**
     * Check if a location is in front of a MapScreen
     *
     * @param orientation The orientation of the relevant MapScreen
     * @param loc         The location to check
     * @param screenLoc   The location of the relevant MapScreen
     *
     * @return true if the location is in front of the MapScreen
     */
    private static boolean isInFrontOfScreen(final BlockFace orientation, final Location loc, final Location screenLoc) {
        return switch (orientation) {
            case NORTH -> loc.getZ() < screenLoc.getZ();
            case EAST -> loc.getX() > screenLoc.getX();
            case SOUTH -> loc.getZ() > screenLoc.getZ();
            case WEST -> loc.getX() < screenLoc.getX();
            default -> false;
        };
    }

    /**
     * Check if a yaw value is facing a MapScreen
     *
     * @param orientation The orientation of the relevant MapScreen
     * @param yaw         The yaw value to check
     *
     * @return true if the yaw value is facing the MapScreen
     */
    private static boolean isFacingScreen(final BlockFace orientation, final float yaw) {
        return switch (orientation) {
            case NORTH -> Math.abs(yaw) < 90;
            case EAST -> yaw > 0 && yaw < 180;
            case SOUTH -> yaw < -90 || yaw > 90;
            case WEST -> yaw < 0;
            default -> false;
        };
    }

}
