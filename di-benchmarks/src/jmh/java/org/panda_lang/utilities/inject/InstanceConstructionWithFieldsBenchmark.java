package org.panda_lang.utilities.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.panda_lang.utilities.inject.annotations.Inject;

/* JDK17 (I5-8600K OC 4.5 Ghz, 32GB RAM 3200Mhz, Windows 10)
    Benchmark                                                    Mode  Cnt        Score      Error   Units
    InstanceConstructionWithFieldsBenchmark.direct              thrpt   10  1784219.469 � 7246.175  ops/ms
    InstanceConstructionWithFieldsBenchmark.injected            thrpt   10     1508.048 �   10.094  ops/ms
    InstanceConstructionWithFieldsBenchmark.injectedFast        thrpt   10     2540.255 �   11.664  ops/ms
    InstanceConstructionWithFieldsBenchmark.injectedStatic      thrpt   10     2139.461 �   15.704  ops/ms
    InstanceConstructionWithFieldsBenchmark.injectedStaticFast  thrpt   10     3807.769 �   19.776  ops/ms
    InstanceConstructionWithFieldsBenchmark.reflection          thrpt   10    21433.173 �   28.671  ops/ms
 */
@Fork(value = 1)
@Warmup(iterations = 10, time = 2)
@Measurement(iterations = 10, time = 2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class InstanceConstructionWithFieldsBenchmark {

    public static class Entity {

        private final int id;
        private final String name;

        @Inject
        public EntityData data;
        @Inject
        public EntityStorage storage;

        public Entity(int id, String name) {
            this.name = name;
            this.id = id;
        }

    }

    public static class EntityData {

        private final int coins;
        private final float health;

        private EntityData(int coins, float health) {
            this.coins = coins;
            this.health = health;
        }

    }

    private static class EntityStorage {

        private final List<Object> items;

        private EntityStorage(@NotNull List<Object> items) {
            this.items = items;
        }

    }

    @Benchmark
    public void direct(DIState state) {
        Entity entity = new Entity(123456789, "PandaIsCool");
        entity.data = state.entityDataSupplier.get();
        entity.storage = state.entityStorage;
    }

    @Benchmark
    public void reflection(DIState state) throws Throwable {
        Entity object = state.entityConstructor.newInstance(123456789, "PandaIsCool");
        state.entityConstructor.setAccessible(true);

        Field dataField = Entity.class.getDeclaredField("data");
        dataField.setAccessible(true);
        dataField.set(object, state.entityDataSupplier.get());

        Field storageField = Entity.class.getDeclaredField("storage");
        storageField.setAccessible(true);
        storageField.set(object, state.entityStorage);
    }

    @Benchmark
    public void injected(DIState state) {
        state.entityInjector.newInstanceWithFields(Entity.class);
    }

    @Benchmark
    public void injectedFast(DIState state) throws Exception {
        state.entityInjector.forConstructor(Entity.class).newInstance();
    }

    @Benchmark
    public void injectedStatic(DIState state) {
        state.entityInjector.newInstanceWithFields(state.entityConstructor);
    }

    @Benchmark
    public void injectedStaticFast(DIState state) throws Exception {
        state.entityInjector.forConstructor(state.entityConstructor).newInstance();
    }

    @State(Scope.Benchmark)
    public static class DIState {

        private Constructor<Entity> entityConstructor;
        private Injector entityInjector;
        private final Supplier<EntityData> entityDataSupplier = () -> new EntityData(123456789, 24.5243F);
        private final EntityStorage entityStorage = new EntityStorage(Arrays.asList("Item1", "Item2", "Item3"));

        @Setup(Level.Trial)
        public void setup() throws Exception {
            this.entityConstructor = Entity.class.getDeclaredConstructor(int.class, String.class);
            this.entityInjector = DependencyInjection.createInjector(resources -> {
                resources.on(int.class).assignInstance(123456789);
                resources.on(String.class).assignInstance("PandaIsCool");
                resources.on(EntityData.class).assignInstance(this.entityDataSupplier);
                resources.on(EntityStorage.class).assignInstance(this.entityStorage);
            });

            CodegenFieldsInjector<Entity> entityCodegenFieldsInjector = new CodegenFieldsInjector<>(new InjectorProcessor(this.entityInjector), this.entityInjector.forConstructor(Entity.class));
        }

    }

    public static void main(String[] args) throws Exception {
        BenchmarkRunner.run(InstanceConstructionWithFieldsBenchmark.class);
    }

}
