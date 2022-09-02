package dev.cerus.maps.util;

import dev.cerus.maps.api.Frame;
import dev.cerus.maps.api.MapScreen;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Calculates hit boxes for map screens
 */
public class HitBoxCalculatorUtil {

    // Thickness of the hit box
    private static final float HIT_BOX_THICKNESS = 0.1f;
    // Additional thickness if the screen has visible frames
    private static final float FRAME_THICKNESS = 0.0625f;

    private HitBoxCalculatorUtil() {
    }

    /**
     * Calculate the hit box of a map screen
     *
     * @param screen The screen to calculate the hit box for
     *
     * @return The calculated hit box or null if the calculation failed
     */
    public static MapScreen.HitBox calculateHitBox(final MapScreen screen) {
        if (screen.getFrames() == null || screen.getFrames().length == 0 || screen.getFrames()[0].length == 0) {
            return null;
        }

        final List<Frame> frameList = Arrays.stream(screen.getFrames()).flatMap(Arrays::stream).toList();
        Location bottomLeft = null;
        Location topRight = null;
        Vector thicknessVec = null;
        Vector frameThicknessVec = new Vector(0, 0, 0);
        for (final Frame frame : frameList) {
            switch (frame.getFacing()) {
                case NORTH -> {
                    // X -
                    if (bottomLeft == null || (frame.getPosX() > bottomLeft.getX() || frame.getPosY() < bottomLeft.getY())) {
                        bottomLeft = new Location(frame.getWorld(), frame.getPosX() + 0.99d, frame.getPosY(), (frame.getPosZ() + 1) - 0.05);
                    }
                    if (topRight == null || (frame.getPosX() < topRight.getX() || frame.getPosY() > topRight.getY())) {
                        topRight = new Location(frame.getWorld(), frame.getPosX(), frame.getPosY() + 0.99, (frame.getPosZ() + 1) - 0.05);
                    }
                    thicknessVec = new Vector(0, 0, HIT_BOX_THICKNESS);
                    if (frame.isVisible()) {
                        frameThicknessVec = new Vector(0, 0, -FRAME_THICKNESS);
                    }
                }
                case EAST -> {
                    // Z -
                    if (bottomLeft == null || (frame.getPosZ() > bottomLeft.getZ() || frame.getPosY() < bottomLeft.getY())) {
                        bottomLeft = new Location(frame.getWorld(), frame.getPosX(), frame.getPosY(), frame.getPosZ() + 0.99);
                    }
                    if (topRight == null || (frame.getPosZ() < topRight.getZ() || frame.getPosY() > topRight.getY())) {
                        topRight = new Location(frame.getWorld(), frame.getPosX(), frame.getPosY() + 0.99, frame.getPosZ());
                    }
                    thicknessVec = new Vector(-HIT_BOX_THICKNESS, 0, 0);
                    if (frame.isVisible()) {
                        frameThicknessVec = new Vector(FRAME_THICKNESS, 0, 0);
                    }
                }
                case SOUTH -> {
                    // X +
                    if (bottomLeft == null || (frame.getPosX() < bottomLeft.getX() || frame.getPosY() < bottomLeft.getY())) {
                        bottomLeft = new Location(frame.getWorld(), frame.getPosX(), frame.getPosY(), frame.getPosZ());
                    }
                    if (topRight == null || (frame.getPosX() > topRight.getX() || frame.getPosY() > topRight.getY())) {
                        topRight = new Location(frame.getWorld(), frame.getPosX() + 0.99, frame.getPosY() + 0.99, frame.getPosZ());
                    }
                    thicknessVec = new Vector(0, 0, -HIT_BOX_THICKNESS);
                    if (frame.isVisible()) {
                        frameThicknessVec = new Vector(0, 0, FRAME_THICKNESS);
                    }
                }
                case WEST -> {
                    // Z +
                    if (bottomLeft == null || (frame.getPosZ() < bottomLeft.getZ() || frame.getPosY() < bottomLeft.getY())) {
                        bottomLeft = new Location(frame.getWorld(), frame.getPosX() + 0.99d, frame.getPosY(), frame.getPosZ());
                    }
                    if (topRight == null || (frame.getPosZ() > topRight.getZ() || frame.getPosY() > topRight.getY())) {
                        topRight = new Location(frame.getWorld(), frame.getPosX() + 0.99, frame.getPosY() + 0.99, frame.getPosZ() + 0.99);
                    }
                    thicknessVec = new Vector(HIT_BOX_THICKNESS, 0, 0);
                    if (frame.isVisible()) {
                        frameThicknessVec = new Vector(-FRAME_THICKNESS, 0, 0);
                    }
                }
            }
        }

        if (thicknessVec == null) {
            return null;
        }

        return new MapScreen.HitBox(
                // Add the frame thickness vector to account for visible item frames
                bottomLeft.add(frameThicknessVec),
                // Add the thickness vector to give the hit box a bit of thickness
                topRight.add(thicknessVec)
        );
    }

}
