package org.panda_lang.utilities.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

final class DefaultFieldsInjector<T> implements FieldsInjector<T> {

    private final InjectorProcessor processor;
    private final ConstructorInjector<T> constructorInjector;
    private final Field[] fields;

    DefaultFieldsInjector(InjectorProcessor processor, ConstructorInjector<T> constructorInjector) {
        this.processor = processor;
        this.constructorInjector = constructorInjector;
        this.fields = ClassCache.getInjectorFields(constructorInjector.getConstructor().getDeclaringClass());
    }

    @Override
    public T newInstance(Object... injectorArgs) throws Exception {
        T instance = this.constructorInjector.newInstance(injectorArgs);
        for (Field field : this.fields) {
            field.set(instance, this.processor.fetchValue(new PropertyField(field), injectorArgs));
        }
        return instance;
    }

    @Override
    public Constructor<T> getConstructor() {
        return this.constructorInjector.getConstructor();
    }

}
