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

/**
 * General injector exception thrown when something went wrong during the dependency injection
 * or inside injected method/constructor
 *
 * @see MissingBindException for specific exception thrown when the bind is missing
 */
public class DependencyInjectionException extends RuntimeException {

    DependencyInjectionException(String message) {
        super(message);
    }

    DependencyInjectionException(String message, Throwable cause) {
        super(message, cause);
    }

}
