package dev.cerus.maps.raycast;

import dev.cerus.maps.api.MapScreen;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RayCastUtil {

    private static final RayCaster RAY_CASTER = new RayCaster();

    private RayCastUtil() {
    }

    public static Optional<Result> getTargetedScreen(final Player player, final double maxDistance, final MapScreen screen, final MapScreen... additionalScreens) {
        final List<MapScreen> screens = new ArrayList<>(Arrays.asList(additionalScreens));
        screens.add(screen);
        return getTargetedScreen(player, maxDistance, screens);
    }

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
        final Map.Entry<Integer, Integer> screenPos = switch (target.getFrames()[0][0].getFacing()) {
            case NORTH -> {
                final int x = (int) (relPos.getX() * 128d);
                final int y = (int) ((-relPos.getY()) * 128d);
                yield Map.entry(x, y);
            }
            case EAST -> {
                final int x = (int) (relPos.getZ() * 128d);
                final int y = (int) ((-relPos.getY()) * 128d);
                yield Map.entry(x, y);
            }
            case SOUTH -> {
                final int x = (int) ((-relPos.getX()) * 128d);
                final int y = (int) ((-relPos.getY()) * 128d);
                yield Map.entry(x, y);
            }
            case WEST -> {
                final int x = (int) ((-relPos.getZ()) * 128d);
                final int y = (int) ((-relPos.getY()) * 128d);
                yield Map.entry(x, y);
            }
            default -> Map.entry(0, 0);
        };
        return Optional.of(new Result(target, screenPos.getKey(), screenPos.getValue()));
    }

    public record Result(MapScreen targetScreen, int screenX, int screenY) {
    }

}
