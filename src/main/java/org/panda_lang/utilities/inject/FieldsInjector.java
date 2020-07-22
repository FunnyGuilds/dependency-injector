package org.panda_lang.utilities.inject;

import org.panda_lang.utilities.inject.annotations.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public final class FieldsInjector<T> {

    private final InjectorProcessor processor;
    private final Constructor<T> constructor;
    private final InjectorCache cache;

    FieldsInjector(InjectorProcessor processor, Constructor<T> constructor) {
        this.processor = processor;
        this.constructor = constructor;
        this.cache = InjectorCache.of(processor, constructor);
        this.constructor.setAccessible(true);
    }

    public T newInstance(Object... injectorArgs) throws Throwable {
        T instance = constructor.newInstance(processor.fetchValues(cache, injectorArgs));

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }

            field.setAccessible(true);
            field.set(instance, processor.tryFetchValue(processor, new InjectorPropertyField(field), injectorArgs));
        }

        return instance;
    }

    public Constructor<T> getConstructor() {
        return constructor;
    }

}
