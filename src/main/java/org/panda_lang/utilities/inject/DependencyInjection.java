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

public final class DependencyInjection {

    protected static final InjectorFactory INJECTOR_FACTORY = new InjectorFactory();

    /**
     * Create injector
     *
     * @param controller the controller to use by the injector
     * @return injector instance
     */
    public static Injector createInjector(InjectorController controller) {
        return INJECTOR_FACTORY.createInjector(controller);
    }

    /**
     * Create injector
     *
     * @return injector instance
     */
    public static Injector createInjector() {
        return INJECTOR_FACTORY.createInjector(new DefaultInjectorController());
    }

}