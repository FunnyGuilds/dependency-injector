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

    private static final AtomicInteger ID = new AtomicInteger();

    private final InjectorProcessor processor;
    private final ConstructorInjector<T> constructorInjector;
    private final Class<?> declaringClass;

    private final BiConsumer<Object, Object[]> generated;

    CodegenFieldsInjector(InjectorProcessor processor, ConstructorInjector<T> constructorInjector) throws Exception {
        this.processor = processor;
        this.constructorInjector = constructorInjector;
        this.declaringClass = constructorInjector.getConstructor().getDeclaringClass();

        this.generated = generate(this.declaringClass);
    }

    @Override
    public T newInstance(Object... injectorArgs) throws Exception {
        T instance = this.constructorInjector.newInstance(injectorArgs);

        Field[] fields = ClassCache.getInjectorFields(this.declaringClass);
        Object[] values = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            values[i] = this.processor.fetchValue(new PropertyField(fields[i]), injectorArgs);
        }
        this.generated.accept(instance, values);

        return instance;
    }

    @Override
    public Constructor<T> getConstructor() {
        return this.constructorInjector.getConstructor();
    }

    private static BiConsumer<Object, Object[]> generate(Class<?> declaringClass) throws Exception {
        if (!Modifier.isPublic(declaringClass.getModifiers())) {
            throw new IllegalStateException(declaringClass + " has to be public");
        }

        Field[] fields = ClassCache.getInjectorFields(declaringClass);
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (!Modifier.isPublic(modifiers)) {
                throw new IllegalStateException(field + " has to be public");
            } else if (Modifier.isFinal(modifiers)) {
                throw new IllegalStateException(field + " cannot be final");
            }
        }

        ByteBuddy byteBuddy = new ByteBuddy();
        DynamicType.Unloaded<?> classPackage = byteBuddy
                .makePackage(declaringClass.getPackage().getName())
                .make();


        /*DynamicType.Builder.MethodDefinition.ExceptionDefinition<Object> init = byteBuddy.subclass(Object.class)
                .implement(GeneratedConsumer.class)
                .name(declaringClass.getName() + "$init$" + ID.incrementAndGet())
                .method(ElementMatchers.named("accept"))
                .withoutCode()
                .defineMethod("init", void.class, Modifier.PUBLIC)
                .withParameters(ArrayUtils.merge(new Type[]{declaringClass}, Arrays.stream(fields).map(Field::getType).toArray(Class[]::new)))
                .intercept(FieldAccessor.ofField("instance").setsArgumentAt(0));

        DynamicType.Unloaded<Object> unloaded = init
                .intercept(MethodDelegation.to(FieldAccessor.ofField("instance")))
                .make();*/

        return (instance, values) -> {

        };
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
    public interface GeneratedConsumer extends BiConsumer<Object, Object[]> { }

}
