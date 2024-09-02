package org.panda_lang.utilities.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import panda.utilities.ObjectUtils;

@ApiStatus.Internal
final class CodegenCache {

    private static final AtomicInteger CONSTRUCTORS_ID = new AtomicInteger();
    private static final Map<Constructor<?>, Function<Object[], Object>> CONSTRUCTOR_INVOKERS = new ConcurrentHashMap<>();

    private static final AtomicInteger FIELDS_ID = new AtomicInteger();
    private static final Map<Class<?>, BiConsumer<Object, Object[]>> FIELD_INVOKERS = new ConcurrentHashMap<>();

    private static final AtomicInteger METHODS_ID = new AtomicInteger();
    private static final Map<Method, BiFunction<Object, Object[], Object>> METHOD_INVOKERS = new ConcurrentHashMap<>();

    private CodegenCache() {
    }

    public static Function<Object[], Object> getConstructorInvoker(Constructor<?> constructor) {
        return CONSTRUCTOR_INVOKERS.computeIfAbsent(constructor, key -> {
            Class<?> declaringClass = validateExecutable(constructor);

            ByteBuddy byteBuddy = new ByteBuddy();
            Class<?> loaded = byteBuddy.subclass(Object.class)
                    .implement(GeneratedFunction.class)
                    .name(declaringClass.getName() + "$" + constructor.getName() + "$fields$" + CONSTRUCTORS_ID.incrementAndGet())
                    .method(ElementMatchers.named("apply"))
                    .intercept(MethodCall.construct(constructor)
                            .withArgumentArrayElements(0)
                            .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                    .make()
                    .include(preparePackage(byteBuddy, declaringClass.getPackage()))
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

    public static BiConsumer<Object, Object[]> getFieldInvoker(Class<?> declaringClass, Field[] fields) {
        return FIELD_INVOKERS.computeIfAbsent(declaringClass, key -> {
            ByteBuddy byteBuddy = new ByteBuddy();

            Implementation.Composable function = MethodCall.run(() -> {});
            int i = 0;
            for (Field field : fields) {
                function.andThen(FieldAccessor.of(field)
                                .setsArgumentAt(i + 1));
                i++;
            }

            Class<?> loaded = byteBuddy.subclass(Object.class)
                    .implement(GeneratedBiConsumer.class)
                    .name(declaringClass.getName() + "$" + FIELDS_ID.incrementAndGet())
                    .method(ElementMatchers.named("apply"))
                    .intercept(MethodCall.invoke(declaringClass.getDeclaredFields()[0].getGetMethod())
                            .onArgument(0)
                            .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                    .make()
                    .include(preparePackage(byteBuddy, declaringClass.getPackage()))
                    .load(declaringClass.getClassLoader())
                    .getLoaded();

            try {
                return ObjectUtils.cast(loaded.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException ex) {
                throw new DependencyInjectionException("Failed to generate codegen field invoker", ex);
            }
        });
    }

    public static BiFunction<Object, Object[], Object> getMethodInvoker(Method method) {
        return METHOD_INVOKERS.computeIfAbsent(method, key -> {
            Class<?> declaringClass = validateExecutable(method);

            ByteBuddy byteBuddy = new ByteBuddy();
            Class<?> loaded = byteBuddy.subclass(Object.class)
                    .implement(GeneratedBiFunction.class)
                    .name(declaringClass.getName() + "$" + method.getName() + "$" + METHODS_ID.incrementAndGet())
                    .method(ElementMatchers.named("apply"))
                    .intercept(MethodCall.invoke(method)
                            .onArgument(0)
                            .withArgumentArrayElements(1)
                            .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                    .make()
                    .include(preparePackage(byteBuddy, declaringClass.getPackage()))
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

    @NotNull
    private static Class<?> validateExecutable(Executable constructor) {
        Class<?> declaringClass = constructor.getDeclaringClass();
        if (!Modifier.isPublic(declaringClass.getModifiers())) {
            throw new IllegalStateException(declaringClass + " has to be public");
        }

        if (!Modifier.isPublic(constructor.getModifiers())) {
            throw new IllegalStateException(constructor + " has to be public");
        }
        return declaringClass;
    }

    @NotNull
    private static DynamicType.Unloaded<?> preparePackage(ByteBuddy byteBuddy, Package declaringClass) {
        return byteBuddy
                .makePackage(declaringClass.getName())
                .make();
    }

    @ApiStatus.Internal
    @FunctionalInterface
    public interface GeneratedFunction extends Function<Object[], Object> {

    }

    @ApiStatus.Internal
    @FunctionalInterface
    public interface GeneratedBiFunction extends BiFunction<Object, Object[], Object> {

    }

    @ApiStatus.Internal
    @FunctionalInterface
    public interface GeneratedBiConsumer extends BiConsumer<Object, Object[]> {

    }

}
