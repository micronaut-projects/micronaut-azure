plugins {
    id("io.micronaut.build.internal.azure-example")
    id("io.micronaut.build.internal.python")
}

micronaut {
    version(libs.versions.micronaut.platform.get())
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

// TODO(python): the examples live in src/main/python (the `source="main"` snippets) and the tests in
// src/test/python. Compiling them separately yields two GraalPy VFS roots whose generated shim modules shadow
// each other at test time, and the Python compiler resolves the imports of a source file only within its own
// source root, so both roots are merged into one directory compiled with the tests.
val mergePythonSources by tasks.registering(Sync::class) {
    from(layout.projectDirectory.dir("src/main/python"))
    from(layout.projectDirectory.dir("src/test/python"))
    into(layout.buildDirectory.dir("merged-python-sources"))
}
tasks.named("compilePython") {
    enabled = false
}
tasks.named<io.micronaut.build.python.PythonCompile>("compileTestPython") {
    dependsOn(mergePythonSources)
    source.setFrom(mergePythonSources.map { it.destinationDir })
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("micronaut.python.pool.enabled", "false")
    // Gradle enables assertions in test JVMs; an internal Truffle host-interop assertion trips on varargs overloads
    enableAssertions = false
    // The blob service endpoint read by example.BlobServiceFactory
    environment("AZURE_BLOB_ENDPOINT", "https://example.blob.core.windows.net")
}
