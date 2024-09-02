package org.panda_lang.utilities.inject;

import java.lang.reflect.Method;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
interface MethodInjectorFactory {

    MethodInjector createMethodInjector(InjectorProcessor processor, Method method);

}
