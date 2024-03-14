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
    testImplementation(libs.jakarta.inject.api)
    implementation(libs.managed.azure.functions.java.library)
}
