plugins {
    id("io.micronaut.build.internal.http-test-module")
}
repositories {
    mavenCentral()
}
val micronautVersion: String by project
dependencies {
    testImplementation(projects.azureFunctionHttp)
    testImplementation(libs.managed.azure.functions.java.library)
    testImplementation(projects.httpServerTck)
}
