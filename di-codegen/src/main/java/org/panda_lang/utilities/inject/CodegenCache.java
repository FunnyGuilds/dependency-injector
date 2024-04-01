package org.panda_lang.utilities.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;
import org.jetbrains.annotations.ApiStatus;
import panda.utilities.ObjectUtils;

@ApiStatus.Internal
final class CodegenCache {

    private static final AtomicInteger CONSTRUCTORS_ID = new AtomicInteger();
    private static final Map<Constructor<?>, Function<Object[], Object>> CONSTRUCTOR_INVOKERS = new ConcurrentHashMap<>();

    private static final AtomicInteger METHODS_ID = new AtomicInteger();
    private static final Map<Method, BiFunction<Object, Object[], Object>> METHOD_INVOKERS = new ConcurrentHashMap<>();

    private CodegenCache() {
    }

    public static Function<Object[], Object> getConstructorInvoker(Constructor<?> constructor) {
        return CONSTRUCTOR_INVOKERS.computeIfAbsent(constructor, key -> {
            Class<?> declaringClass = constructor.getDeclaringClass();
            if (!Modifier.isPublic(declaringClass.getModifiers())) {
                throw new IllegalStateException(declaringClass + " has to be public");
            }

            if (!Modifier.isPublic(constructor.getModifiers())) {
                throw new IllegalStateException(constructor + " has to be public");
            }

            ByteBuddy byteBuddy = new ByteBuddy();
            DynamicType.Unloaded<?> classPackage = byteBuddy
                    .makePackage(declaringClass.getPackage().getName())
                    .make();

            Class<?> loaded = byteBuddy.subclass(Object.class)
                    .implement(GeneratedFunction.class)
                    .name(declaringClass.getName() + "$" + constructor.getName() + "$" + CONSTRUCTORS_ID.incrementAndGet())
                    .method(ElementMatchers.named("apply"))
                    .intercept(MethodCall.construct(constructor)
                            .withArgumentArrayElements(0)
                            .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                    .make()
                    .include(classPackage)
                    .load(declaringClass.getClassLoader())
                    .getLoaded();

            try {
                return ObjectUtils.cast(loaded.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException ex) {
                throw new DependencyInjectionException("Failed to generate codegen constructor invoker", ex);
            }
        });
    }

    public static BiFunction<Object, Object[], Object> getMethodInvoker(Method method) {
        return METHOD_INVOKERS.computeIfAbsent(method, key -> {
            Class<?> declaringClass = method.getDeclaringClass();
            if (!Modifier.isPublic(declaringClass.getModifiers())) {
                throw new IllegalStateException(declaringClass + " has to be public");
            }

            if (!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalStateException(method + " has to be public");
            }

            ByteBuddy byteBuddy = new ByteBuddy();
            DynamicType.Unloaded<?> classPackage = byteBuddy
                    .makePackage(declaringClass.getPackage().getName())
                    .make();

            Class<?> loaded = byteBuddy.subclass(Object.class)
                    .implement(GeneratedBiFunction.class)
                    .name(declaringClass.getName() + "$" + method.getName() + "$" + METHODS_ID.incrementAndGet())
                    .method(ElementMatchers.named("apply"))
                    .intercept(MethodCall.invoke(method)
                            .onArgument(0)
                            .withArgumentArrayElements(1)
                            .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                    .make()
                    .include(classPackage)
                    .load(declaringClass.getClassLoader())
                    .getLoaded();

            try {
                return ObjectUtils.cast(loaded.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException ex) {
                throw new DependencyInjectionException("Failed to generate codegen method invoker", ex);
            }
        });
    }

    @ApiStatus.Internal
    @FunctionalInterface
    public interface GeneratedFunction extends Function<Object[], Object> {

    }

    @ApiStatus.Internal
    @FunctionalInterface
    public interface GeneratedBiFunction extends BiFunction<Object, Object[], Object> {

    }

}
