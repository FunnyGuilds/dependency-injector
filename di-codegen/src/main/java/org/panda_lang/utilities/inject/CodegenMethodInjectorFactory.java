package org.panda_lang.utilities.inject;

import java.lang.reflect.Method;

public final class CodegenMethodInjectorFactory implements MethodInjectorFactory {

    @Override
    public MethodInjector createMethodInjector(InjectorProcessor processor, Method method) {
        return new CodegenMethodInjector(processor, method);
    }

}
