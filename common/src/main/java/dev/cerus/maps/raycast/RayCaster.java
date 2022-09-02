package dev.cerus.maps.raycast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Utility for casting rays
 * <p>
 * Used to detect objects in the Minecraft world
 */
public class RayCaster {

    /**
     * Cast a ray
     *
     * @param pos             The starting position of the ray cast
     * @param direction       The direction that the ray should head in
     * @param length          The length that the ray should travel
     * @param multiplier      The size of each step
     * @param obstructionFunc Function for detecting obstructions
     * @param collectPoints   Whether we should keep track of each point we have travelled or not
     * @param <T>             The type of obstruction that we could encounter
     *
     * @return The result of this ray cast operation
     */
    public <T> Result<T> cast(final Location pos,
                              final Vector direction,
                              final double length,
                              final double multiplier,
                              final Function<Location, T> obstructionFunc,
                              final boolean collectPoints) {
        final List<Location> points = new ArrayList<>();
        Location posCopy = pos.clone();
        final Vector dirCopy = direction.clone().normalize().multiply(multiplier);
        double dist = 0D;

        while (dist < length * length) {
            // Travel to the next point
            posCopy = posCopy.add(dirCopy);
            if (collectPoints) {
                points.add(posCopy.clone());
            }
            dist = pos.distanceSquared(posCopy);

            // Check for obstructions
            final T obstruction = obstructionFunc.apply(posCopy);
            if (obstruction != null) {
                // Obstruction found, early return
                return new Result<>(
                        dist,
                        length,
                        points,
                        posCopy,
                        obstruction,
                        true
                );
            }
        }

        // No obstruction found
        return new Result<>(
                dist,
                length,
                points,
                posCopy,
                null,
                false
        );
    }

    /**
     * Result of a ray cast operation
     *
     * @param lengthTravelled The total length that the ray has travelled
     * @param length          The length that the ray was supposed to travel
     * @param points          The individual points of the ray cast
     * @param stoppedAt       The point where the ray has stopped at
     * @param hitObstruction  The obstruction that the ray has hit
     * @param forceStop       Whether the ray was forced to stop by an obstruction or not
     * @param <O>             The type of the obstruction
     */
    public record Result<O>(
            double lengthTravelled,
            double length,
            List<Location> points,
            Location stoppedAt,
            O hitObstruction,
            boolean forceStop
    ) {}

}
