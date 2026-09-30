plugins {
    id("io.micronaut.build.internal.azure-example")
}

micronaut {
    version(libs.versions.micronaut.platform.get())
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    compileOnly(mn.micronaut.inject.groovy)
    implementation(projects.micronautAzureFunctionHttp)
    implementation(projects.micronautAzureSecretManager)
    implementation(libs.jakarta.inject.api)
    implementation(libs.managed.azure.functions.java.library)
    implementation(libs.azure.storage.blob)

    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly(mnTest.junit.platform.launcher)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnSerde.micronaut.serde.jackson)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // The blob service endpoint read by example.BlobServiceFactory
    environment("AZURE_BLOB_ENDPOINT", "https://example.blob.core.windows.net")
}
