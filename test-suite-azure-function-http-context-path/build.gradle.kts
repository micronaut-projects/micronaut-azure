plugins {
    id("io.micronaut.build.internal.azure-module")
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    api(mn.micronaut.inject)
    api(mn.micronaut.http.server)
    api(project(":azure-function-http"))
    api(libs.managed.azure.functions.java.library)

    api(mn.micronaut.servlet.core)
    implementation(libs.jetty.server)
    implementation(libs.jakarta.inject.api)
    testCompileOnly(mn.micronaut.inject.groovy)
    testImplementation(mn.micronaut.test.spock)
    testImplementation(mn.micronaut.http.client)
    testAnnotationProcessor(mn.micronaut.inject.java)
}
