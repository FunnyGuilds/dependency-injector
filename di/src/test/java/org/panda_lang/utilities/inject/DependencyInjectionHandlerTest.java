package org.panda_lang.utilities.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.panda_lang.utilities.inject.annotations.Inject;
import org.panda_lang.utilities.inject.annotations.Injectable;
import org.panda_lang.utilities.inject.annotations.PostConstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DependencyInjectionHandlerTest {

    @Injectable
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Custom { }

    @Injectable
    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotation { }

    private static class Service1 {

        @Inject
        @Custom
        public String fieldOne;

        @Inject
        public int fieldTwo;

        @Inject
        @TestAnnotation
        public int fieldThree;

        @PostConstruct
        public void construct() {
            assertEquals("HelloWorld", this.fieldOne);

            assertEquals(7, this.fieldTwo);
            assertEquals(2, this.fieldThree);
        }

    }

    @Test
    void shouldCreateInstance() {
        Injector injector = DependencyInjection.createInjector(resources -> {
            resources.annotatedWith(Custom.class).assignHandler((type, annotation, args) -> "HelloWorld");
            resources.on(int.class).assignHandler((type, annotation, args) -> {
                TestAnnotation testAnnotation = type.getAnnotation(TestAnnotation.class);
                if (testAnnotation == null) {
                    return 7;
                }
                return 2;
            });
        });

        injector.newInstanceWithFields(Service1.class);
    }

    private static class Service2 {

        @Inject
        public String fieldOne;

    }

    @Test
    void shouldNotCreateInstanceAndFail() {
        Injector injector = DependencyInjection.createInjector(resources -> {
            resources.on(String.class).assignThrowingHandler((type, annotation, args) -> {
                throw new Exception("Failed");
            });
        });
        assertThrows(DependencyInjectionException.class, () -> injector.newInstanceWithFields(Service2.class));
    }

}
