package org.panda_lang.utilities.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.panda_lang.utilities.inject.annotations.AutoConstruct;
import org.panda_lang.utilities.inject.annotations.Inject;
import panda.std.Pair;
import panda.utilities.ObjectUtils;

/**
 * Utility class for caching class data (fields, methods, etc.) to improve performance.
 */
@ApiStatus.Internal
final class ClassCache {

    private static final Map<Class<?>, Constructor<?>> CACHED_CONSTRUCTORS = new ConcurrentHashMap<>();

    private static final Map<Class<?>, Field[]> CACHED_FIELDS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field[]> INJECTOR_CACHED_FIELDS = new ConcurrentHashMap<>();

    private static final Map<Pair<Class<?>, Class<? extends Annotation>>, Method[]> CACHED_ANNOTATED_METHODS = new ConcurrentHashMap<>();

    private ClassCache() {}

    public static <T> Constructor<T> getConstructor(Class<T> clazz) {
        return ObjectUtils.cast(CACHED_CONSTRUCTORS.computeIfAbsent(clazz, key -> {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            if (constructors.length != 1) {
                throw new InvalidParameterException("Class has to contain one and only constructor");
            }
            Constructor<T> constructor = ObjectUtils.cast(constructors[0]);
            constructor.setAccessible(true);
            return constructor;
        }));
    }

    /**
     * Get all fields of the class.
     * The result is cached.
     *
     * @param clazz class to get fields from
     * @return array of fields
     */
    public static Field[] getFields(Class<?> clazz) {
        return CACHED_FIELDS.computeIfAbsent(clazz, ClassCache::getAllFields);
    }

    /**
     * Get all fields of the class that are annotated with {@link Inject} or {@link AutoConstruct} and make them accessible.
     * The result is cached.
     *
     * @param clazz class to get fields from
     * @return array of fields
     */
    public static Field[] getInjectorFields(Class<?> clazz) {
        return INJECTOR_CACHED_FIELDS.computeIfAbsent(
                clazz,
                key -> Arrays.stream(getFields(clazz))
                        .filter(field -> field.isAnnotationPresent(Inject.class) || field.isAnnotationPresent(AutoConstruct.class))
                        .peek(field -> field.setAccessible(true))
                        .toArray(Field[]::new)
        );
    }

    private static Field[] getAllFields(Class<?> type) {
        Field[] fields = type.getDeclaredFields();
        Class<?> superType = type.getSuperclass();
        if (superType != null) {
            return mergeArrays(fields, getAllFields(superType));
        }
        return fields;
    }

    /**
     * Get all methods of the class that are annotated with the specified annotation.
     * The result is cached.
     *
     * @param clazz      class to get methods from
     * @param annotation annotation to filter methods by
     * @return array of methods
     */
    public static Method[] getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        return CACHED_ANNOTATED_METHODS.computeIfAbsent(
                Pair.of(clazz, annotation),
                key -> Arrays.stream(getAllMethods(clazz))
                        .filter(method -> method.isAnnotationPresent(annotation))
                        .toArray(Method[]::new)
        );
    }

    private static Method[] getAllMethods(Class<?> type) {
        Method[] methods = type.getDeclaredMethods();
        Class<?> superType = type.getSuperclass();
        if (superType != null) {
            return mergeArrays(methods, getAllMethods(superType));
        }
        return methods;
    }

    private static <T> T[] mergeArrays(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}
