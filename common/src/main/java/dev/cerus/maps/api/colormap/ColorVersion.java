package dev.cerus.maps.api.colormap;

public enum ColorVersion {

    /**
     * 1.8 colors
     */
    MC_1_8(35),

    /**
     * 1.12 colors
     */
    MC_1_12(51),

    /**
     * 1.16 colors
     */
    MC_1_16(58),

    /**
     * 1.17 colors
     */
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
