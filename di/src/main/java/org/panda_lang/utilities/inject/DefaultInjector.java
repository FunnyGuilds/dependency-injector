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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.panda_lang.utilities.inject.annotations.PostConstruct;
import panda.std.Lazy;
import panda.utilities.ObjectUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ServiceLoader;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.ORDERED;

final class DefaultInjector implements Injector {

    private final Resources resources;
    private final InjectorProcessor processor;

    private final Lazy<ConstructorInjectorFactory> constructorInjectorFactory = new Lazy<>(() ->
            StreamSupport.stream(Spliterators.spliteratorUnknownSize(ServiceLoader.load(ConstructorInjectorFactory.class).iterator(), ORDERED), false)
                    .findAny()
                    .orElseGet(() -> ((processor, constructor) -> this.forConstructor(constructor)))
    );
    private final Lazy<MethodInjectorFactory> methodInjectorFactory = new Lazy<>(() ->
            StreamSupport.stream(Spliterators.spliteratorUnknownSize(ServiceLoader.load(MethodInjectorFactory.class).iterator(), ORDERED), false)
                    .findAny()
                    .orElseGet(() -> ((processor, method) -> this.forMethod(method)))
    );

    public DefaultInjector(Resources resources) {
        this.resources = resources;
        this.processor = new InjectorProcessor(this);
    }

    @Override
    public <T> ConstructorInjector<T> forConstructor(Class<T> type) {
        return new DefaultConstructorInjector<>(this.processor, ClassCache.getConstructor(type));
    }

    @Override
    public <T> ConstructorInjector<T> forConstructor(Constructor<T> constructor) {
        return new DefaultConstructorInjector<>(this.processor, constructor);
    }

    @Override
    public <T> ConstructorInjector<T> forGeneratedConstructor(Class<T> type) {
        return this.forGeneratedConstructor(ClassCache.getConstructor(type));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ConstructorInjector<T> forGeneratedConstructor(Constructor<T> constructor) {
        return (ConstructorInjector<T>) this.constructorInjectorFactory.get().createConstructorInjector(this.processor, constructor);
    }

    @Override
    public <T> FieldsInjector<T> forFields(Class<T> type) {
        return new DefaultFieldsInjector<>(this.processor, this.forConstructor(type));
    }

    @Override
    public <T> FieldsInjector<T> forFields(Constructor<T> constructor) {
        return new DefaultFieldsInjector<>(this.processor, this.forConstructor(constructor));
    }

    @Override
    public <T> FieldsInjector<T> forGeneratedFields(Constructor<T> constructor) {
        return this.forFields(constructor);
    }

    @Override
    public <T> FieldsInjector<T> forGeneratedFields(Class<T> type) {
        return this.forFields(type);
    }

    @Override
    public <T> T newInstance(Class<T> type, Object... injectorArgs) throws DependencyInjectionException {
        try {
            T instance = this.forConstructor(type).newInstance(injectorArgs);
            this.invokeAnnotatedMethods(PostConstruct.class, instance, injectorArgs);
            return instance;
        } catch (Exception exception) {
            throw new DependencyInjectionException("Cannot create instance of " + type.getSimpleName(), exception);
        }
    }

    @Override
    public <T> T newInstance(Constructor<T> constructor, Object... injectorArgs) throws DependencyInjectionException {
        try {
            T instance = this.forConstructor(constructor).newInstance(injectorArgs);
            this.invokeAnnotatedMethods(PostConstruct.class, instance, injectorArgs);
            return instance;
        } catch (Exception exception) {
            throw new DependencyInjectionException("Cannot create instance of " + constructor.getDeclaringClass().getSimpleName(), exception);
        }
    }

    @Override
    public <T> T newInstanceWithFields(Class<T> type, Object... injectorArgs) throws DependencyInjectionException {
        try {
            T instance = this.forFields(type).newInstance(injectorArgs);
            this.invokeAnnotatedMethods(PostConstruct.class, instance, injectorArgs);
            return instance;
        } catch (Exception exception) {
            throw new DependencyInjectionException("Cannot create instance of " + type.getSimpleName(), exception);
        }
    }

    @Override
    public <T> T newInstanceWithFields(Constructor<T> constructor, Object... injectorArgs) throws DependencyInjectionException {
        try {
            T instance = this.forFields(constructor).newInstance(injectorArgs);
            this.invokeAnnotatedMethods(PostConstruct.class, instance, injectorArgs);
            return instance;
        } catch (Exception exception) {
            throw new DependencyInjectionException("Cannot create instance of " + constructor.getDeclaringClass().getSimpleName(), exception);
        }
    }

    @Override
    public MethodInjector forMethod(Method method) {
        return new DefaultMethodInjector(this.processor, method);
    }

    @Override
    public MethodInjector forGeneratedMethod(Method method) {
        return this.methodInjectorFactory.get().createMethodInjector(this.processor, method);
    }

    @Override
    public <T> @UnknownNullability T invokeMethod(Method method, Object instance, Object... injectorArgs) throws DependencyInjectionException {
        try {
            return this.forMethod(method).invoke(instance, injectorArgs);
        } catch (Exception exception) {
            throw new DependencyInjectionException("Cannot invoke method " + method.getName() + " of " + instance.getClass().getSimpleName(), exception);
        }
    }

    @Override
    public void invokeAnnotatedMethods(Class<? extends Annotation> annotation, Object instance, Object... injectorArgs) throws DependencyInjectionException {
        for (Method method : ClassCache.getAnnotatedMethods(instance.getClass(), annotation)) {
            this.invokeMethod(method, instance, injectorArgs);
        }
    }

    @Override
    public <T> @UnknownNullability T invokeParameter(Parameter parameter, Object... injectorArgs) throws Exception {
        return ObjectUtils.cast(this.processor.fetchValue(new PropertyParameter(parameter), injectorArgs));
    }

    @Override
    public Injector fork(InjectorController controller) {
        return DependencyInjection.INJECTOR_FACTORY.createInjector(controller, this.resources.fork());
    }

    @Override
    public Injector duplicate(InjectorController controller) {
        return DependencyInjection.INJECTOR_FACTORY.createInjector(controller, this.resources.duplicate());
    }

    public Resources getResources() {
        return this.resources;
    }

}
