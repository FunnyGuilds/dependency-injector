package org.panda_lang.utilities.inject;

import java.lang.reflect.Method;

public interface MethodInjectorFactory {

    MethodInjector createMethodInjector(InjectorProcessor processor, Method method) throws Exception;

}
