package dev.cerus.maps.api.event;

import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.util.Vec2;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player clicks a map screen
 * <p>
 * Note: This is only called if the maps plugin is installed and click listening is enabled.
 */
public class PlayerClickScreenEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final MapScreen clickedScreen;
    private final Vec2 clickPos;
    private final boolean rightClick;
    private boolean cancelled;

    public PlayerClickScreenEvent(final Player who, final boolean async, final MapScreen clickedScreen, final Vec2 clickPos, final boolean rightClick) {
        super(async);
        this.player = who;
        this.clickedScreen = clickedScreen;
        this.clickPos = clickPos;
        this.rightClick = rightClick;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Player getPlayer() {
        return this.player;
    }

    public MapScreen getClickedScreen() {
        return this.clickedScreen;
    }

    public Vec2 getClickPos() {
        return this.clickPos;
    }

    public boolean isRightClick() {
        return this.rightClick;
    }

    public boolean isLeftClick() {
        return !this.isRightClick();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }

}
