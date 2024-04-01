package org.panda_lang.utilities.inject;

import java.lang.reflect.Constructor;
import org.jetbrains.annotations.ApiStatus;

public interface ConstructorInjector<T> {

    /**
     * Create a new instance of the specified constructor
     *
     * @param injectorArgs arguments for injector
     * @return new instance of the specified type
     * @throws Exception if anything happens during the instance creation
     */
    T newInstance(Object... injectorArgs) throws Exception;

    /**
     * @return constructor that will be used to create new instance
     */
    @ApiStatus.Internal
    Constructor<T> getConstructor();

}
