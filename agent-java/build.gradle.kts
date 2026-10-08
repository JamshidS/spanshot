plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile>().configureEach {
    // The agent runs inside the user's JVM: Spring Boot 3 apps can be on Java 17.
    options.release = 17
}

tasks.jar {
    archiveBaseName = "spanshot-agent"
    manifest {
        attributes(
            "Premain-Class" to "dev.spanshot.agent.SpanshotAgent",
            "Implementation-Title" to "Spanshot Agent",
            "Implementation-Version" to project.version,
        )
    }
}
