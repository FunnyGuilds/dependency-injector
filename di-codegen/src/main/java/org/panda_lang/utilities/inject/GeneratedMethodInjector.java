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

import panda.std.Pair;
import panda.utilities.ArrayUtils;
import panda.utilities.ClassUtils;
import panda.utilities.ObjectUtils;
import panda.utilities.StringUtils;
import panda.utilities.text.Joiner;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public final class GeneratedMethodInjector implements MethodInjector {

    private static final Object[] EMPTY = new Object[0];

    private static final AtomicInteger ID = new AtomicInteger();

    private final InjectorProcessor processor;
    private final Method method;
    private final BiFunction<Object, Object[], Object> function;
    private final InjectorCache cache;
    private final boolean empty;

    GeneratedMethodInjector(InjectorProcessor processor, Method method) throws Exception {
        this.processor = processor;
        this.method = method;
        this.function = generate(method);
        this.cache = InjectorCache.of(processor, method);
        this.empty = method.getParameterCount() == 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T invoke(Object instance, Object... injectorArgs) throws Exception {
        return (T) function.apply(instance, empty
                ? EMPTY
                : processor.fetchValues(cache, injectorArgs));
    }

    private static BiFunction<Object, Object[], Object> generate(Method method) throws Exception {
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalStateException(method + " has to be public");
        }

        Class<?> declaringClass = method.getDeclaringClass();

        if (!Modifier.isPublic(declaringClass.getModifiers())) {
            throw new IllegalStateException(declaringClass + " has to be public");
        }

        StringBuilder body = new StringBuilder();
        body.append(declaringClass.getName()).append(" instance = (").append(declaringClass.getName()).append(") $1;\n");
        body.append(Object.class.getName()).append("[] array = (").append(Object.class.getName()).append("[]) $2;\n");

        Class<?>[] parameterTypes = method.getParameterTypes();

        for (int index = 0; index < parameterTypes.length; index++) {
            Class<?> parameterType = parameterTypes[index];
            String type = parameterType.getName();

            if (parameterType.isArray()) {
                Pair<Class<?>, Integer> baseClass = ArrayUtils.getBaseClassWithDimensions(parameterType);
                type = baseClass.getFirst().getName() + StringUtils.repeated(baseClass.getSecond(), "[]");
            }

            body.append(type).append(" arg").append(index).append(" = ((");

            // Auto-boxing impl
            if (parameterType.isPrimitive()) {
                Class<?> objectType = ClassUtils.getNonPrimitiveClass(parameterType);
                body.append(objectType.getName()).append(") array[").append(index).append("]).").append(type).append("Value();\n");
            }
            else {
                body.append(type).append(") array[").append(index).append("]);\n");
            }
        }

        Class<?> returnType = method.getReturnType();
        boolean isVoid = method.getReturnType() == void.class;
        Class<?> objectType = ClassUtils.getNonPrimitiveClass(returnType);

        if (!isVoid) {
            body.append("return ");

            // Auto-boxing impl
            if (returnType.isPrimitive()) {
                body.append("new ").append(objectType.getName()).append("(");
            }
        }

        body.append("instance.").append(method.getName())
                .append("(")
                .append(Joiner.on(", ").join(parameterTypes, (index, value) -> "arg" + index))
                .append(")");

        // Auto-boxing impl
        if (!isVoid && returnType.isPrimitive()) {
            body.append(")");
        }

        body.append(";");

        String name = Injector.class.getPackage().getName() + ".PandaDI" + ID.incrementAndGet() + method.getDeclaringClass().getSimpleName() + method.getName();
        Class<?> type = new FunctionGenerator(name, BiFunction.class, new LinkedHashMap<>(), body.toString()).generate(Injector.class);

        return ObjectUtils.cast(type.newInstance());
    }

    @Override
    public Method getMethod() {
        return method;
    }

}
