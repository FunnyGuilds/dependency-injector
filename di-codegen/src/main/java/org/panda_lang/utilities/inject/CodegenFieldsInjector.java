package org.panda_lang.utilities.inject;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.matcher.ElementMatchers;
import panda.utilities.ArrayUtils;

public class CodegenFieldsInjector<T> implements FieldsInjector<T> {

    private static final Object[] EMPTY = new Object[0];

    private final InjectorProcessor processor;
    private final ConstructorInjector<T> constructorInjector;
    private final Class<?> declaringClass;
    private final Field[] fields;
    private final BiConsumer<Object, Object[]> generated;

    CodegenFieldsInjector(InjectorProcessor processor, ConstructorInjector<T> constructorInjector) {
        this.processor = processor;
        this.constructorInjector = constructorInjector;
        this.declaringClass = constructorInjector.getConstructor().getDeclaringClass();
        this.fields = ClassCache.getInjectorFields(this.declaringClass);
        this.generated = CodegenCache.getFieldInvoker(this.declaringClass, this.fields);
    }

    @Override
    public T newInstance(Object... injectorArgs) throws Exception {
        T instance = this.constructorInjector.newInstance(injectorArgs);

        Object[] values = new Object[this.fields.length];
        for (int i = 0; i < this.fields.length; i++) {
            values[i] = this.processor.fetchValue(new PropertyField(this.fields[i]), injectorArgs);
        }
        this.generated.accept(instance, values);

        return instance;
    }

    @Override
    public Constructor<T> getConstructor() {
        return this.constructorInjector.getConstructor();
    }

    public static class Interceptor {

        private final Field[] fields;

        public Interceptor(Field[] fields) {
            this.fields = fields;
        }

        public void intercept(@AllArguments Object[] args) throws IllegalAccessException {
            Object instance = args[0];
            Object[] values = Arrays.copyOfRange(args, 1, args.length);

            for (int i = 0; i < values.length; i++) {
                Field field = this.fields[i];
                field.setAccessible(true);
                field.set(instance, values[i]);
            }

        }

    }

    @FunctionalInterface
    public interface GeneratedConsumer extends BiConsumer<Object, Object[]> {

    }

}
