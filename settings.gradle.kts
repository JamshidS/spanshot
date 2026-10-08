plugins {
    // Lets Gradle download a matching JDK when the toolchain isn't installed locally.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "spanshot"

include("agent-java")
include("examples:spring-demo")
