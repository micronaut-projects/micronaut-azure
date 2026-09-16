plugins {
    id("io.micronaut.build.internal.azure-example")
    id("groovy")
}

micronaut {
    version(libs.versions.micronaut.platform.get())
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    implementation(mnServlet.micronaut.servlet.core)
    compileOnly(mn.micronaut.inject.groovy)
    implementation(projects.micronautAzureFunctionHttp)
    implementation(projects.micronautAzureSecretManager)
    implementation(libs.managed.azure.functions.java.library)
    implementation(libs.azure.storage.blob)
    implementation(libs.jakarta.inject.api)

    testCompileOnly(mn.micronaut.inject.groovy)
    testImplementation(mnTest.micronaut.test.spock)
    testRuntimeOnly(mnTest.junit.platform.launcher)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnSerde.micronaut.serde.jackson)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // The blob service endpoint read by example.BlobServiceFactory
    environment("AZURE_BLOB_ENDPOINT", "https://example.blob.core.windows.net")
}
