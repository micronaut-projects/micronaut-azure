plugins {
    id("io.micronaut.build.internal.azure-example")
    id("io.micronaut.build.internal.python")
}

micronaut {
    version(libs.versions.micronaut.platform.get())
}

micronautBuild {
    python {
        // The Azure Functions runtime reads the @FunctionName/@HttpTrigger/... annotations reflectively
        compilerArgs.add("-Amicronaut.introspection.allowReflection=example.*")
    }
}

// The examples of the guide, in Python, compiled by the Python compiler (micronaut-inject-python). The compiler
// takes the (jar-resolved) compile classpath as its annotation processor path, so the Micronaut processors are
// regular dependencies rather than annotationProcessor ones. The Python tests only run with -Ppython-ci.
dependencies {
    // The Java test helper (example.support.FakeKeyVaultKeySigner) is processed by javac
    testAnnotationProcessor(mn.micronaut.inject.java)

    testImplementation(mn.micronaut.inject.python.test)
    testImplementation(mn.micronaut.context.python)
    implementation(projects.micronautAzureFunctionHttp)
    implementation(projects.micronautAzureSecretManager)
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
    systemProperty("micronaut.python.pool.enabled", "false")
    // The blob service endpoint read by example.BlobServiceFactory
    environment("AZURE_BLOB_ENDPOINT", "https://example.blob.core.windows.net")
}
