plugins {
    id("io.micronaut.build.internal.kotlin-kapt")
}

dependencies {
    testImplementation(projects.micronautAzureFunctionHttp)
    testImplementation(libs.managed.azure.functions.java.library)
}
