package org.panda_lang.utilities.inject;

import org.junit.jupiter.api.Test;
import org.panda_lang.utilities.inject.annotations.Inject;
import org.panda_lang.utilities.inject.annotations.Injectable;
import org.panda_lang.utilities.inject.annotations.PostConstruct;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

public class DependencyInjectionHandlerTest {

    @Injectable
    @Retention(RetentionPolicy.RUNTIME)
    @interface Custom {
    }

    @Injectable
    @Retention(RetentionPolicy.RUNTIME)
    @interface AwesomeRandom {
    }

    public static class Service1 {
        @Inject
        @Custom
        public String fieldOne;

        @Inject
        public int fieldTwo;

        @Inject
        @AwesomeRandom
        public int fieldThree;

        @PostConstruct
        public void construct() {
            assertEquals("HelloWorld", this.fieldOne);

            assertEquals(7, this.fieldTwo);
            assertTrue(this.fieldTwo >= 2 && this.fieldTwo <= 10);
        }
    }

    @Test
    void shouldCreateInstance() {
        Injector injector = DependencyInjection.createInjector(resources -> {
            resources.annotatedWith(Custom.class).assignHandler((type, annotation, args) -> "HelloWorld");
            resources.on(int.class).assignHandler((type, annotation, args) -> {
                AwesomeRandom randomAnnotation = type.getAnnotation(AwesomeRandom.class);
                if (randomAnnotation == null) {
                    return 7;
                }
                return ThreadLocalRandom.current().nextInt(2, 10);
            });
        });

        injector.newInstanceWithFields(Service1.class);
    }

    public static class Service2 {
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
