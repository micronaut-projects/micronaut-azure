plugins {
    id("io.micronaut.build.internal.http-server-tck-module")
}

dependencies {
    testImplementation(projects.micronautAzureFunctionHttpTest)
    testImplementation(libs.managed.azure.functions.java.library)
    testRuntimeOnly(mn.snakeyaml)
}
