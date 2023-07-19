package org.panda_lang.utilities.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.jupiter.api.Test;
import org.panda_lang.utilities.inject.annotations.Inject;
import org.panda_lang.utilities.inject.annotations.Injectable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DependencyInjectionFieldsTest {

    @Injectable
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Custom {}

    private static class Service extends AbstractService {

        public final boolean throughConstructor;
        public final String customArgument;

        @Inject
        public String fieldOne;
        @Inject
        public Integer fieldTwo;

        Service(boolean constructor, @Custom String customArgument) {
            this.throughConstructor = constructor;
            this.customArgument = customArgument;
        }

        public String serve() {
            assertTrue(throughConstructor);
            assertEquals("custom argument", customArgument);
            assertEquals("Hello Field", fieldOne);
            assertEquals(7, fieldTwo);

            return fieldOne + " " + fieldTwo;
        }

    }

    private static abstract class AbstractService {

        @Inject
        protected float abstractFieldOne;
        @Inject
        private long abstractFieldTwo;

        public String serveAbstract() {
            assertEquals(1.2f, abstractFieldOne);
            assertEquals(254623242914889729L, abstractFieldTwo);

            return abstractFieldOne + " " + abstractFieldTwo;
        }

    }

    @Test
    void shouldInjectFields() {
        Injector injector = DependencyInjection.createInjector(resources -> {
            resources.on(boolean.class).assignInstance(true);
            resources.on(String.class).assignInstance("Hello Field");
            resources.on(Integer.class).assignInstance(7);
            resources.on(float.class).assignInstance(1.2f);
            resources.on(long.class).assignInstance(254623242914889729L);
            resources.annotatedWithTested(Custom.class).assignHandler((property, custom, objects) -> objects[0].toString());
        });

        Service service = injector.newInstanceWithFields(Service.class, "custom argument");
        assertEquals("Hello Field 7", service.serve());
        assertEquals("1.2 254623242914889729", service.serveAbstract());
    }

}
