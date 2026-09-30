plugins {
    id("io.micronaut.build.internal.kotlin-kapt")
}

dependencies {
    kapt(platform(mn.micronaut.core.bom))
    kapt(mn.micronaut.inject.java)
    implementation(platform(mn.micronaut.core.bom))
    implementation(platform(projects.micronautAzureBom))
    implementation(projects.micronautAzureFunctionHttp)
    implementation(projects.micronautAzureSecretManager)
    implementation(libs.managed.azure.functions.java.library)
    implementation(libs.azure.storage.blob)

    kaptTest(mn.micronaut.inject.java)
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
