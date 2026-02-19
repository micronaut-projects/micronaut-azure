plugins {
    id("io.micronaut.build.internal.kotlin-kapt")
}

dependencies {
    implementation(projects.micronautAzureFunctionHttp)
    implementation(libs.managed.azure.functions.java.library)
}
