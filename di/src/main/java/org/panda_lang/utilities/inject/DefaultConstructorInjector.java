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

import panda.utilities.ObjectUtils;
import java.lang.reflect.Constructor;

final class DefaultConstructorInjector<T> implements ConstructorInjector<T> {

    private static final Object[] EMPTY = new Object[0];

    private final InjectorProcessor processor;
    private final Constructor<T> constructor;
    private final boolean empty;
    private final InjectorCache cache;

    DefaultConstructorInjector(InjectorProcessor processor, Constructor<T> constructor) {
        this.processor = processor;
        this.constructor = constructor;
        constructor.setAccessible(true);
        this.empty = constructor.getParameterCount() == 0;
        this.cache = InjectorCache.of(processor, constructor);
    }

    @Override
    public T newInstance(Object... injectorArgs) throws Exception {
        return this.constructor.newInstance(
                this.empty
                        ? EMPTY
                        : this.processor.fetchValues(this.cache, injectorArgs)
        );
    }

    @Override
    public Constructor<T> getConstructor() {
        return this.constructor;
    }

}