package org.panda_lang.utilities.inject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.panda_lang.utilities.inject.annotations.Inject;

import java.lang.reflect.Field;
import org.panda_lang.utilities.inject.annotations.PostConstruct;

public final class FieldsInjector<T> {

    private final InjectorProcessor processor;
    private final ConstructorInjector<T> constructorInjector;

    FieldsInjector(InjectorProcessor processor, ConstructorInjector<T> constructorInjector) {
        this.processor = processor;
        this.constructorInjector = constructorInjector;
    }

    public T newInstance(Object... injectorArgs) throws Throwable {
        T instance = constructorInjector.newInstance(injectorArgs);

        for (Field field : getAllFields(new ArrayList<>(), instance.getClass())) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }

            field.setAccessible(true);
            field.set(instance, processor.tryFetchValue(processor, new PropertyField(field), injectorArgs));
        }

        for (Method method : getAllMethods(new ArrayList<>(), instance.getClass())) {
            if (!method.isAnnotationPresent(PostConstruct.class)) {
                continue;
            }

            method.setAccessible(true);
            method.invoke(instance, processor.tryFetchValues(processor, method, injectorArgs));
        }

        return instance;
    }

    private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    private static List<Method> getAllMethods(List<Method> methods, Class<?> type) {
        methods.addAll(Arrays.asList(type.getDeclaredMethods()));

        if (type.getSuperclass() != null) {
            getAllMethods(methods, type.getSuperclass());
        }

        return methods;
    }

}
