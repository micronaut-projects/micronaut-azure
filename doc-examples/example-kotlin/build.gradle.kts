plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    id ("io.micronaut.build.internal.kotlin-base")
}

dependencies {
    implementation(projects.micronautAzureFunctionHttp)
    implementation(libs.managed.azure.functions.java.library)
}
