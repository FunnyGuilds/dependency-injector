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

import org.jetbrains.annotations.NotNull;
import panda.std.function.ThrowingSupplier;
import panda.std.function.ThrowingTriFunction;
import panda.std.function.TriFunction;
import java.lang.annotation.Annotation;
import java.util.function.Supplier;

public interface Bind<A extends Annotation> extends Comparable<Bind<A>> {

    /**
     * Assign an object to the bind
     *
     * @param value the instance to assign
     */
    void assignInstance(Object value);

    /**
     * Assign value supplier to the bind
     *
     * @param valueSupplier the supplier to assign
     */
    void assignInstance(Supplier<?> valueSupplier);

    /**
     * Assign value supplier to the bind which can throw an exception
     *
     * @param valueSupplier the supplier to assign
     */
    void assignThrowingInstance(ThrowingSupplier<?, ? extends Exception> valueSupplier);

    /**
     * Assign lazy instance to the bind, that will be initialized on the first call and cached
     *
     * @param valueSupplier the supplier to assign
     */
    void assignLazyInstance(Supplier<?> valueSupplier);

    /**
     * Assign custom handler to the bind
     *
     * @param handler the handler which accepts type of parameter and bind type as arguments
     */
    void assignHandler(TriFunction<Property, A, Object[], ?> handler);

    /**
     * Assign custom handler to the bind which can throw an exception
     *
     * @param handler the handler which accepts type of parameter and bind type as arguments
     */
    void assignThrowingHandler(ThrowingTriFunction<Property, A, Object[], ?, ? extends Exception> handler);

    /**
     * Get the value of bind for the required (parameter) type and instance of a bind type
     *
     * @param required the required return type
     * @param annotation instance of bind generic type
     * @param injectorArgs custom arguments for injector, used by custom handlers
     * @return the result value
     * @throws Exception if anything wrong during obtaining value
     */
    Object getValue(Property required, A annotation, Object... injectorArgs) throws Exception;

    /**
     * Get an associated type with the bind
     *
     * @return the associated type
     */
    Class<?> getAssociatedType();

    /**
     * Get a data type
     *
     * @return the data type
     */
    Class<?> getDataType();

    @Override
    default int compareTo(@NotNull Bind bind) {
        return Integer.compare(InjectorResourceBindType.of(getAssociatedType()).getPriority(), InjectorResourceBindType.of(bind.getAssociatedType()).getPriority());
    }

}
