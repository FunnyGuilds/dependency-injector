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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.panda_lang.utilities.inject.annotations.Injectable;

public final class DependencyInjectionUtils {

    private DependencyInjectionUtils() { }

    /**
     * Check if annotation is available at runtime and is annotated by {@link Injectable} annotation
     *
     * @param annotation the annotation to check
     * @param <T> annotation type
     * @return the tested annotation
     * @throws DependencyInjectionException when:
     *  <ul>
     *      <li>the given class is not an annotation</li>
     *      <li>annotation is not marked as @{@link Injectable}</li>
     *      <li>retention policy is not defined or its value is other than the {@link RetentionPolicy#RUNTIME} </li>
     *  </ul>
     */
    public static <T> Class<T> testAnnotation(Class<T> annotation) throws DependencyInjectionException {
        if (!annotation.isAnnotation()) {
            throw new DependencyInjectionException(annotation + " is not an annotation");
        }

        Retention retention = annotation.getAnnotation(Retention.class);
        if (retention == null) {
            throw new DependencyInjectionException(annotation + " has no specified retention policy");
        }

        if (retention.value() != RetentionPolicy.RUNTIME) {
            throw new DependencyInjectionException(annotation + " is not marked as runtime annotation");
        }

        if (annotation.getAnnotation(Injectable.class) == null) {
            throw new DependencyInjectionException(annotation + " is not marked as @Injectable");
        }

        return annotation;
    }

}
