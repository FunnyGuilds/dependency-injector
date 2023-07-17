package org.panda_lang.utilities.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.panda_lang.utilities.inject.annotations.AutoConstruct;
import org.panda_lang.utilities.inject.annotations.Inject;

public final class FieldsInjector<T> {

    private final InjectorProcessor processor;
    private final ConstructorInjector<T> constructorInjector;

    FieldsInjector(InjectorProcessor processor, ConstructorInjector<T> constructorInjector) {
        this.processor = processor;
        this.constructorInjector = constructorInjector;
    }

    public T newInstance(Object... injectorArgs) throws Exception {
        T instance = constructorInjector.newInstance(injectorArgs);

        for (Field field : getAllFields(instance.getClass())) {
            if (!field.isAnnotationPresent(Inject.class) && !field.isAnnotationPresent(AutoConstruct.class)) {
                continue;
            }

            field.setAccessible(true);
            field.set(instance, processor.tryFetchValue(processor, new PropertyField(field), injectorArgs));
        }

        return instance;
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            fields.addAll(getAllFields(type.getSuperclass()));
        }
        return fields;
    }

}
