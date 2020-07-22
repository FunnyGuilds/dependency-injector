package org.panda_lang.utilities.inject;

import org.panda_lang.utilities.commons.function.Option;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

final class InjectorPropertyField implements InjectorProperty {

    private final Field field;

    InjectorPropertyField(Field field) {
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
    public Class<?> getType() {
        return field.getType();
    }

    @Override
    public String getName() {
        return field.getName();
    }

}
