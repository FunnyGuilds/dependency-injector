package org.panda_lang.utilities.inject;

import org.junit.jupiter.api.Test;
import org.panda_lang.utilities.inject.annotations.Inject;
import org.panda_lang.utilities.inject.annotations.PostConstruct;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class DependencyInjectionPostConstructTest {

    static class Service extends AbstractService {

        @Inject
        public String value;

        @PostConstruct
        private void construct() {
            assertEquals("Hello Field", value);
        }

    }

    abstract static class AbstractService {

        @Inject
        private float abstractValue;

        @PostConstruct
        public void abstractConstruct(float methodParameter) {
            assertEquals(1.2f, abstractValue);
            assertEquals(1.2f, methodParameter);
        }

    }

    @Test
    void shouldRunPostConstructMethods() throws Throwable {
        Injector injector = DependencyInjection.createInjector(resources -> {
            resources.on(String.class).assignInstance("Hello Field");
            resources.on(float.class).assignInstance(1.2f);
        });

        injector.newInstanceWithFields(Service.class);
    }

}
