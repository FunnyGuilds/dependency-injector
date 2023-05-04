plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

description = "Dependency Injector|Parent"

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "signing")
    apply(plugin = "maven-publish")

    group = "org.panda-lang.utilities"
    version = "1.6.0"

    repositories {
        mavenCentral()
        maven {
            name = "panda-repository"
            url = uri("https://repo.panda-lang.org/releases")
        }
    }

    publishing {
        repositories {
            maven {
                name = "panda-repository"
                url = uri("https://maven.reposilite.com/${if (version.toString().endsWith("-SNAPSHOT")) "snapshots" else "releases"}")

                credentials {
                    username = getEnvOrProperty("MAVEN_NAME", "mavenUser")
                    password = getEnvOrProperty("MAVEN_TOKEN", "mavenPassword")
                }
            }
        }
    }

    afterEvaluate {
        description
            ?.takeIf { it.isNotEmpty() }
            ?.split("|")
            ?.let { (projectName, projectDescription) ->
                publishing {
                    publications {
                        create<MavenPublication>("library") {
                            pom {
                                name.set(projectName)
                                description.set(projectDescription)
                                url.set("https://github.com/FunnyGuilds/dependency-injector")

                                licenses {
                                    license {
                                        name.set("The MIT License")
                                        url.set("https://opensource.org/licenses/MIT")
                                    }
                                }
                                developers {
                                    developer {
                                        id.set("dzikoysk")
                                        name.set("dzikoysk")
                                        email.set("dzikoysk@dzikoysk.net")
                                    }
                                    developer {
                                        id.set("peridot")
                                        name.set("Peridot")
                                        email.set("peridot491@pm.me")
                                    }
                                }
                                scm {
                                    connection.set("scm:git:git://github.com/FunnyGuilds/dependency-injector.git")
                                    developerConnection.set("scm:git:ssh://github.com/FunnyGuilds/dependency-injector.git")
                                    url.set("https://github.com/FunnyGuilds/dependency-injector.git")
                                }
                            }

                            from(components.getByName("java"))
                        }
                    }
                }

                if (findProperty("signing.keyId").takeIf { it?.toString()?.trim()?.isNotEmpty() == true } != null) {
                    signing {
                        sign(publishing.publications.getByName("library"))
                    }
                }
            }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

subprojects {
    dependencies {
        // General
        api("org.panda-lang:panda-utilities:0.5.3-alpha")
        api("org.panda-lang:expressible:1.3.4")
        compileOnly("org.jetbrains:annotations:24.0.0")

        // Benchmarks
        testImplementation("org.openjdk.jmh:jmh-core:1.32")
        testImplementation("org.openjdk.jmh:jmh-generator-annprocess:1.32")

        // Tests
        val junit = "5.9.1"
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junit")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:$junit")
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.register("release") {
        dependsOn(
            "publishAllPublicationsToPanda-repositoryRepository",
            "publishToSonatype",
        )
    }
}

tasks.register("release") {
    dependsOn(
        "clean", "build",
        "publishAllPublicationsToPanda-repositoryRepository",
        "publishAllPublicationsToSonatypeRepository",
        "closeAndReleaseSonatypeStagingRepository"
    )
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            username.set(getEnvOrProperty("SONATYPE_USER", "sonatypeUser"))
            password.set(getEnvOrProperty("SONATYPE_PASSWORD", "sonatypePassword"))
        }
    }
}

fun getEnvOrProperty(env: String, property: String): String? =
    System.getenv(env) ?: findProperty(property)?.toString()