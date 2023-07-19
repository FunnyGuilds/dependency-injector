package org.panda_lang.utilities.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MethodsInvocationTest {

    private static class TestClass {

        private String testString;
        private int testInt = 0;

        public void testMethod() {
            this.testString = "test";
        }

        @TestAnnotation
        public void annotatedMethod() {
            this.testInt += 2;
        }

        @TestAnnotation
        public void annotatedMethod(int value) {
            this.testInt += value;
        }

        @TestAnnotation2
        public void annotatedMethod2() {
            throw new IllegalStateException("This method should not be invoked");
        }

        public void failMethod() {
            throw new RuntimeException();
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotation { }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotation2 { }

    @Test
    void shouldInvokeMethods() throws NoSuchMethodException {
        Injector injector = DependencyInjection.createInjector(resources -> {
            resources.on(int.class).assignInstance(3);
        });

        TestClass testClass = new TestClass();
        assertDoesNotThrow(() -> injector.invokeMethod(TestClass.class.getMethod("testMethod"), testClass));
        assertEquals("test", testClass.testString);

        assertDoesNotThrow(() -> injector.invokeAnnotatedMethods(TestAnnotation.class, testClass));
        assertEquals(5, testClass.testInt);
    }

    @Test
    void shouldNotInvokeMethods() throws NoSuchMethodException {
        Injector injector = DependencyInjection.createInjector(resources -> {
            resources.on(int.class).assignInstance(3);
        });

        TestClass testClass = new TestClass();
        assertThrows(DependencyInjectionException.class, () -> injector.invokeMethod(TestClass.class.getMethod("failMethod"), testClass));
    }

}
