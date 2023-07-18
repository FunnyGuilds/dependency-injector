/*
 * Copyright (c) 2020 Dzikoysk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.panda_lang.utilities.inject;

import org.jetbrains.annotations.Nullable;
import org.panda_lang.utilities.inject.annotations.AutoConstruct;
import org.panda_lang.utilities.inject.annotations.Injectable;
import panda.utilities.ObjectUtils;
import panda.utilities.text.Joiner;
import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

final class InjectorProcessor {

    private final Injector injector;
    private final Map<Executable, Annotation[]> injectableCache = new HashMap<>();

    private final Bind<Annotation> autoConstructBind;

    InjectorProcessor(Injector injector) {
        this.injector = injector;

        this.autoConstructBind = new DefaultBind<>(AutoConstruct.class);
        this.autoConstructBind.assignThrowingHandler((property, annotation, injectorArgs) -> injector.newInstanceWithFields(property.getType(), injectorArgs));
    }

    protected Object[] fetchValues(InjectorCache cache, Object... injectorArgs) throws Exception {
        Property[] properties = cache.getProperties();
        Object[] values = new Object[cache.getInjectable().length];
        for (int index = 0; index < values.length; index++) {
            values[index] = fetchValue(cache, properties[index], index, injectorArgs);
        }
        return values;
    }

    protected @Nullable Object fetchValue(Property property, Object... injectorArgs) throws Exception {
        InjectorCache cache = InjectorCache.of(this, property);
        return fetchValue(cache, property, 0, injectorArgs);
    }

    private @Nullable Object fetchValue(InjectorCache cache, Property property, int index, Object... injectorArgs) throws Exception {
        Object value = cache.getBinds()[index].getValue(property, cache.getInjectable()[index], injectorArgs);

        for (BindHandler<Annotation, Object, ?> handler : cache.getHandlers()[index]) {
            Annotation annotation = null;
            if (handler.getAnnotation().isPresent()) {
                annotation = cache.getAnnotations()[index].get(handler.getAnnotation().get());
            }
            value = handler.process(property, annotation, ObjectUtils.cast(value), injectorArgs);
        }

        return value;
    }

    protected Property[] fetchInjectorProperties(Parameter[] parameters) {
        Property[] properties = new Property[parameters.length];
        for (int index = 0; index < parameters.length; index++) {
            properties[index] = new PropertyParameter(parameters[index]);
        }
        return properties;
    }

    protected Annotation[] fetchAnnotations(Executable executable) {
        Annotation[] injectorAnnotations = injectableCache.get(executable);
        if (injectorAnnotations != null) {
            return injectorAnnotations;
        }

        injectorAnnotations = new Annotation[executable.getParameterTypes().length];
        Annotation[][] parameterAnnotations = injector.getResources().fetchAnnotations(executable);

        for (int index = 0; index < parameterAnnotations.length; index++) {
            for (Annotation annotation : parameterAnnotations[index]) {
                if (annotation.annotationType().isAnnotationPresent(Injectable.class)) {
                    injectorAnnotations[index] = annotation;
                }
            }
        }

        injectableCache.put(executable, injectorAnnotations);
        return injectorAnnotations;
    }

    protected Map<Class<? extends Annotation>, Annotation>[] fetchAnnotationsMap(Executable executable) {
        Annotation[][] annotations = injector.getResources().fetchAnnotations(executable);
        Map<Class<? extends Annotation>, Annotation>[] mappedAnnotations = ObjectUtils.cast(new HashMap[annotations.length]);

        for (int index = 0; index < annotations.length; index++) {
            Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<>();
            for (Annotation annotation : annotations[index]) {
                annotationMap.put(annotation.annotationType(), annotation);
            }
            mappedAnnotations[index] = annotationMap;
        }

        return mappedAnnotations;
    }

    protected Bind<Annotation>[] fetchBinds(Annotation[] annotations, Executable executable) throws MissingBindException {
        Resources resources = injector.getResources();
        Parameter[] parameters = executable.getParameters();
        Bind<Annotation>[] binds = ObjectUtils.cast(new Bind[parameters.length]);

        for (int index = 0; index < annotations.length; index++) {
            Annotation annotation = annotations[index];
            Parameter parameter = parameters[index];

            Bind<Annotation> bind = annotation != null
                    ? this.injector.getResources().getBind(annotation.annotationType()).orNull()
                    : null;

            if (bind == null) {
                bind = this.injector.getResources().getBind(parameter.getType()).orNull();
            }

            if (bind == null && parameter.getAnnotation(AutoConstruct.class) != null) {
                bind = this.autoConstructBind;
            }

            if (bind == null) {
                String simplifiedParameters = Joiner.on(", ").join(Arrays.stream(executable.getParameters())
                        .map(p -> p.getType().getSimpleName() + " " + p.getName())
                        .collect(Collectors.toList()))
                        .toString();

                throw new MissingBindException(
                        "Cannot find proper bind" +
                        System.lineSeparator() +
                        "    missing bind for parameter: " + parameter.getType().getSimpleName() + " " + parameter.getName() +
                        System.lineSeparator() +
                        "    in executable: " + executable.getDeclaringClass().getSimpleName() + "#" + executable.getName() + "(" + simplifiedParameters + ")" +
                        System.lineSeparator()
                );
            }
            binds[index] = bind;
        }

        return binds;
    }

    protected Bind<Annotation> fetchBind(@Nullable Annotation annotation, Property property) throws MissingBindException {
        Bind<Annotation> bind = annotation != null
                ? this.injector.getResources().getBind(annotation.annotationType()).orNull()
                : null;

        if (bind == null) {
            bind = this.injector.getResources().getBind(property.getType()).orNull();
        }

        if (bind == null && property.getAnnotation(AutoConstruct.class) != null) {
            bind = this.autoConstructBind;
        }

        if (bind == null) {
            throw new MissingBindException("Cannot find proper bind for property: " + property.getType().getSimpleName() + " " + property.getName());
        }
        return bind;
    }

    protected Collection<BindHandler<Annotation, Object, ?>>[] fetchHandlers(Executable executable) {
        Collection<BindHandler<Annotation, Object, ?>>[] handlers = ObjectUtils.cast(new Collection[executable.getParameterCount()]);
        Parameter[] parameters = executable.getParameters();

        for (int index = 0; index < parameters.length; index++) {
            handlers[index] = injector.getResources().getHandler(parameters[index]);
        }

        return handlers;
    }

}
