package org.panda_lang.utilities.inject;

import java.lang.reflect.Field;

public final class FieldsInjector<T> {

    private final InjectorProcessor processor;
    private final ConstructorInjector<T> constructorInjector;

    FieldsInjector(InjectorProcessor processor, ConstructorInjector<T> constructorInjector) {
        this.processor = processor;
        this.constructorInjector = constructorInjector;
    }

    public T newInstance(Object... injectorArgs) throws Exception {
        T instance = this.constructorInjector.newInstance(injectorArgs);
        for (Field field : ClassCache.getInjectorFields(instance.getClass())) {
            field.set(instance, this.processor.fetchValue(new PropertyField(field), injectorArgs));
        }
        return instance;
    }

}
