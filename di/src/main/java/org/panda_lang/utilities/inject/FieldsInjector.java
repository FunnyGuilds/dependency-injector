package org.panda_lang.utilities.inject;

import java.lang.reflect.Field;
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
        T instance = this.constructorInjector.newInstance(injectorArgs);
        for (Field field : getAllFields(instance.getClass())) {
            if (!field.isAnnotationPresent(Inject.class) && !field.isAnnotationPresent(AutoConstruct.class)) {
                continue;
            }

            field.setAccessible(true);
            field.set(instance, this.processor.fetchValue(new PropertyField(field), injectorArgs));
        }
        return instance;
    }

    private static Field[] getAllFields(Class<?> type) {
        Field[] fields = type.getDeclaredFields();
        Class<?> superType = type.getSuperclass();
        if (superType != null) {
            Field[] superFields = getAllFields(superType);
            Field[] allFields = new Field[fields.length + superFields.length];
            System.arraycopy(fields, 0, allFields, 0, fields.length);
            System.arraycopy(superFields, 0, allFields, fields.length, superFields.length);
            return allFields;
        }
        return fields;
    }

}
