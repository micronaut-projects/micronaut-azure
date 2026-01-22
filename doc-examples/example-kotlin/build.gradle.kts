plugins {
    id("io.micronaut.build.internal.kotlin-kapt")
}

dependencies {
    testImplementation(projects.micronautAzureFunctionHttp)
    testImplementation(libs.managed.azure.functions.java.library)
    testImplementation(mnTest.junit.platform.suite)
}

tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
}
