description = "Dependency Injector|Dependency Injector Benchmarks"

plugins {
    id("me.champeau.jmh") version "0.7.3"
}

dependencies {
    implementation(project(":di"))
    implementation(project(":di-codegen"))

    // Benchmark Tools
    val jmh = 1.36
    jmh("org.openjdk.jmh:jmh-core:$jmh")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:$jmh")
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:$jmh")
}

tasks.withType<PublishToMavenRepository>().configureEach { enabled = false }
tasks.withType<PublishToMavenLocal>().configureEach { enabled = false }