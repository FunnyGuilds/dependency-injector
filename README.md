# Dependency Injection
Blazingly fast and lightweight dependency injection framework for Java. Supported operations:
* Creating a new instance of the specified type using Injector _(constructors)_
* Invoking methods using Injector _(methods)_

### Install
Library is available in `panda-repository` Maven repository:

```xml
<repository>
    <id>panda-repository</id>
    <url>https://repo.panda-lang.org/releases</url>
</repository>
```

To use `annotations`, declare the dependency in your `pom.xml`

```xml
<dependency>
    <groupId>org.panda-lang.utilities</groupId>
    <artifactId>di</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Usage
Firstly, you have to create `Injector` which keeps all the registered bindings (injected values)

```java
Injector injector = DependencyInjection.createInjector()

// If you are able to register binding at the init time, it's also recommended to use the following structure
Injector injector = DependencyInjection.createInjector(resources -> {
    // bind resources
});
```

Injector supports three main ways to bind with a value:
* Binding to the specified type `resources.on(<Type>)`
* Binding to the specified annotation `resources.annotatedWith(<Annotation>)`
* Binding to the verified annotation `resources.annotatedWithTested(<Annotation>)` *(safer but slower alternative to `annotatedWith`)*

Each binding supports three ways of assigning value:
* `assign(<Type>)` - creates a new instance of type per call
* `assignInstance(<Object>)`/`assignInstance(Supplier<Object>)` - binds the specified value/some kind of lazy values
* `assignHandler((<Expected Type Of Value>, <Annotation>) -> { /* logic */ })` - binds custom handler

#### Instances
The most common use of DI comes down to some kind of instance assignation:

```java
public static class Bean { }

public interface Custom { }
static class CustomImpl implements Custom { }

static class Service {
    public Service(Bean bean, Custom custom) {
        assertNotNull(bean);
        assertNotNull(custom);
    }
}
```

To create `Service` class, just invoke its constructor:

```java
Injector injector = DependencyInjection.createInjector();

// some logic, a few hours later...

injector.getResources().on(Bean.class).assign(Bean.class);
injector.getResources().on(Custom.class).assignInstance(new CustomImpl());

Service service = injector.forConstructor(Service.class).newInstance();
```

Full example: [DependencyInjectionInstancesTest.java](https://github.com/dzikoysk/di/blob/master/src/test/java/org/panda_lang/panda/utilities/inject/DependencyInjectionInstancesTest.java)

#### Custom logic

Let's build a random example based on these methods using a custom annotation:

```java
@Injectable // mark annotation as DI ready annotation
@Retention(RetentionPolicy.RUNTIME) // make sure that the annotation is visible at runtime
@interface AwesomeRandom { }

static final class Entity {
    private final UUID id;

    private Entity(@AwesomeRandom UUID random) {
        this.id = random;
    }

    public UUID getId() {
        return id;
    }
}
```

We'd like to generate a new id per each `Entity` instance with a private constructor. It's also important for us, to support id in two forms:
* String
* UUID

```java
Injector injector = DependencyInjection.createInjector(resources -> {
    resources.annotatedWith(AwesomeRandom.class).assignHandler((expectedType, annotation) -> {
        if (expectedType.equals(String.class)) {
            return UUID.randomUUID().toString();
        }

        if (expectedType.equals(UUID.class)) {
            return UUID.randomUUID();
        }

        throw new IllegalArgumentException("Unsupported type " + expectedType);
    });
});

// Create entities using the injector instance
Entity entityA = injector.newInstance(Entity.class);
Entity entityB = injector.newInstance(Entity.class);

// Print generated values
System.out.println(entityA.getId());
System.out.println(entityB.getId());
```

The output produces some random identifiers as intended 👍 

```
e23442b2-f695-41fa-9290-0f1192118a1a
9f92121c-096e-4bdb-b6ad-0901974bbe37

Process finished with exit code 0
```

Full example is available here -> [DependencyInjectionWikiTest.java](https://github.com/dzikoysk/di/blob/master/src/test/java/org/panda_lang/panda/utilities/inject/DependencyInjectionWikiTest.java)