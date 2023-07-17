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
import java.util.function.Supplier;
import panda.std.function.ThrowingSupplier;

final class StaticBindValue<A extends Annotation> implements BindValue<A> {

    private final ThrowingSupplier<?, ? extends Exception> valueSupplier;

    StaticBindValue(Object value) {
        this((ThrowingSupplier<?, ? extends Exception>) () -> value);
    }

    StaticBindValue(ThrowingSupplier<?, ? extends Exception> valueSupplier) {
        this.valueSupplier = valueSupplier;
    }

    StaticBindValue(Supplier<?> valueSupplier) {
        this.valueSupplier = valueSupplier::get;
    }

    @Override
    public Object getValue(Property required, A annotation, Object... injectorArgs) throws Exception {
        return valueSupplier.get();
    }

}
