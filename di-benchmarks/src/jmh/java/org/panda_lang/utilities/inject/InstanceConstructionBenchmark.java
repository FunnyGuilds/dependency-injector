package org.panda_lang.utilities.inject;

import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
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

/* JDK17 (R9-5900X, 32GB RAM 3200Mhz, Windows 11)
    Benchmark                                               Mode  Cnt        Score        Error   Units
    InstanceConstructionBenchmark.direct                   thrpt   10  8660370.966 � 104379.534  ops/ms
    InstanceConstructionBenchmark.generatedInjected        thrpt   10    13621.451 �     37.353  ops/ms
    InstanceConstructionBenchmark.generatedInjectedStatic  thrpt   10    14021.858 �    180.536  ops/ms
    InstanceConstructionBenchmark.injected                 thrpt   10     8321.647 �    187.627  ops/ms
    InstanceConstructionBenchmark.injectedFast             thrpt   10     8941.758 �    263.119  ops/ms
    InstanceConstructionBenchmark.injectedStatic           thrpt   10     8742.619 �    370.675  ops/ms
    InstanceConstructionBenchmark.injectedStaticFast       thrpt   10     9668.630 �    376.711  ops/ms
    InstanceConstructionBenchmark.reflection               thrpt   10   193259.892 �    671.292  ops/ms
 */
@Fork(value = 1)
@Warmup(iterations = 10, time = 2)
@Measurement(iterations = 10, time = 2)
@Threads(4)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class InstanceConstructionBenchmark {

    public static class Entity {

        private final int id;
        private final String name;
        private final EntityData data;

        public Entity(int id, String name, EntityData data) {
            this.name = name;
            this.id = id;
            this.data = data;
        }

    }

    public static class EntityData {

        private final int coins;
        private final float health;

        public EntityData(int coins, float health) {
            this.coins = coins;
            this.health = health;
        }

    }

    @Benchmark
    public void direct(DIState state) {
        new Entity(123456789, "PandaIsCool", state.entityDataSupplier.get());
    }

    @Benchmark
    public void reflection(DIState state) throws Throwable {
        state.entityConstructor.newInstance(123456789, "PandaIsCool", state.entityDataSupplier.get());
    }

    @Benchmark
    public void injected(DIState state) {
        state.entityInjector.newInstance(Entity.class);
    }

    @Benchmark
    public void injectedFast(DIState state) throws Exception {
        state.entityInjector.forConstructor(Entity.class).newInstance();
    }

    @Benchmark
    public void injectedStatic(DIState state) {
        state.entityInjector.newInstance(state.entityConstructor);
    }

    @Benchmark
    public void injectedStaticFast(DIState state) throws Exception {
        state.entityInjector.forConstructor(state.entityConstructor).newInstance();
    }

    @Benchmark
    public void generatedInjected(DIState state) throws Exception {
        state.entityInjector.forGeneratedConstructor(Entity.class).newInstance();
    }

    @Benchmark
    public void generatedInjectedStatic(DIState state) throws Exception {
        state.entityInjector.forGeneratedConstructor(state.entityConstructor).newInstance();
    }

    @State(Scope.Benchmark)
    public static class DIState {

        private Constructor<Entity> entityConstructor;
        private Injector entityInjector;
        private final Supplier<EntityData> entityDataSupplier = () -> new EntityData(123456789, 24.5243F);

        @Setup(Level.Trial)
        public void setup() throws Exception {
            this.entityConstructor = Entity.class.getDeclaredConstructor(int.class, String.class, EntityData.class);
            this.entityInjector = DependencyInjection.createInjector(resources -> {
                resources.on(int.class).assignInstance(123456789);
                resources.on(String.class).assignInstance("PandaIsCool");
                resources.on(EntityData.class).assignInstance(this.entityDataSupplier);
            });
        }

    }

    public static void main(String[] args) throws Exception {
        BenchmarkRunner.run(InstanceConstructionBenchmark.class);
    }

}
