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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public interface Injector {

    /**
     * Create injector for the given type
     *
     * @param type the type to process
     * @param <T>  type of class
     * @return constructor injector
     */
    <T> ConstructorInjector<T> forConstructor(Class<T> type);

    /**
     * Create injector for the given constructor
     *
     * @param constructor the constructor to process
     * @param <T>         type of class
     * @return constructor injector
     */
    <T> ConstructorInjector<T> forConstructor(Constructor<T> constructor);

    <T> ConstructorInjector<T> forGeneratedConstructor(Class<T> type);

    <T> ConstructorInjector<T> forGeneratedConstructor(Constructor<T> constructor);

    /**
     * Create injector for fields (and constructor)
     *
     * @param type the type to process
     * @param <T>  type of class
     * @return fields injector
     */
    <T> FieldsInjector<T> forFields(Class<T> type);

    /**
     * Create injector for fields (and constructor)
     *
     * @param constructor the constructor to process
     * @param <T>         type of class
     * @return fields injector
     */
    <T> FieldsInjector<T> forFields(Constructor<T> constructor);

    <T> FieldsInjector<T> forGeneratedFields(Class<T> type);

    <T> FieldsInjector<T> forGeneratedFields(Constructor<T> constructor);

    /**
     * Create a new instance of the specified type using Injector
     *
     * @param type the class to instantiate
     * @param <T>  the type
     * @return a new instance
     * @throws DependencyInjectionException if anything happened during the injection
     */
    <T> T newInstance(Class<T> type, Object... injectorArgs) throws DependencyInjectionException;

    /**
     * Create a new instance of the specified constructor using Injector
     *
     * @param constructor the constructor to instantiate
     * @param <T>         the type
     * @return a new instance
     * @throws DependencyInjectionException if anything happens during the injection
     */
    <T> T newInstance(Constructor<T> constructor, Object... injectorArgs) throws DependencyInjectionException;

    /**
     * Create a new instance of the specified type using Injector with support for field injection
     *
     * @param type the class to instantiate
     * @param <T>  type of class
     * @return a new instance
     * @throws DependencyInjectionException if anything happens during the injection
     */
    <T> T newInstanceWithFields(Class<T> type, Object... injectorArgs) throws DependencyInjectionException;

    /**
     * Create a new instance of the specified constructor using Injector with support for field injection
     *
     * @param constructor the constructor to instantiate
     * @param <T>         type of class
     * @return a new instance
     * @throws DependencyInjectionException if anything happens during the injection
     */
    <T> T newInstanceWithFields(Constructor<T> constructor, Object... injectorArgs) throws DependencyInjectionException;

    /**
     * Create injector for the given method
     *
     * @param method the method to process
     * @return injector for the given method
     */
    MethodInjector forMethod(Method method);

    /**
     * Generate injector for the given method
     * <p>
     * This method should be used only with <strong>di-codegen</strong> module or other {@link MethodInjector} implementation
     * Otherwise, it will fall back to {@link #forMethod(Method)} and performance could be degraded
     *
     * @param method the method to process (works only for public properties)
     * @return injector for the given method
     * @throws Exception if anything happens during the generation of method wrapper
     */
    MethodInjector forGeneratedMethod(Method method);

    /**
     * Invoke the method using Injector
     *
     * @param method   the method to invoke
     * @param instance the instance to use (nullable for static context)
     * @param <T>      the return type
     * @return the return value
     * @throws DependencyInjectionException if anything happens during execution of the method
     */
    @UnknownNullability <T> T invokeMethod(Method method, @Nullable Object instance, Object... injectorArgs) throws DependencyInjectionException;

    /**
     * Invoke methods annotated with the given annotation
     *
     * @param annotation the annotation to look for
     * @param instance   the instance to use
     * @throws DependencyInjectionException if anything happens during execution of the methods
     */
    void invokeAnnotatedMethods(Class<? extends Annotation> annotation, Object instance, Object... injectorArgs) throws DependencyInjectionException;

    /**
     * Get value that would be used to inject the given parameter
     *
     * @param parameter the parameter for which the value will be returned
     * @param <T>       type of expected value
     * @return the associated binding value
     * @throws Exception if anything happens during invocation of the parameter
     */
    @ApiStatus.Internal
    @UnknownNullability <T> T invokeParameter(Parameter parameter, Object... injectorArgs) throws Exception;

    /**
     * Create a fork of resources. The current resources will be used as a parent of a new instance.
     *
     * @param controller initializer for a new instance of resources
     * @return a forked instance of resources
     */
    Injector fork(InjectorController controller);

    /**
     * Duplicate (clone) resources
     *
     * @param controller initializer for a new instance of resources
     * @return a duplicated instance of resources
     */
    Injector duplicate(InjectorController controller);

    /**
     * Get resources of injector
     *
     * @return the resources
     */
    Resources getResources();

}
