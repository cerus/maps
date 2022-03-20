package dev.cerus.maps.api.graphics;

import dev.cerus.maps.util.Vec2;
import java.util.function.Consumer;
import java.util.function.Function;

public class FunctionalMapGraphics<G extends MapGraphics<?, ?>> {

    private final G backing;

    private FunctionalMapGraphics(final G backing) {
        this.backing = backing;
    }

    public static <T extends MapGraphics<?, ?>> FunctionalMapGraphics<T> backedBy(final T backedBy) {
        return new FunctionalMapGraphics<>(backedBy);
    }

    public static FunctionalMapGraphics<MapGraphics<MapGraphics<?, ?>, Vec2>> standalone(final int w, final int h) {
        return new FunctionalMapGraphics<>(new StandaloneMapGraphics(w, h));
    }

    public FunctionalMapGraphics<G> with(final Consumer<G> action) {
        action.accept(this.backing);
        return this;
    }

    public <T> T get(final Function<G, T> function) {
        return function.apply(this.backing);
    }

    public G getGraphics() {
        return this.backing;
    }

}
