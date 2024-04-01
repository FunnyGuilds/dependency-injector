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

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import panda.utilities.ReflectionUtils;

/* JDK17 (R9-5900X, 32GB RAM 3200Mhz, Windows 11)
    Benchmark                                 Mode  Cnt       Score       Error   Units
    InvokeMethodBenchmark.direct             thrpt   10  868064.094 �  2086.213  ops/ms
    InvokeMethodBenchmark.generatedInjected  thrpt   10  864293.983 � 13610.569  ops/ms
    InvokeMethodBenchmark.injected           thrpt   10  857157.778 � 22008.580  ops/ms
    InvokeMethodBenchmark.reflection         thrpt   10  434325.439 �  1554.849  ops/ms
 */
@Fork(value = 1)
@Warmup(iterations = 10, time = 2)
@Measurement(iterations = 10, time = 2)
@Threads(4)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class InvokeMethodBenchmark {

    public static class Entity {
        private int points;
        public Integer bump() { return ++points; }
    }

    @Benchmark
    public Integer direct(DIState state) {
        return state.entity.bump();
    }

    @Benchmark
    public Object reflection(DIState state) throws Throwable {
        return state.method.invoke(state.entity);
    }

    @Benchmark
    public Integer injected(DIState state) throws Throwable {
        return state.injectedMethod.invoke(state.entity);
    }

    @Benchmark
    public Integer generatedInjected(DIState state) throws Throwable {
        return state.generatedInjectedMethod.invoke(state.entity);
    }

    @State(Scope.Thread)
    public static class DIState {

        private Entity entity;
        private Method method;
        private MethodInjector injectedMethod;
        private MethodInjector generatedInjectedMethod;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            this.entity = new Entity();
            this.method = ReflectionUtils.getMethod(Entity.class, "bump").get();
            this.injectedMethod = DependencyInjection.createInjector().forMethod(method);
            this.generatedInjectedMethod = DependencyInjection.createInjector().forGeneratedMethod(method);
        }

    }

    public static void main(String[] args) throws Exception {
        BenchmarkRunner.run(InvokeMethodBenchmark.class);
    }

}
