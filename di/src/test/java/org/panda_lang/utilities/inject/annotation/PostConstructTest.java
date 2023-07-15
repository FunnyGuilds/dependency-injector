package org.panda_lang.utilities.inject.annotation;

import org.junit.jupiter.api.Test;
import org.panda_lang.utilities.inject.DependencyInjection;
import org.panda_lang.utilities.inject.Injector;
import org.panda_lang.utilities.inject.annotations.Inject;
import org.panda_lang.utilities.inject.annotations.PostConstruct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PostConstructTest {

    static class Service extends AbstractService {

        @Inject
        public String value;

        private long longValue;

        @PostConstruct
        private void construct() {
            assertEquals("Hello Field", value);
            longValue = 123456789L;
        }

    }

    abstract static class AbstractService {

        @Inject
        private float abstractValue;

        protected boolean abstractBoolean;

        @PostConstruct
        public void abstractConstruct(int methodParameter) {
            assertEquals(1.2f, abstractValue);
            assertEquals(2022, methodParameter);
            abstractBoolean = true;
        }

    }

    @Test
    void shouldRunPostConstructMethods() throws Throwable {
        Injector injector = DependencyInjection.createInjector(resources -> {
            resources.on(String.class).assignInstance("Hello Field");
            resources.on(float.class).assignInstance(1.2f);
            resources.on(int.class).assignInstance(2022);
        });

        Service service = injector.newInstanceWithFields(Service.class);
        assertEquals(123456789L, service.longValue);
        assertTrue(service.abstractBoolean);
    }

}
