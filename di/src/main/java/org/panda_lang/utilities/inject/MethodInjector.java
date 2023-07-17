package org.panda_lang.utilities.inject;

import java.lang.reflect.Method;

public interface MethodInjector {

    <T> T invoke(Object instance, Object... injectorArgs) throws Exception;

    Method getMethod();

}
