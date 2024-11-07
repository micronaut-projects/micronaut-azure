package io.micronaut.http.server.tck.azurehttpfunction.tests;

import org.junit.platform.suite.api.*;

@Suite
@ExcludeClassNamePatterns({
        "io.micronaut.http.server.tck.tests.FilterProxyTest",
        "io.micronaut.http.server.tck.tests.jsonview.JsonViewsTest",  // fails testJsonViewFlux
})
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for Azure Functions")
public class AzureFunctionHttpHttpServerTestSuite {
}
