package org.panda_lang.panda.utilities.inject;

import org.junit.jupiter.api.Test;
import org.panda_lang.utilities.inject.DependencyInjection;
import org.panda_lang.utilities.inject.Injector;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class DependencyInjectionInstancesTest {

    @Test
    void shouldInjectInstances() throws Throwable {
        Injector injector = DependencyInjection.createInjector();

        // some logic, a few hours later...

        injector.getResources().on(Custom.class).assignInstance(new CustomImpl()); // singleton
        injector.getResources().on(Bean.class).assignInstance(Bean::new); // new instance per call

        Service service = injector.forConstructor(Service.class).newInstance();
        assertNotNull(service);
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
