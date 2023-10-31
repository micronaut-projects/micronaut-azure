plugins {
    id("io.micronaut.build.internal.azure-module")
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    implementation(mn.micronaut.context)
    implementation(platform(projects.micronautAzureBom))
    api(libs.azure.identity)
    compileOnly(libs.azure.storage.common)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.inject.java)
    testImplementation(libs.azure.storage.blob)
    testImplementation(libs.system.lambda)
    testRuntimeOnly(libs.azure.storage.common)
}

tasks.withType(Test::class) {
    // required for SystemLambda
    jvmArgs = listOf("--add-opens", "java.base/java.util=ALL-UNNAMED")
}
