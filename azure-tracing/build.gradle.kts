import io.micronaut.build.TestFramework

plugins {
    id("io.micronaut.build.internal.azure-module")
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    api(libs.opentelemetry.autoconfigure)
    api(mnTracing.micronaut.tracing.opentelemetry)
    testImplementation(mnTest.mockito.core)
}

micronautBuild {
    // new module
    binaryCompatibility {
        enabled.set(false)
    }
    testFramework = TestFramework.JUNIT5
}
