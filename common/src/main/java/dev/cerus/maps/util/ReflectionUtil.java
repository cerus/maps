package dev.cerus.maps.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionUtil {

    public static Object invoke(final String name, final Class<?> cls, final Object o, final Object[] params) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return invoke(name, cls, o, Arrays.stream(params).map(Object::getClass).toArray(Class[]::new), params);
    }

    public static Object invoke(final String name, final Class<?> cls, final Object o, final Class<?>[] paramsClasses, final Object[] params) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final Method method = cls.getDeclaredMethod(name, paramsClasses);
        method.setAccessible(true);
        return method.invoke(o, params);
    }

    public static Object get(final String name, final Class<?> cls, final Object o) throws NoSuchFieldException, IllegalAccessException {
        final Field field = cls.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(o);
    }

    public static void set(final String name, final Class<?> cls, final Object o, final Object val) throws NoSuchFieldException, IllegalAccessException {
        final Field field = cls.getDeclaredField(name);
        field.setAccessible(true);
        field.set(o, val);
    }

    public static Class<?> subclass(final String name, final Class<?> cls) {
        for (final Class<?> declaredClass : cls.getDeclaredClasses()) {
            if (declaredClass.getSimpleName().equals(name)) {
                return declaredClass;
            }
        }
        return null;
    }

}
