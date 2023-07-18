package org.panda_lang.utilities.inject;

import java.security.InvalidParameterException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class DependencyInjectionInstancesTest {

    @Test
    void shouldInjectInstances() {
        Injector injector = DependencyInjection.createInjector();

        // some logic, a few hours later...

        injector.getResources().on(Custom.class).assignInstance(new CustomImpl()); // singleton
        injector.getResources().on(Bean.class).assignInstance(Bean::new); // new instance per call

        Service service = injector.newInstance(Service.class);
        assertNotNull(service);
    }

    @Test
    void shouldNotInjectInstances() {
        Injector injector = DependencyInjection.createInjector(resources -> {
            resources.on(Custom.class).assignThrowingInstance(() -> {
                throw new Exception("Failed");
            });
            resources.on(Bean.class).assignThrowingInstance(() -> {
                throw new Exception("Failed");
            });
        });

        assertThrows(InvalidParameterException.class, () -> injector.forConstructor(InvalidClass.class).newInstance(), "Class has contain one and only one constructor");
        assertThrows(DependencyInjectionException.class, () -> injector.newInstance(Service.class));
    }

    private static class Bean { }

    private interface Custom { }
    private static class CustomImpl implements Custom { }

    private static class Service {
        public Service(Bean bean, Custom custom) {
            assertNotNull(bean);
            assertNotNull(custom);
        }
    }

    private static class InvalidClass { // 2 constructors (only 1 is allowed)
        public InvalidClass(Bean bean, Custom custom) {
            assertNotNull(bean);
            assertNotNull(custom);
        }

        public InvalidClass(Bean bean) {
            assertNotNull(bean);
        }
    }

}
