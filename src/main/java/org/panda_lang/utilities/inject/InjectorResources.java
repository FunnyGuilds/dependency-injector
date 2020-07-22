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

import org.panda_lang.utilities.commons.function.Option;
import org.panda_lang.utilities.commons.function.ThrowingQuadFunction;
import org.panda_lang.utilities.commons.function.ThrowingTriFunction;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Collection;

public interface InjectorResources {

    /**
     * Create bind for the specified type
     *
     * @param associatedType type to bind
     * @return the bind based on associated type
     */
    InjectorResourceBind<?> on(Class<?> associatedType);

    /**
     * Create bind for parameters annotated with the specified annotation
     *
     * @param annotation the annotation to bind
     * @param <A> type of annotation
     * @return the bind based on associated annotation
     */
    <A extends Annotation> InjectorResourceBind<A> annotatedWith(Class<A> annotation);

    /**
     * Create bind for parameters annotated with the specified annotation tested by the {@link org.panda_lang.utilities.inject.DependencyInjectionUtils#testAnnotation(Class)} method
     *
     * @param annotation the annotation to bind
     * @param <A> type of annotation
     * @return the bind based on associated annotation
     */
    default <A extends Annotation> InjectorResourceBind<A> annotatedWithTested(Class<A> annotation) {
        return annotatedWith(DependencyInjectionUtils.testAnnotation(annotation));
    }

    /**
     * Process injected object of the given type
     *
     * @param associatedType type to process
     * @param processor the processor
     * @param <V> type of value to process
     * @param <R> return type
     * @param <E> type of thrown exception
     */
    <V, R, E extends Exception> void processType(Class<V> associatedType, ThrowingTriFunction<Parameter, V, Object[], R, E> processor);

    /**
     * Process injected object annotated with the given annotation
     *
     * @param annotationType the annotation to handle
     * @param processor the processor
     * @param <A> type of annotation to process
     * @param <V> type of the injected value
     * @param <R> return type
     * @param <E> type of thrown exception
     */
    <A extends Annotation, V, R, E extends Exception> void processAnnotated(Class<A> annotationType, ThrowingQuadFunction<A, Parameter, V, Object[], R, E> processor);

    /**
     * Process injected of the given type and annotated with the given annotation
     *
     * @param annotationType the annotation to handle
     * @param type the type to handle
     * @param processor the processor
     * @param <A> type of annotation to process
     * @param <V> type of the injected value
     * @param <R> return type
     * @param <E> type of thrown exception
     */
    <A extends Annotation, V, R, E extends Exception> void processAnnotatedType(Class<A> annotationType, Class<V> type, ThrowingQuadFunction<A, Parameter, V, Object[], R, E> processor);

    /**
     * Fetch annotations assigned to the given parameter
     *
     * @param parameter the parameter to process
     * @return annotations assigned to the executable parameters
     */
    Annotation[] fetchAnnotations(Parameter parameter);

    /**
     * Fetch annotations assigned to the given executable
     *
     * @param executable the executable to process
     * @return annotations assigned to the executable parameters
     */
    Annotation[][] fetchAnnotations(Executable executable);

    /**
     * Get bind for the given parameter
     *
     * @param parameter the parameter to get bind for
     * @return the associated bind
     */
    Collection<InjectorResourceHandler<Annotation, Object, ?>> getHandler(Parameter parameter);

    /**
     * Get bind for the specified type or annotation
     *
     * @param requestedType the associated class with bind to search for
     * @return the wrapped bind
     */
    Option<InjectorResourceBind<Annotation>> getBind(Class<?> requestedType);

    /**
     * Create a fork of resources. The current resources will be used as a parent of a new instance.
     *
     * @return a forked instance of resources
     */
    InjectorResources fork();

    /**
     * Duplicate (clone) resources
     *
     * @return a duplicated instance of resources
     */
    InjectorResources duplicate();

}
