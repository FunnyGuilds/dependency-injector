package org.panda_lang.utilities.inject;

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

        assertThrows(DependencyInjectionException.class, () -> injector.newInstance(Service.class));
    }

    public static class Bean { }

    public interface Custom { }
    static class CustomImpl implements Custom { }

    static class Service {
        public Service(Bean bean, Custom custom) {
            assertNotNull(bean);
            assertNotNull(custom);
        }
    }

}
