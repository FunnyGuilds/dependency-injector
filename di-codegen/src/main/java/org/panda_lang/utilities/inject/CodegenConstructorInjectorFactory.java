package org.panda_lang.utilities.inject;

import java.lang.reflect.Constructor;

public class CodegenConstructorInjectorFactory<T> implements ConstructorInjectorFactory<T> {

    @Override
    public ConstructorInjector<T> createConstructorInjector(InjectorProcessor processor, Constructor<T> constructor)  {
        return new CodegenConstructorInjector<>(processor, constructor);
    }

}
