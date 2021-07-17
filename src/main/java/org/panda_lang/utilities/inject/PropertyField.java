package org.panda_lang.utilities.inject;

import panda.std.Option;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

final class PropertyField implements Property {

    private final Field field;

    PropertyField(Field field) {
        this.field = field;
    }

    @Override
    public Option<Field> getField() {
        return Option.of(field);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotation) {
        return field.getAnnotation(annotation);
    }

    @Override
    public Annotation[] getAnnotations() {
        return field.getAnnotations();
    }

    @Override
    public Type getParametrizedType() {
        return field.getGenericType();
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

    @Override
    public String getName() {
        return field.getName();
    }

}
