package org.panda_lang.utilities.inject;

import org.panda_lang.utilities.commons.function.Option;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public interface InjectorProperty {

    default Option<Field> getField() {
        return Option.none();
    }

    default Option<Parameter> getParameter() {
        return Option.none();
    }

    <A extends Annotation> A getAnnotation(Class<A> annotation);

    Annotation[] getAnnotations();

    Class<?> getType();

    String getName();

}
