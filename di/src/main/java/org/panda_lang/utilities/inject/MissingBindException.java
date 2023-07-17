package org.panda_lang.utilities.inject;

public class MissingBindException extends DependencyInjectionException {

    MissingBindException(String message) {
        super(message);
    }

}
