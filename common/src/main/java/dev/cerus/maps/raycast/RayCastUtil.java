package dev.cerus.maps.raycast;

import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.util.Vec2;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Utility for getting the screen a player is looking at
 */
public class RayCastUtil {

    private static final RayCaster RAY_CASTER = new RayCaster();

    private RayCastUtil() {
    }

    /**
     * Get the screen and position a player is looking at
     *
     * @param player            The player
     * @param maxDistance       The maximum distance that we should check
     * @param screen            Possible screens to check
     * @param additionalScreens Possible screens to check
     *
     * @return An optional that either contains the result or contains nothing
     */
    public static Optional<Result> getTargetedScreen(final Player player, final double maxDistance, final MapScreen screen, final MapScreen... additionalScreens) {
        // This method is built in a way that requires users to specify at least one screen.
        // The reason for that is simple: It's very easy to forget to specify any screens - It happened to
        // me and finding the cause for the issue took way longer than I would like to admit.

        final Set<MapScreen> screens = new HashSet<>(Arrays.asList(additionalScreens));
        screens.add(screen);
        return getTargetedScreen(player, maxDistance, screens);
    }

    /**
     * Get the screen and position a player is looking at
     *
     * @param player      The player
     * @param maxDistance The maximum distance that we should check
     * @param screens     Possible screens to check
     *
     * @return An optional that either contains the result or contains nothing
     */
    public static Optional<Result> getTargetedScreen(final Player player, final double maxDistance, final MapScreen[] screens) {
        return getTargetedScreen(player, maxDistance, Arrays.asList(screens));
    }

    /**
     * Get the screen and position a player is looking at
     *
     * @param player      The player
     * @param maxDistance The maximum distance that we should check
     * @param screens     Possible screens to check
     *
     * @return An optional that either contains the result or contains nothing
     */
    public static Optional<Result> getTargetedScreen(final Player player, final double maxDistance, final Iterable<MapScreen> screens) {
        final List<MapScreen> screensWithHitBox = StreamSupport.stream(screens.spliterator(), false)
                .filter(mapScreen -> mapScreen.getHitBox() != null)
                .toList();
        final RayCaster.Result<MapScreen> result = RAY_CASTER.cast(player.getEyeLocation(), player.getEyeLocation().getDirection(), maxDistance, 0.01, loc -> {
            for (final MapScreen screen : screensWithHitBox) {
                if (screen.getHitBox().contains(loc)) {
                    return screen;
                }
            }
            return null;
        }, false);

        final MapScreen target = result.hitObstruction();
        if (target == null) {
            return Optional.empty();
        }

        final Location relPos = target.getHitBox().bottomLeft().clone().subtract(result.stoppedAt().clone());
        final Vec2 screenPos = switch (target.getFrames()[0][0].getFacing()) {
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
        return Optional.of(new Result(target, screenPos.x, screenPos.y));
    }

    /**
     * The result of a map screen ray cast operation
     *
     * @param targetScreen The screen that the ray hit
     * @param screenX      The screen x position that was hit
     * @param screenY      The screen y position that was hit
     */
    public record Result(MapScreen targetScreen, int screenX, int screenY) {}

}
