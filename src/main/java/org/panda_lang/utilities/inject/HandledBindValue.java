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

import panda.std.function.TriFunction;
import java.lang.annotation.Annotation;

final class HandledBindValue<A extends Annotation> implements BindValue<A> {

    private final TriFunction<Property, A, Object[], ?> handler;

    HandledBindValue(TriFunction<Property, A, Object[], ?> handler) {
        this.handler = handler;
    }

    @Override
    public Object getValue(Property required, A annotation, Object... injectorArgs) {
        return handler.apply(required, annotation, injectorArgs);
    }

}
