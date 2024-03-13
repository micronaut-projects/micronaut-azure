plugins {
    groovy
    id("io.micronaut.build.internal.azure-example")
    alias(libs.plugins.azurefunctions.plugin)
}

version = "1.0"

micronaut {
    version(libs.versions.micronaut.platform.get())
    runtime("azure_function")
    testRuntime("spock")
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(mnSerde.micronaut.serde.processor)

//    implementation("io.micronaut.azure:micronaut-azure-function-http")
    implementation(projects.micronautAzureFunctionHttp)
    implementation(mnSerde.micronaut.serde.jackson)
    implementation(libs.managed.azure.functions.java.library)

    testImplementation(mnTestResources.testcontainers.core)
    testImplementation(libs.jakarta.inject.api)

    runtimeOnly(mnLogging.logback.classic)
}

azurefunctions {
    resourceGroup = "java-functions-group"
    appName = "test-suite"
    pricingTier = "Consumption"
    region = "westus"
    setRuntime(closureOf<com.microsoft.azure.gradle.configuration.GradleRuntimeConfig> {
        os("linux")
        javaVersion("Java 17")
    })
}

configurations.all {
    resolutionStrategy.preferProjectModules()
}

tasks {
    // We need the jar file for projects.micronautAzureFunctionHttp
    val functionPackage = named("azureFunctionsPackage") {
        dependsOn(rootProject.getTasksByName("publishAllPublicationsToBuildRepository", true))
    }

    named("test") {
        dependsOn(functionPackage)
    }
}
