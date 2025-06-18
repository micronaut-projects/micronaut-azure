plugins {
    id("io.micronaut.build.internal.azure-module")
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    api(libs.opentelemetry.autoconfigure)
    api(mnTracing.micronaut.tracing.opentelemetry)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testCompileOnly(mn.micronaut.inject.groovy)
}

micronautBuild {
    // new module
    binaryCompatibility {
        enabled.set(false)
    }
}
