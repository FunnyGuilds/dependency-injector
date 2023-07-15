package org.panda_lang.utilities.inject;

import org.junit.jupiter.api.Test;
import org.panda_lang.utilities.inject.annotations.AutoConstruct;
import org.panda_lang.utilities.inject.annotations.Inject;
import org.panda_lang.utilities.inject.annotations.PostConstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AutoConstructTest {

    static class Service {

        @AutoConstruct
        private Repository repository;

        @PostConstruct
        private void construct() {
            assertNotNull(this.repository);
        }

    }

    static class Repository {

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

    static class DataProvider {

        @Inject
        private String testValue;

        @PostConstruct
        private void construct() {
            assertEquals("TestString", this.testValue);
        }

    }

    @Test
    void shouldRunPostConstructMethods() throws Throwable {
        Injector injector = DependencyInjection.createInjector(resources -> {
            resources.on(String.class).assignInstance("TestString");
        });

        injector.newInstanceWithFields(Repository.class);
        injector.newInstanceWithFields(Service.class);
    }

}
