plugins {
    id("io.micronaut.build.internal.http-server-tck-module")
}

dependencies {
    testImplementation(projects.micronautAzureFunctionHttp)
    testImplementation(libs.managed.azure.functions.java.library)
}
