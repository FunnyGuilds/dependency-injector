package org.panda_lang.utilities.inject;

import java.lang.reflect.Method;
import org.jetbrains.annotations.ApiStatus;

public interface MethodInjector {

    /**
     * Invoke specific method with the given instance
     *
     * @param instance     the instance to use
     * @param injectorArgs arguments for injector
     * @param <T>          type of return value
     * @return returned value by the method
     * @throws Exception if anything happens during the method invocation
     */
    <T> T invoke(Object instance, Object... injectorArgs) throws Exception;

    /**
     * @return method that will be invoked by this injector
     */
    @ApiStatus.Internal
    Method getMethod();

}
