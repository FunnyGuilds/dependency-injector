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
import org.openjdk.jmh.annotations.Warmup;

/* JDK17 (I5-8600K OC 4.5 Ghz, 32GB RAM 3200Mhz, Windows 10)
    Benchmark                                      Mode  Cnt        Score      Error   Units
    InstanceConstructionBenchmark.direct          thrpt   10  1786519.662 � 1501.003  ops/ms
    InstanceConstructionBenchmark.injected        thrpt   10     1742.075 �   13.386  ops/ms
    InstanceConstructionBenchmark.injectedStatic  thrpt   10     2782.905 �   21.410  ops/ms
    InstanceConstructionBenchmark.reflection      thrpt   10   103925.242 �  270.782  ops/ms
 */
@Fork(value = 1)
@Warmup(iterations = 10, time = 2)
@Measurement(iterations = 10, time = 2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class InstanceConstructionBenchmark {

    private static class Entity {

        private final int id;
        private final String name;
        private final EntityData data;

        public Entity(int id, String name, EntityData data) {
            this.name = name;
            this.id = id;
            this.data = data;
        }

    }

    private static class EntityData {

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
        state.constructor.newInstance(123456789, "PandaIsCool", state.entityDataSupplier.get());
    }

    @Benchmark
    public void injected(DIState state) {
        state.entityInjector.newInstance(Entity.class);
    }

    @Benchmark
    public void injectedStatic(DIState state) throws Exception {
        state.entityInjector.forConstructor(state.constructor).newInstance();
    }

    @State(Scope.Benchmark)
    public static class DIState {

        private Constructor<Entity> constructor;
        private Injector entityInjector;
        private final Supplier<EntityData> entityDataSupplier = () -> new EntityData(123456789, 24.5243F);

        @Setup(Level.Trial)
        public void setup() throws Exception {
            this.constructor = Entity.class.getDeclaredConstructor(int.class, String.class, EntityData.class);
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
