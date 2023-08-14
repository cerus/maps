package dev.cerus.maps.triangulation;

import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.util.MathUtil;
import dev.cerus.maps.util.Vec2;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class ScreenTriangulator {

    public Vec2 getScreenPos(final Player player, final MapScreen screen) {
        return this.getScreenPos(player, screen, false);
    }

    public Vec2 getScreenPos(final Player player, final MapScreen screen, final boolean v) {
        final Location playerLoc = player.getLocation();
        final Location screenLoc = screen.getLocation();
        final BlockFace orientation = screen.getFrames()[0][0].getFacing();
        final float playerYaw = playerLoc.getYaw();
        final float playerPitch = playerLoc.getPitch();

        if (!this.isInFrontOfScreen(orientation, playerLoc, screenLoc)
            || !this.isFacingScreen(orientation, playerYaw)) {
            if (v) {
                player.sendMessage("not in front / not facing");
            }
            return null;
        }

        final float frameThickness = screen.getFrames()[0][0].isVisible() ? 0.0625f : 0f;
        final double dist = this.distance(player, screen) - frameThickness;

        final double yawAlpha = this.yawAlpha(orientation, playerYaw);
        final double yawAlphaMod = this.yawAlphaMod(orientation, playerYaw);
        final double hyp = MathUtil.triHypotenuseB(dist, yawAlpha);
        final double ya = MathUtil.fastSqrt((hyp * hyp) - (dist * dist));
        final double yaMod = ya * yawAlphaMod;

        final double pa = MathUtil.triSideA(hyp, this.pitchAlpha(playerPitch));
        final double paMod = pa * this.pitchAlphaMod(playerPitch);

        final Location eyeLoc = player.getEyeLocation().clone();
        final Location clickLoc = eyeLoc.clone();
        clickLoc.setY(clickLoc.getY() + paMod);
        switch (orientation) {
            case NORTH -> {
                clickLoc.setX(clickLoc.getX() - yaMod);
                clickLoc.setZ(screenLoc.getZ());
            }
            case EAST -> {
                clickLoc.setZ(clickLoc.getZ() - yaMod);
                clickLoc.setX(screenLoc.getX());
            }
            case SOUTH -> {
                clickLoc.setX(clickLoc.getX() + yaMod);
                clickLoc.setZ(screenLoc.getZ());
            }
            case WEST -> {
                clickLoc.setZ(clickLoc.getZ() + yaMod);
                clickLoc.setX(screenLoc.getX());
            }
        }

        if (v) {
            player.sendMessage(String.format("  LB %,.2f %,.2f %,.2f", screen.getHitBox().bottomLeft().getX(), screen.getHitBox().bottomLeft().getY(), screen.getHitBox().bottomLeft().getZ()));
            player.sendMessage(String.format("  TR %,.2f %,.2f %,.2f", screen.getHitBox().topRight().getX(), screen.getHitBox().topRight().getY(), screen.getHitBox().topRight().getZ()));
            player.sendMessage(String.format("  C %,.2f %,.2f %,.2f", clickLoc.getX(), clickLoc.getY(), clickLoc.getZ()));

        }
        if (!screen.getHitBox().contains(clickLoc)) {
            if (v) {
                player.sendMessage("not in hitbox");
            }
            return null;
        }

        final Location relPos = screen.getHitBox().bottomLeft().clone().subtract(clickLoc.clone());
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

    public double distance(final Player player, final MapScreen screen) {
        final Location screenLoc = screen.getLocation();
        final Location playerLoc = player.getLocation();
        final BlockFace orientation = screen.getFrames()[0][0].getFacing();
        return (orientation == BlockFace.NORTH || orientation == BlockFace.SOUTH)
                ? Math.max(playerLoc.getZ(), screenLoc.getZ()) - Math.min(playerLoc.getZ(), screenLoc.getZ())
                : Math.max(playerLoc.getX(), screenLoc.getX()) - Math.min(playerLoc.getX(), screenLoc.getX());
    }

    private double pitchAlpha(final float playerPitch) {
        return Math.abs(playerPitch);
    }

    private double pitchAlphaMod(final float playerPitch) {
        return playerPitch >= 0 ? -1f : 1f;
    }

    private double yawAlpha(final BlockFace orientation, final float playerYaw) {
        return switch (orientation) {
            case NORTH -> Math.abs(playerYaw);
            case EAST -> playerYaw < 90f ? 90f - playerYaw : playerYaw - 90f;
            case SOUTH -> 90 - (Math.abs(playerYaw) - 90);
            case WEST -> playerYaw < -90f ? Math.abs(playerYaw) - 90f : 90f - Math.abs(playerYaw);
            default -> 0f;
        };
    }

    private double yawAlphaMod(final BlockFace orientation, final float playerYaw) {
        return switch (orientation) {
            case NORTH -> playerYaw < 0 ? -1f : 1f;
            case EAST -> playerYaw < 90 ? -1f : 1f;
            case SOUTH -> playerYaw > 0 ? -1f : 1f;
            case WEST -> playerYaw < -90 ? -1f : 1f;
            default -> 0f;
        };
    }

    private boolean isInFrontOfScreen(final BlockFace orientation, final Location playerLoc, final Location screenLoc) {
        return switch (orientation) {
            case NORTH -> playerLoc.getZ() < screenLoc.getZ();
            case EAST -> playerLoc.getX() > screenLoc.getX();
            case SOUTH -> playerLoc.getZ() > screenLoc.getZ();
            case WEST -> playerLoc.getX() < screenLoc.getX();
            default -> false;
        };
    }

    private boolean isFacingScreen(final BlockFace orientation, final float playerYaw) {
        return switch (orientation) {
            case NORTH -> Math.abs(playerYaw) < 90;
            case EAST -> playerYaw > 0 && playerYaw < 180;
            case SOUTH -> playerYaw < -90 || playerYaw > 90;
            case WEST -> playerYaw < 0;
            default -> false;
        };
    }

}
