package org.panda_lang.utilities.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.panda_lang.utilities.inject.annotations.AutoConstruct;
import org.panda_lang.utilities.inject.annotations.Inject;
import panda.std.Pair;

final class ClassCache {

    private static final Map<Class<?>, Field[]> CACHED_FIELDS = new HashMap<>();
    private static final Map<Class<?>, Field[]> INJECTOR_CACHED_FIELDS = new HashMap<>();

    private static final Map<Pair<Class<?>, Class<? extends Annotation>>, Method[]> CACHED_ANNOTATED_METHODS = new HashMap<>();

    public static Field[] getFields(Class<?> clazz) {
        return CACHED_FIELDS.computeIfAbsent(clazz, ClassCache::getAllFields);
    }

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
