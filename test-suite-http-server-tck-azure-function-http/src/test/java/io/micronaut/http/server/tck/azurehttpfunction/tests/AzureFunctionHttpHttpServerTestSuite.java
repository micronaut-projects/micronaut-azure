package io.micronaut.http.server.tck.azurehttpfunction.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for Azure Functions")
@ExcludeClassNamePatterns({
    // Mutability
    "io.micronaut.http.server.tck.tests.filter.RequestFilterExceptionHandlerTest", // Request is immutable
    "io.micronaut.http.server.tck.tests.filter.RequestFilterTest", // Request is immutable
    "io.micronaut.http.server.tck.tests.filter.ResponseFilterTest", // Request is immutable

    "io.micronaut.http.server.tck.tests.cors.SimpleRequestWithCorsNotEnabledTest", // Duplicate route exceptions

    // Misc
    "io.micronaut.http.server.tck.tests.OctetTest", // Response is badly encoded (mime encoding?)
    "io.micronaut.http.server.tck.tests.ParameterTest", // ???

    "io.micronaut.http.server.tck.tests.CookiesTest", // Cookies seem to not be found
    "io.micronaut.http.server.tck.tests.endpoints.health.HealthTest", // Response is OK, but with empty body

    "io.micronaut.http.server.tck.tests.HeadersTest", // headersAreCaseInsensitiveAsPerMessageHeadersSpecification
    "io.micronaut.http.server.tck.tests.StreamTest",
    "io.micronaut.http.server.tck.tests.MissingBodyAnnotationTest",
    "io.micronaut.http.server.tck.tests.constraintshandler.ControllerConstraintHandlerTest" // Broken in servlet
})
public class AzureFunctionHttpHttpServerTestSuite {
}
