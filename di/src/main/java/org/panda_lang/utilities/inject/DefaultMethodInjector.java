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
import java.lang.reflect.Method;

public final class DefaultMethodInjector implements MethodInjector {

    private final InjectorProcessor processor;
    private final Method method;
    private final InjectorCache cache;

    DefaultMethodInjector(InjectorProcessor processor, Method method) {
        this.processor = processor;
        this.method = method;
        method.setAccessible(true);
        this.cache = InjectorCache.of(processor, method);
    }

    /**
     * Invoke injector with the given instance
     *
     * @param instance the instance to use
     * @param injectorArgs arguments for injector
     * @param <T> type of return value
     * @return returned value
     * @throws Exception if anything happens in the evaluated method
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T invoke(Object instance, Object... injectorArgs) throws Exception {
        return ObjectUtils.cast(this.method.invoke(instance, this.processor.fetchValues(this.cache, injectorArgs)));
    }

    @Override
    public Method getMethod() {
        return method;
    }

}
