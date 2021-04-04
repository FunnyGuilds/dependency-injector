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

import org.panda_lang.utilities.commons.ObjectUtils;
import org.panda_lang.utilities.commons.function.TriFunction;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

class DefaultBind<A extends Annotation> implements Bind<A> {

    private final Class<?> associatedType;
    private final Class<?> dataType;
    private BindValue<A> value;

    DefaultBind(Class<?> associatedType) {
        this(associatedType, associatedType);
    }

    DefaultBind(Class<?> associatedType, Class<?> dataType) {
        if (ObjectUtils.areNull(associatedType, dataType)) {
            throw new IllegalArgumentException("Associated type cannot be null at the same time");
        }

        this.associatedType = associatedType;
        this.dataType = dataType;
    }

    private void with(BindValue<A> value) {
        this.value = value;
    }

    @Override
    public void assignInstance(Object value) {
        with(new StaticBindValue<>(value));
    }

    @Override
    public void assignInstance(Supplier<?> valueSupplier) {
        with(new StaticBindValue<>(valueSupplier));
    }

    @Override
    public void assignHandler(TriFunction<Property, A, Object[], ?> handler) {
        with(new HandledBindValue<>(handler));
    }

    @Override
    public Object getValue(Property required, A annotation, Object... injectedArgs) throws Exception {
        return value.getValue(required, annotation, injectedArgs);
    }

    @Override
    public Class<?> getAssociatedType() {
        return associatedType;
    }

    @Override
    public Class<?> getDataType() {
        return dataType;
    }

}
