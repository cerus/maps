package dev.cerus.maps.util;

public class EntityIdUtil {

    private static int ID = 17031703;

    private EntityIdUtil() {
    }

    public static int next() {
        return ID++;
    }

}
