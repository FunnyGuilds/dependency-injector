package org.panda_lang.utilities.inject.shared;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;

public final class AnnotationUtils {

    private AnnotationUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A instanceAnnotation(Class<A> annotationClass) {
        return (A) Proxy.newProxyInstance(annotationClass.getClassLoader(), new Class[] { annotationClass }, (proxy, method, args) -> {
            if (method.getName().equals("annotationType")) {
                return annotationClass;
            }

            if (method.getName().equals("toString")) {
                return "@" + annotationClass.getName() + "()";
            }

            if (method.getName().equals("hashCode")) {
                return 0;
            }

            if (method.getName().equals("equals")) {
                return proxy == args[0];
            }

            throw new UnsupportedOperationException("Unsupported method: " + method);
        });
    }

}
