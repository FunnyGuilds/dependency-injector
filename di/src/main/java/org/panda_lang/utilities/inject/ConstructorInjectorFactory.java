package org.panda_lang.utilities.inject;

import java.lang.reflect.Constructor;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
interface ConstructorInjectorFactory<T> {

    ConstructorInjector<T> createConstructorInjector(InjectorProcessor processor, Constructor<T> constructor);

}
