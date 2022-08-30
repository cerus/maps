package dev.cerus.maps.api.colormap;

public enum ColorVersion {

    MC_1_8(35),
    MC_1_12(51),
    MC_1_16(58),
    MC_1_17(61);

    private final int lastId;

    ColorVersion(final int lastId) {
        this.lastId = lastId;
    }

    public static ColorVersion latest() {
        return MC_1_17;
    }

    public int getLastId() {
        return this.lastId;
    }

}
