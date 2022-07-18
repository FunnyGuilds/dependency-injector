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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.panda_lang.utilities.inject.annotations.PostConstruct;
import panda.std.Lazy;
import panda.utilities.ObjectUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.security.InvalidParameterException;
import java.util.ServiceLoader;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.ORDERED;

final class DefaultInjector implements Injector {

    private final Resources resources;
    private final InjectorProcessor processor;

    private final Lazy<MethodInjectorFactory> methodInjectorFactory = new Lazy<>(() ->
            StreamSupport.stream(Spliterators.spliteratorUnknownSize(ServiceLoader.load(MethodInjectorFactory.class).iterator(), ORDERED), false)
                    .findAny()
                    .orElseGet(() -> ((processor, method) -> forMethod(method)))
    );

    public DefaultInjector(Resources resources) {
        this.resources = resources;
        this.processor = new InjectorProcessor(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ConstructorInjector<T> forConstructor(Class<T> type) {
        if (type.getDeclaredConstructors().length != 1) {
            throw new InvalidParameterException("Class has to contain one and only constructor");
        }

        return new ConstructorInjector<T>(processor, (Constructor<T>) type.getDeclaredConstructors()[0]);
    }

    @Override
    public <T> ConstructorInjector<T> forConstructor(Constructor<T> constructor) {
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }

        return new ConstructorInjector<>(processor, constructor);
    }

    @Override
    public <T> T newInstance(Class<T> type, Object... injectorArgs) throws Throwable {
        T instance = forConstructor(type).newInstance(injectorArgs);
        invokeAnnotatedMethods(PostConstruct.class, instance, injectorArgs);
        return instance;
    }

    @Override
    public <T> FieldsInjector<T> forFields(Class<T> type) {
        if (type.getDeclaredConstructors().length != 1) {
            throw new InvalidParameterException("Class has to contain one and only constructor");
        }

        return new FieldsInjector<T>(processor, forConstructor(type));
    }

    @Override
    public <T> T newInstanceWithFields(Class<T> type, Object... injectorArgs) throws Throwable {
        T instance = forFields(type).newInstance(injectorArgs);
        invokeAnnotatedMethods(PostConstruct.class, instance, injectorArgs);
        return instance;
    }

    @Override
    public <T> T invokeMethod(Method method, Object instance, Object... injectorArgs) throws Throwable {
        return forMethod(method).invoke(instance, injectorArgs);
    }

    @Override
    public void invokeAnnotatedMethods(Class<? extends Annotation> annotation, Object instance, Object... injectorArgs) throws Throwable {
        for (Method method : getAllMethods(new ArrayList<>(), instance.getClass())) {
            if (!method.isAnnotationPresent(annotation)) {
                continue;
            }

            invokeMethod(method, instance, injectorArgs);
        }
    }

    @Override
    public MethodInjector forMethod(Method method) {
        return new DefaultMethodInjector(processor, method);
    }

    @Override
    public MethodInjector forGeneratedMethod(Method method) throws Exception {
        return methodInjectorFactory.get().createMethodInjector(processor, method);
    }

    @Override
    public <T> @Nullable T invokeParameter(Parameter parameter, Object... injectorArgs) throws Exception {
        return ObjectUtils.cast(processor.tryFetchValue(processor, new PropertyParameter(parameter), injectorArgs));
    }

    @Override
    public Injector fork(InjectorController controller) {
        return DependencyInjection.INJECTOR_FACTORY.createInjector(controller, resources.fork());
    }

    @Override
    public Injector duplicate(InjectorController controller) {
        return DependencyInjection.INJECTOR_FACTORY.createInjector(controller, resources.duplicate());
    }

    public Resources getResources() {
        return resources;
    }


    private static List<Method> getAllMethods(List<Method> methods, Class<?> type) {
        methods.addAll(Arrays.asList(type.getDeclaredMethods()));

        if (type.getSuperclass() != null) {
            getAllMethods(methods, type.getSuperclass());
        }

        return methods;
    }

}
