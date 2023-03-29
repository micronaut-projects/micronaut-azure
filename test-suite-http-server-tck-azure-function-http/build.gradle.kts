plugins {
    id("io.micronaut.build.internal.http-server-tck-module")
}

dependencies {
    testAnnotationProcessor(mnValidation.micronaut.validation.processor)
    testImplementation(mn.micronaut.http.validation)
    testImplementation(projects.micronautAzureFunctionHttp)
    testImplementation(libs.managed.azure.functions.java.library)
    testImplementation(mnValidation.micronaut.validation.processor)
}
