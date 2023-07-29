plugins {
    id("java-library")
    id("groovy")
}

dependencies {
    testAnnotationProcessor(platform(mn.micronaut.bom))
    testAnnotationProcessor(mn.micronaut.inject.java)

    testImplementation(project(":azure-function-http"))

    testImplementation(libs.managed.azure.functions.java.library)

    testImplementation(platform(mn.micronaut.bom))
    testImplementation(libs.junit.platform.engine)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(mn.micronaut.test.spock)
}

tasks.withType(Test::class.java) {
    useJUnitPlatform()
}
