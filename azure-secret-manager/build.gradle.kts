plugins {
    id("io.micronaut.build.internal.azure-module")
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    implementation(platform(projects.micronautAzureBom))

    api(mn.micronaut.inject)
    api(mn.micronaut.http.server)
    api(projects.micronautAzureSdk)
    api(libs.azure.identity)
    api(libs.azure.security.keyvault.secrets)

    implementation(mn.micronaut.discovery.core)
    implementation(mnReactor.micronaut.reactor)

    testAnnotationProcessor(mn.micronaut.inject.java)
    testCompileOnly(mn.micronaut.inject.groovy)
    testImplementation(mn.micronaut.runtime)
    testImplementation(mn.micronaut.http.client)
}

