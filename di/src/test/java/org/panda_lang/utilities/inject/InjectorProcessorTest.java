package org.panda_lang.utilities.inject;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import org.panda_lang.utilities.inject.annotations.AutoConstruct;
import org.panda_lang.utilities.inject.annotations.Injectable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class InjectorProcessorTest {

    private final Injector injector = DependencyInjection.createInjector(resources -> {
        resources.on(int.class).assignInstance(2023);
        resources.on(String.class).assignInstance("Test");
        resources.annotatedWithTested(TestAnnotation.class).assignInstance("TestAnnotation");
    });
    private final InjectorProcessor processor = new InjectorProcessor(this.injector);

    @Injectable
    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotation {}

    private static class TestClass {

        private int intProp;
        private float floatProp;
        private String stringProp;
        @TestAnnotation
        private String stringAnnotatedProp;
        @AutoConstruct
        private Object autoConstructProp;

        public TestClass(int intProp, @TestAnnotation String stringAnnotatedProp) {}

        public TestClass(int intProp, float floatProp) {}

        private static PropertyField getProperty(String fieldName) throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField(fieldName);
            return new PropertyField(field);
        }

    }

    @Test
    void shouldFetchAnnotations() {
        Annotation[] annotations = this.processor.fetchAnnotations(TestClass.class.getConstructors()[0]);
        assertEquals(2, annotations.length);
        assertNull(annotations[0]);
        assertTrue(annotations[1] instanceof TestAnnotation);
    }

    @Test
    void shouldFetchBinds() throws Exception {
        // Fetch single
        Property intProperty = TestClass.getProperty("intProp");
        assertDoesNotThrow(() -> this.processor.fetchBind(null, intProperty));

        Property stringProperty = TestClass.getProperty("stringProp");
        Bind<Annotation> stringBind = assertDoesNotThrow(() -> this.processor.fetchBind(null, stringProperty));
        assertEquals("Test", stringBind.getValue(stringProperty, null));

        Property stringAnnotatedProperty = TestClass.getProperty("stringAnnotatedProp");
        TestAnnotation testAnnotation = stringAnnotatedProperty.getAnnotation(TestAnnotation.class);
        Bind<Annotation> annotatedStringBind = assertDoesNotThrow(() -> this.processor.fetchBind(testAnnotation, stringAnnotatedProperty));
        assertEquals("TestAnnotation", annotatedStringBind.getValue(stringAnnotatedProperty, testAnnotation));

        Property autoConstructProperty = TestClass.getProperty("autoConstructProp");
        Bind<Annotation> autoConstructBind = assertDoesNotThrow(() -> this.processor.fetchBind(null, autoConstructProperty));
        assertEquals(this.processor.getAutoConstructBind(), autoConstructBind);

        // Fetch multiple
        Constructor<?> testConstructor = TestClass.class.getConstructors()[0];
        Annotation[] annotations = this.processor.fetchAnnotations(testConstructor);
        Bind<Annotation>[] fetchedBinds = assertDoesNotThrow(() -> this.processor.fetchBinds(annotations, testConstructor));

        assertEquals(2, fetchedBinds.length);
        assertNotNull(fetchedBinds[0]);
        assertNotNull(fetchedBinds[1]);
    }

    @Test
    void shouldNotFetchBinds() throws Exception {
        // Fetch single
        Property floatProperty = TestClass.getProperty("floatProp");
        assertThrows(MissingBindException.class, () -> this.processor.fetchBind(null, floatProperty));

        // Fetch multiple
        Constructor<?> testConstructor = TestClass.class.getConstructors()[1];
        Annotation[] annotations = this.processor.fetchAnnotations(testConstructor);
        assertThrows(MissingBindException.class, () -> this.processor.fetchBinds(annotations, testConstructor));
    }

}
