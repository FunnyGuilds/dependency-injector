package org.panda_lang.utilities.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class MethodsCache {

    private final Map<Class<?>, List<Method>> cachedMethods = new HashMap<>();
    private final Map<Class<? extends Annotation>, Map<Class<?>, List<Method>>> cachedAnnotatedMethods = new HashMap<>();

    public List<Method> getMethods(Class<?> clazz) {
        List<Method> methods = this.cachedMethods.get(clazz);
        if (methods == null) {
            methods = getAllMethods(new ArrayList<>(), clazz);
            this.cachedMethods.put(clazz, methods);
        }
        return methods;
    }

    public List<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        Map<Class<?>, List<Method>> annotatedMethods = this.cachedAnnotatedMethods.get(annotation);
        if (annotatedMethods == null) {
            annotatedMethods = new HashMap<>();

            annotatedMethods.put(clazz, this.getMethods(clazz)
                    .stream()
                    .filter(method -> method.isAnnotationPresent(annotation))
                    .collect(Collectors.toList()));

            this.cachedAnnotatedMethods.put(annotation, annotatedMethods);
        }
        return annotatedMethods.get(clazz);
    }

    private static List<Method> getAllMethods(List<Method> methods, Class<?> type) {
        methods.addAll(Arrays.asList(type.getDeclaredMethods()));
        if (type.getSuperclass() != null) {
            getAllMethods(methods, type.getSuperclass());
        }
        return methods;
    }

}
