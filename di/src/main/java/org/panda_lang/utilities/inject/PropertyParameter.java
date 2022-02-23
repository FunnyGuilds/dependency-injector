package org.panda_lang.utilities.inject;

import panda.std.Option;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

final class PropertyParameter implements Property {

    private final Parameter parameter;

    PropertyParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public Option<Parameter> getParameter() {
        return Option.of(parameter);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotation) {
        return parameter.getAnnotation(annotation);
    }

    @Override
    public Annotation[] getAnnotations() {
        return parameter.getAnnotations();
    }

    @Override
    public Type getParametrizedType() {
        return parameter.getParameterizedType();
    }

    @Override
    public Class<?> getType() {
        return parameter.getType();
    }

    @Override
    public String getName() {
        return parameter.getName();
    }

}
