package org.panda_lang.utilities.inject;

import java.lang.reflect.Method;

public class CodegenMethodInjectorFactory implements MethodInjectorFactory {

    @Override
    public MethodInjector createMethodInjector(InjectorProcessor processor, Method method) throws Exception {
        return new GeneratedMethodInjector(processor, method);
    }

}
