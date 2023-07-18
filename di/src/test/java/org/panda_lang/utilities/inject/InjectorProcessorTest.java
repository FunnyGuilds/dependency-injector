package org.panda_lang.utilities.inject;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;
import org.panda_lang.utilities.inject.annotations.AutoConstruct;
import org.panda_lang.utilities.inject.annotations.Injectable;
import org.panda_lang.utilities.inject.shared.AnnotationUtils;
import org.panda_lang.utilities.inject.shared.DummyProperty;

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
    @interface TestAnnotation {

    }

    private static class TestClass {

        public TestClass(int intProp, @TestAnnotation String stringAnnotatedProp) { }

        public TestClass(int intProp, float floatProp) { }

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
        Property intProperty = new DummyProperty<>("intProp", int.class);
        assertDoesNotThrow(() -> this.processor.fetchBind(null, intProperty));

        Property stringProperty = new DummyProperty<>("stringProp", String.class);
        Bind<Annotation> stringBind = assertDoesNotThrow(() -> this.processor.fetchBind(null, stringProperty));
        assertEquals("Test", stringBind.getValue(stringProperty, null));

        Property stringAnnotatedProperty = new DummyProperty<>("stringAnnotatedProp", String.class, TestAnnotation.class);
        TestAnnotation testAnnotation = AnnotationUtils.instanceAnnotation(TestAnnotation.class);
        Bind<Annotation> annotatedStringBind = assertDoesNotThrow(() -> this.processor.fetchBind(testAnnotation, stringAnnotatedProperty));
        assertEquals("TestAnnotation", annotatedStringBind.getValue(stringAnnotatedProperty, testAnnotation));

        Property autoConstructProperty = new DummyProperty<>("autoConstructProp", Object.class, AutoConstruct.class);
        Bind<Annotation> autoConstructBind = assertDoesNotThrow(() -> this.processor.fetchBind(null, autoConstructProperty));
        assertEquals(this.processor.autoConstructBind, autoConstructBind);

        // Fetch multiple
        Constructor<?> testConstructor = TestClass.class.getConstructors()[0];
        Annotation[] annotations = this.processor.fetchAnnotations(testConstructor);
        Bind<Annotation>[] fetchedBinds = assertDoesNotThrow(() -> this.processor.fetchBinds(annotations, testConstructor));

        assertEquals(2, fetchedBinds.length);
        assertNotNull(fetchedBinds[0]);
        assertNotNull(fetchedBinds[1]);
    }

    @Test
    void shouldNotFetchBinds() {
        // Fetch single
        Property floatProperty = new DummyProperty<>("floatProp", float.class);
        Property doubleProperty = new DummyProperty<>("doubleProp", double.class);

        assertThrows(MissingBindException.class, () -> this.processor.fetchBind(null, floatProperty));
        assertThrows(MissingBindException.class, () -> this.processor.fetchBind(null, doubleProperty));

        // Fetch multiple
        Constructor<?> testConstructor = TestClass.class.getConstructors()[1];
        Annotation[] annotations = this.processor.fetchAnnotations(testConstructor);

        assertThrows(MissingBindException.class, () -> this.processor.fetchBinds(annotations, testConstructor));
    }

}
