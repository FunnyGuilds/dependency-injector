package org.panda_lang.utilities.inject;

import org.panda_lang.utilities.inject.annotations.Inject;

import java.lang.reflect.Field;

public final class FieldsInjector<T> {

    private final InjectorProcessor processor;
    private final ConstructorInjector<T> constructorInjector;

    FieldsInjector(InjectorProcessor processor, ConstructorInjector<T> constructorInjector) {
        this.processor = processor;
        this.constructorInjector = constructorInjector;
    }

    public T newInstance(Object... injectorArgs) throws Throwable {
        T instance = constructorInjector.newInstance(injectorArgs);

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }

            field.setAccessible(true);
            field.set(instance, processor.tryFetchValue(processor, new PropertyField(field), injectorArgs));
        }

        return instance;
    }

}
