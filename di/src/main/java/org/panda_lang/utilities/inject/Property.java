package org.panda_lang.utilities.inject;

import panda.std.Option;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

/**
 * Represents injectable property. Supported implementations:
 * <ul>
 *     <li>fields</li>
 *     <li>parameters</li>
 * </ul>
 */
public interface Property {

    /**
     * Get associated field if present
     *
     * @return the associated field
     */
    default Option<Field> getField() {
        return Option.none();
    }

    /**
     * Get associated parameter if present
     *
     * @return the associated parameter
     */
    default Option<Parameter> getParameter() {
        return Option.none();
    }

    /**
     * Get the specific annotation
     *
     * @param annotation the type of annotation to search for
     * @param <A> the annotation type
     * @return annotation instance if found, otherwise null
     */
    <A extends Annotation> A getAnnotation(Class<A> annotation);

    /**
     * Get annotations assigned to the property
     *
     * @return the annotations of property
     */
    Annotation[] getAnnotations();

    /**
     * Get the generic signature used by the property
     *
     * @return the parametrized type of property
     */
    Type getParametrizedType();

    /**
     * Get a property type
     *
     * @return the type of property
     */
    Class<?> getType();

    /**
     * Get property name
     *
     * @return the name of property
     */
    String getName();

}
