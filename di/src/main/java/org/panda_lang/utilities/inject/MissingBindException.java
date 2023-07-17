package org.panda_lang.utilities.inject;

/**
 * Specific exception thrown when the bind is missing
 */
public class MissingBindException extends DependencyInjectionException {

    MissingBindException(String message) {
        super(message);
    }

}
