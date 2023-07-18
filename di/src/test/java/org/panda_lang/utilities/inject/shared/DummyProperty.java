package org.panda_lang.utilities.inject.shared;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.panda_lang.utilities.inject.Property;
import panda.std.stream.PandaStream;

public class DummyProperty<T> implements Property {

    private final String name;
    private final Class<T> type;
    private final Map<Class<? extends Annotation>, Annotation> annotations;

    public DummyProperty(String name, Class<T> type, List<Class<? extends Annotation>> annotations) {
        this.name = name;
        this.type = type;
        this.annotations = PandaStream.of(annotations)
                .toMap(annotation -> annotation, annotation -> {
                    try {
                        return AnnotationUtils.instanceAnnotation(annotation);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }

    @SafeVarargs
    public DummyProperty(String name, Class<T> type, Class<? extends Annotation>... annotations) {
        this(name, type, Arrays.asList(annotations));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Class<T> getType() {
        return this.type;
    }

    @Override
    public Type getParametrizedType() {
        return this.type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> annotation) {
        return (A) this.annotations.get(annotation);
    }

    @Override
    public Annotation[] getAnnotations() {
        return this.annotations.values().toArray(new Annotation[0]);
    }

}
