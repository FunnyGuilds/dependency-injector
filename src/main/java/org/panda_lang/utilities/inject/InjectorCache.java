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

import org.panda_lang.utilities.commons.ArrayUtils;
import org.panda_lang.utilities.inject.annotations.Injectable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

final class InjectorCache {

    private final Property[] properties;
    private final Annotation[] injectable;
    private final Map<Class<? extends Annotation>, Annotation>[] annotations;
    private final Bind<Annotation>[] binds;
    private final Collection<BindHandler<Annotation, Object, ?>>[] handlers;

    private InjectorCache(
        Property[] properties,
        Annotation[] injectable,
        Map<Class<? extends Annotation>, Annotation>[] annotations,
        Bind<Annotation>[] binds,
        Collection<BindHandler<Annotation, Object, ?>>[] handlers
    ) {
        this.properties = properties;
        this.injectable = injectable;
        this.annotations = annotations;
        this.binds = binds;
        this.handlers = handlers;
    }

    Annotation[] getInjectable() {
        return injectable;
    }

    Map<Class<? extends Annotation>, Annotation>[] getAnnotations() {
        return annotations;
    }

    Bind<Annotation>[] getBinds() {
        return binds;
    }

    Collection<BindHandler<Annotation, Object, ?>>[] getHandlers() {
        return handlers;
    }

    Property[] getProperties() {
        return properties;
    }

    public static InjectorCache of(InjectorProcessor processor, Executable executable) {
        Annotation[] injectable = processor.fetchAnnotations(executable);

        return new InjectorCache(
                processor.fetchInjectorProperties(executable.getParameters()),
                injectable,
                processor.fetchAnnotationsMap(executable),
                processor.fetchBinds(injectable, executable),
                processor.fetchHandlers(executable)
        );
    }

    public static InjectorCache of(InjectorProcessor processor, Property property) {
        Annotation annotation = ArrayUtils.findIn(property.getAnnotations(), a -> a.annotationType().isAnnotationPresent(Injectable.class)).getOrNull();

        return new InjectorCache(
                ArrayUtils.of(property),
                ArrayUtils.of(annotation),
                ArrayUtils.of(Collections.emptyMap()),
                ArrayUtils.of(processor.fetchBind(annotation, property)),
                ArrayUtils.of(Collections.emptyList())
        );
    }

}