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

import org.jetbrains.annotations.Nullable;
import panda.std.Option;
import panda.std.function.ThrowingQuadFunction;
import panda.std.function.ThrowingTriFunction;
import java.lang.annotation.Annotation;

final class DefaultBindHandler<A extends Annotation, V, R, E extends Exception> implements BindHandler<A, V, R> {

    private final ThrowingQuadFunction<A, Property, V, Object[], R, E> processor;
    @SuppressWarnings("OptionUsedAsFieldOrParameterType")
    private final Option<Class<A>> annotationType;

    DefaultBindHandler(@Nullable Class<A> annotation, ThrowingQuadFunction<A, Property, V, Object[], R, E> processor) {
        this.processor = processor;
        this.annotationType = Option.of(annotation);
    }

    DefaultBindHandler(@Nullable Class<A> annotation, ThrowingTriFunction<Property, V, Object[], R, E> processor) {
        this(annotation, (_annotation, parameter, value, injectorArgs) -> processor.apply(parameter, value, injectorArgs));
    }

    @Override
    public R process(Property required, A annotation, V value, Object... injectorArgs) throws Exception {
        return processor.apply(annotation, required, value, injectorArgs);
    }

    @Override
    public Option<Class<A>> getAnnotation() {
        return annotationType;
    }

}
