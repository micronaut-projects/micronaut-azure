import io.micronaut.build.TestFramework

plugins {
    id("io.micronaut.build.internal.azure-module")
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    implementation(platform(libs.managed.jackson2.bom))
    api(libs.managed.opentelemetry.autoconfigure)
    api(mnTracing.micronaut.tracing.opentelemetry)
    testImplementation(mnTest.mockito.core)
}

micronautBuild {
    // new module
    binaryCompatibility {
        enabled.set(false)
    }
    testFramework = TestFramework.JUNIT6
}
