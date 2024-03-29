plugins {
    id("io.micronaut.build.internal.azure-module")
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    api(mn.micronaut.function)
    compileOnly(libs.managed.azure.functions.java.library)
    testCompileOnly(mn.micronaut.inject.groovy)
    testAnnotationProcessor(mn.micronaut.inject.java)
}
