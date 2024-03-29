package dev.cerus.maps.api;

import java.util.Arrays;
import java.util.Objects;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.map.MapCursor;

/**
 * Represents a map marker
 * <p>
 * Changes to instances of this maker will call back to the parent clientside map (if one is present)
 */
public class Marker {

    private int x;
    private int y;
    private byte direction;
    private byte type;
    private boolean visible;
    private BaseComponent[] caption;
    private ClientsideMap parent;

    public Marker(final int x, final int y, final byte direction, final byte type, final boolean visible) {
        this(x, y, direction, type, visible, new BaseComponent[0]);
    }

    public Marker(final int x, final int y, final byte direction, final MapCursor.Type type, final boolean visible) {
        this(x, y, direction, type, visible, new BaseComponent[0]);
    }

    public Marker(final int x, final int y, final byte direction, final byte type, final boolean visible, final BaseComponent... caption) {
        this.x = x;
        this.y = y;
        this.setDirection(direction);
        this.setRawType(type);
        this.visible = visible;
        this.caption = caption;
    }

    public Marker(final int x, final int y, final byte direction, final MapCursor.Type type, final boolean visible, final BaseComponent... caption) {
        this.x = x;
        this.y = y;
        this.setDirection(direction);
        this.setType(type);
        this.visible = visible;
        this.caption = caption;
    }

    public void setRawType(final byte type) {
        if (type < 0 || type > 26) {
            throw new IllegalArgumentException("Type must be in the range 0-26");
        }
        this.type = type;
        if (this.parent != null) {
            this.parent.setDirtyMarkers(true);
        }
    }

    public int getX() {
        return this.x;
    }

    public void setX(final int x) {
        this.x = x;
        if (this.parent != null) {
            this.parent.setDirtyMarkers(true);
        }
    }

    // X pos used in packet
    public byte getCompressedX() {
        return (byte) ((this.x % 256) - 128);
    }

    public int getY() {
        return this.y;
    }

    public void setY(final int y) {
        this.y = y;
        if (this.parent != null) {
            this.parent.setDirtyMarkers(true);
        }
    }

    // Y pos used in packet
    public byte getCompressedY() {
        return (byte) ((this.y % 256) - 128);
    }

    public byte getDirection() {
        return this.direction;
    }

    public void setDirection(final byte direction) {
        if (direction < 0 || direction > 15) {
            throw new IllegalArgumentException("Direction must be in the range 0-15");
        }
        this.direction = direction;
        if (this.parent != null) {
            this.parent.setDirtyMarkers(true);
        }
    }

    public byte getType() {
        return this.type;
    }

    public void setType(final MapCursor.Type type) {
        this.setRawType(type.getValue());
    }

    public void setType(final byte type) {
        this.type = type;
        if (this.parent != null) {
            this.parent.setDirtyMarkers(true);
        }
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
        if (this.parent != null) {
            this.parent.setDirtyMarkers(true);
        }
    }

    public boolean hasCaption() {
        return this.getCaption() != null && this.getCaption().length > 0;
    }

    public BaseComponent[] getCaption() {
        return this.caption;
    }

    public void setCaption(final BaseComponent[] caption) {
        this.caption = caption;
        if (this.parent != null) {
            this.parent.setDirtyMarkers(true);
        }
    }

    public String getCaptionString() {
        return ComponentSerializer.toString(this.caption);
    }

    void setParent(final ClientsideMap parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Marker marker = (Marker) o;
        return this.getX() == marker.getX() && this.getY() == marker.getY() && this.getDirection() == marker.getDirection()
                && this.getType() == marker.getType() && this.isVisible() == marker.isVisible() && Arrays.equals(this.getCaption(), marker.getCaption());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.getX(), this.getY(), this.getDirection(), this.getType(), this.isVisible());
        result = 31 * result + Arrays.hashCode(this.getCaption());
        return result;
    }

}
