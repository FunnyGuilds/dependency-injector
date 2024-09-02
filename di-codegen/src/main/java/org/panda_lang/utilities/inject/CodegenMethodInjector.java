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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;
import panda.utilities.ObjectUtils;

final class CodegenMethodInjector implements MethodInjector {

    private static final Object[] EMPTY = new Object[0];

    private final InjectorProcessor processor;
    private final Method method;
    private final InjectorCache cache;
    private final boolean empty;

    private final BiFunction<Object, Object[], Object> generated;

    CodegenMethodInjector(InjectorProcessor processor, Method method) {
        this.processor = processor;
        this.method = method;
        this.cache = InjectorCache.of(processor, method);
        this.empty = method.getParameterCount() == 0;
        this.generated = CodegenCache.getMethodInvoker(method);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T invoke(Object instance, Object... injectorArgs) throws Exception {
        return (T) this.generated.apply(instance,
                this.empty
                        ? EMPTY
                        : this.processor.fetchValues(this.cache, injectorArgs)
        );
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

}
