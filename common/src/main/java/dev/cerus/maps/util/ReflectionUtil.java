package dev.cerus.maps.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Collection of reflection utility methods
 */
public class ReflectionUtil {

    private ReflectionUtil() {
        throw new UnsupportedOperationException();
    }

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

    public static Optional<Field> findField(final Class<?> cls, Class<?> fieldType) {
        return findField(cls, fieldType, field -> true);
    }

    public static Optional<Field> findField(final Class<?> cls, Class<?> fieldType, Predicate<Field> filter) {
        return findField(cls, f -> f.getType() == fieldType && filter.test(f));
    }

    public static Optional<Field> findField(final Class<?> cls, Predicate<Field> filter) {
        for (Field field : cls.getDeclaredFields()) {
            if (filter.test(field)) {
                return Optional.of(field);
            }
        }
        return Optional.empty();
    }

    public static Optional<Class<?>> findClass(String... names) {
        for (String name : names) {
            try {
                return Optional.of(Class.forName(name));
            } catch (ClassNotFoundException ignored) {
            }
        }
        return Optional.empty();
    }

    public static Object sneakyGet(Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
