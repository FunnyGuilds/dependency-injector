/*
 * Copyright (c) 2020 Dzikoysk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.panda_lang.utilities.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.jupiter.api.Test;
import org.panda_lang.utilities.inject.annotations.Injectable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DependencyInjectionUtilsTest {

    @Test
    void testAnnotation() {
        assertThrows(DependencyInjectionException.class, () -> DependencyInjectionUtils.testAnnotation(NotAnAnnotation.class));
        assertThrows(DependencyInjectionException.class, () -> DependencyInjectionUtils.testAnnotation(DefaultAnnotation.class));
        assertThrows(DependencyInjectionException.class, () -> DependencyInjectionUtils.testAnnotation(ClassAnnotation.class));
        assertThrows(DependencyInjectionException.class, () -> DependencyInjectionUtils.testAnnotation(RuntimeAnnotation.class));

        assertDoesNotThrow(() -> DependencyInjectionUtils.testAnnotation(InjectableAnnotation.class));
    }

    private static class NotAnAnnotation {}

    private @interface DefaultAnnotation {}

    @Retention(RetentionPolicy.CLASS)
    private @interface ClassAnnotation {}

    @Retention(RetentionPolicy.RUNTIME)
    private @interface RuntimeAnnotation {}

    @Injectable
    @Retention(RetentionPolicy.RUNTIME)
    private @interface InjectableAnnotation {}

}