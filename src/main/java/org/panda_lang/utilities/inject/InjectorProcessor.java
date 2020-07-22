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
import org.panda_lang.utilities.commons.ObjectUtils;
import org.panda_lang.utilities.commons.function.Option;
import org.panda_lang.utilities.commons.text.ContentJoiner;
import org.panda_lang.utilities.inject.annotations.Injectable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

final class InjectorProcessor {

    protected static final Object[] EMPTY = { };

    private final Injector injector;
    private final Map<Executable, Annotation[]> injectableCache = new HashMap<>();

    InjectorProcessor(Injector injector) {
        this.injector = injector;
    }

    protected Object[] fetchValues(InjectorCache cache, Object... injectorArgs) throws Exception {
        InjectorProperty[] properties = cache.getProperties();
        Object[] values = new Object[cache.getInjectable().length];

        for (int index = 0; index < values.length; index++) {
            values[index] = fetchValue(cache, properties[index], index, injectorArgs);
        }

        return values;
    }

    protected Object tryFetchValue(InjectorProcessor processor, InjectorProperty property, Object... injectorArgs) throws Exception {
        InjectorCache cache = InjectorCache.of(processor, property);
        return fetchValue(cache, property, 0, injectorArgs);
    }

    private @Nullable Object fetchValue(InjectorCache cache, InjectorProperty property, int index, Object... injectorArgs) throws Exception {
        Object value = cache.getBinds()[index].getValue(property, cache.getInjectable()[index], injectorArgs);

        for (InjectorResourceHandler<Annotation, Object, ?> handler : cache.getHandlers()[index]) {
            Annotation annotation = null;

            if (handler.getAnnotation().isPresent()) {
                annotation = cache.getAnnotations()[index].get(handler.getAnnotation().get());
            }

            value = handler.process(property, annotation, ObjectUtils.cast(value), injectorArgs);
        }

        return value;
    }

    protected InjectorProperty[] fetchInjectorProperties(Parameter[] parameters) {
        InjectorProperty[] properties = new InjectorProperty[parameters.length];

        for (int index = 0; index < parameters.length; index++) {
            properties[index] = new InjectorPropertyParameter(parameters[index]);
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

    protected InjectorResourceBind<Annotation>[] fetchBinds(Annotation[] annotations, Executable executable) {
        InjectorResources resources = injector.getResources();
        Parameter[] parameters = executable.getParameters();
        InjectorResourceBind<Annotation>[] binds = ObjectUtils.cast(new InjectorResourceBind[parameters.length]);

        for (int index = 0; index < annotations.length; index++) {
            Annotation annotation = annotations[index];
            Parameter parameter = parameters[index];

            Class<?> requiredType = annotation != null ? annotation.annotationType() : parameter.getType();
            Option<InjectorResourceBind<Annotation>> bindValue = resources.getBind(requiredType);

            binds[index] = bindValue.orThrow(() -> {
                String simplifiedParameters = ContentJoiner.on(", ").join(Arrays.stream(executable.getParameters())
                        .map(p -> p.getType().getSimpleName() + " " + p.getName())
                        .collect(Collectors.toList()))
                        .toString();

                throw new DependencyInjectionException(
                        "Cannot inject value due to missing bind" +
                        System.lineSeparator() +
                        "    missing bind for parameter: " + parameter.getType().getSimpleName() + " " + parameter.getName() +
                        System.lineSeparator() +
                        "    in executable: " + executable.getDeclaringClass().getSimpleName() + "#" + executable.getName() + "(" + simplifiedParameters + ")" +
                        System.lineSeparator()
                );
            });
        }

        return binds;
    }

    protected InjectorResourceBind<Annotation> fetchBind(@Nullable Annotation annotation, InjectorProperty property) {
        Class<?> requiredType = annotation != null ? annotation.annotationType() : property.getType();
        return injector.getResources().getBind(requiredType).orThrow(() -> {
            throw new DependencyInjectionException("Cannot find proper bind for " + property + " property");
        });
    }

    protected Collection<InjectorResourceHandler<Annotation, Object, ?>>[] fetchHandlers(Executable executable) {
        Collection<InjectorResourceHandler<Annotation, Object, ?>>[] handlers = ObjectUtils.cast(new Collection[executable.getParameterCount()]);
        Parameter[] parameters = executable.getParameters();

        for (int index = 0; index < parameters.length; index++) {
            handlers[index] = injector.getResources().getHandler(parameters[index]);
        }

        return handlers;
    }

}
