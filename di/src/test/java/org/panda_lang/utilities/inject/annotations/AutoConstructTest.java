package org.panda_lang.utilities.inject.annotations;

import org.junit.jupiter.api.Test;
import org.panda_lang.utilities.inject.DependencyInjection;
import org.panda_lang.utilities.inject.Injector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class AutoConstructTest {

    private static class Service {

        @AutoConstruct
        private Repository repository;

        @PostConstruct
        private void construct() {
            assertNotNull(this.repository);
        }

        public void testMethod(@AutoConstruct DataProvider dataProvider) {
            assertNotNull(dataProvider);
        }

    }

    private static class Repository {

        @AutoConstruct
        private DataProvider dataProvider;

        @Inject
        private String testValue;

        @PostConstruct
        private void construct() {
            assertEquals("TestString", this.testValue);
            assertNotNull(this.dataProvider);
        }

    }

    private static class DataProvider {

        @Inject
        private String testValue;

        @PostConstruct
        private void construct() {
            assertEquals("TestString", this.testValue);
        }

    }

    @Test
    void shouldCreateInstances() throws Throwable {
        Injector injector = DependencyInjection.createInjector(resources -> {
            resources.on(String.class).assignInstance("TestString");
        });

        injector.newInstanceWithFields(DataProvider.class);
        injector.newInstanceWithFields(Repository.class);
        injector.newInstanceWithFields(Service.class);
    }

    @Test
    void shouldRunMethod() throws Throwable {
        Injector injector = DependencyInjection.createInjector(resources -> {
            resources.on(String.class).assignInstance("TestString");
        });

        Service service = injector.newInstanceWithFields(Service.class);
        injector.invokeMethod(Service.class.getMethod("testMethod", DataProvider.class), service);
    }

}
