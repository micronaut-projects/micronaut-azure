plugins {
    id("io.micronaut.build.internal.azure-module")
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    api(mn.micronaut.inject)
    api(mn.micronaut.http.server)
    api(projects.azureFunction)
    api(mnServlet.micronaut.servlet.core)
    implementation(mn.micronaut.http.netty)
    implementation(mn.micronaut.router)
    implementation(libs.jakarta.inject.api)
    compileOnly(libs.managed.azure.functions.java.library)

    testImplementation(mn.micronaut.jackson.databind)
    testImplementation(libs.managed.azure.functions.java.library)
    testCompileOnly(mn.micronaut.inject.groovy)
    testAnnotationProcessor(mn.micronaut.inject.java)
}
