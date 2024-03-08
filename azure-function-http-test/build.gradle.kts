plugins {
    id("io.micronaut.build.internal.azure-module")
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    api(mn.micronaut.inject)
    api(mn.micronaut.http.server)
    api(projects.micronautAzureFunctionHttp)
    api(libs.managed.azure.functions.java.library)

    api(mnServlet.micronaut.servlet.core)
    implementation(platform(mnServlet.boms.jetty))
    implementation(libs.jetty.server)
    implementation(libs.jakarta.inject.api)
    testCompileOnly(mn.micronaut.inject.groovy)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(mnTest.micronaut.test.spock)
    testImplementation(mn.micronaut.http.client)
    testAnnotationProcessor(mn.micronaut.inject.java)
}
