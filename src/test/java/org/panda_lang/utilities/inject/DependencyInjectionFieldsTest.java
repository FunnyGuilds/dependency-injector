package org.panda_lang.utilities.inject;

import org.junit.jupiter.api.Test;
import org.panda_lang.utilities.inject.annotations.Inject;
import org.panda_lang.utilities.inject.annotations.Injectable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.jupiter.api.Assertions.*;

final class DependencyInjectionFieldsTest {

    @Injectable
    @Retention(RetentionPolicy.RUNTIME)
    @interface Custom { }

    static class Service {

        private final boolean throughConstructor;
        private final String customArgument;

        @Inject
        private String fieldOne;
        @Inject
        private Integer fieldTwo;

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

    @Test
    void shouldInjectFields() throws Throwable {
        Injector injector = DependencyInjection.createInjector(resources -> {
            resources.on(boolean.class).assignInstance(true);
            resources.on(String.class).assignInstance("Hello Field");
            resources.on(Integer.class).assignInstance(7);
            resources.annotatedWithTested(Custom.class).assignHandler((property, custom, objects) -> objects[0].toString());
        });

        Service service = injector.forFields(Service.class).newInstance("custom argument");
        assertEquals("Hello Field 7", service.serve());
    }

}
