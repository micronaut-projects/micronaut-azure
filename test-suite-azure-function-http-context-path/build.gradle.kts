plugins {
    id("java-library")
    id("groovy")
}

dependencies {
    testAnnotationProcessor(platform(mn.micronaut.core.bom))
    testAnnotationProcessor(mn.micronaut.inject.java)

    testImplementation(projects.micronautAzureFunctionHttp)

    testImplementation(libs.managed.azure.functions.java.library)

    testImplementation(platform(mn.micronaut.core.bom))
    testImplementation(libs.junit.platform.engine)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(mnTest.micronaut.test.spock)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(mnLogging.logback.classic)
    testImplementation(mn.snakeyaml)
}

tasks.withType(Test::class.java) {
    useJUnitPlatform()
}

configurations.all {
    resolutionStrategy.preferProjectModules()
}
