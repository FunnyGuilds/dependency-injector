package org.panda_lang.utilities.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import panda.std.Pair;

final class MethodsCache {

    private final Map<Class<?>, List<Method>> cachedMethods = new HashMap<>();
    private final Map<Pair<Class<?>, Class<? extends Annotation>>, List<Method>> cachedAnnotatedMethods = new HashMap<>();

    public List<Method> getMethods(Class<?> clazz) {
        List<Method> methods = this.cachedMethods.get(clazz);
        if (methods == null) {
            methods = getAllMethods(new ArrayList<>(), clazz);
            this.cachedMethods.put(clazz, methods);
        }
        return methods;
    }

    public List<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Method> methods = this.cachedAnnotatedMethods.get(Pair.of(clazz, annotation));
        if (methods == null) {
            methods = getAllMethods(new ArrayList<>(), clazz)
                    .stream()
                    .filter(method -> method.isAnnotationPresent(annotation))
                    .collect(Collectors.toList());

            this.cachedAnnotatedMethods.put(Pair.of(clazz, annotation), methods);
        }
        return methods;
    }

    private static List<Method> getAllMethods(List<Method> methods, Class<?> type) {
        methods.addAll(Arrays.asList(type.getDeclaredMethods()));
        if (type.getSuperclass() != null) {
            getAllMethods(methods, type.getSuperclass());
        }
        return methods;
    }

}
