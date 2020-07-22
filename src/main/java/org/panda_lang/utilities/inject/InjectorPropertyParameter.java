package org.panda_lang.utilities.inject;

import org.panda_lang.utilities.commons.function.Option;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

final class InjectorPropertyParameter implements InjectorProperty {

    private final Parameter parameter;

    InjectorPropertyParameter(Parameter parameter) {
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
    public Class<?> getType() {
        return parameter.getType();
    }

    @Override
    public String getName() {
        return parameter.getName();
    }

}
