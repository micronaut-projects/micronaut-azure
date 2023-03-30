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

    // Reactive issues
    "io.micronaut.http.server.tck.tests.BodyTest", // Serialized reactive body is {"scanAvailable":true,"prefetch":-1}
    "io.micronaut.http.server.tck.tests.FluxTest", // Serialized reactive body is {"scanAvailable":true,"prefetch":-1}

    // Routing issues
    "io.micronaut.http.server.tck.tests.VersionTest", // Matches more than one route
    "io.micronaut.http.server.tck.tests.cors.CorsSimpleRequestTest", // Duplicate route exceptions
    "io.micronaut.http.server.tck.tests.cors.SimpleRequestWithCorsNotEnabledTest", // Duplicate route exceptions
    "io.micronaut.http.server.tck.tests.MiscTest", // Matches more than one route and 'error' failure
    "io.micronaut.http.server.tck.tests.cors.CorsDisabledByDefaultTest", // Duplicate route exceptions

    // Misc
    "io.micronaut.http.server.tck.tests.FiltersTest", // Cors filter fails Method [OPTIONS] not allowed for URI [/filter-test/ok]. Allowed methods: [HEAD, GET]
    "io.micronaut.http.server.tck.tests.OctetTest", // Response is badly encoded (mime encoding?)
    "io.micronaut.http.server.tck.tests.ParameterTest", // ???

    "io.micronaut.http.server.tck.tests.filter.ClientRequestFilterTest", // Multiple failures
    "io.micronaut.http.server.tck.tests.filter.ClientResponseFilterTest", // multiple failures

    "io.micronaut.http.server.tck.tests.CookiesTest", // Cookies seem to not be found
    "io.micronaut.http.server.tck.tests.endpoints.health.HealthTest", // Response is OK, but with empty body

    "io.micronaut.http.server.tck.tests.HeadersTest", // headersAreCaseInsensitiveAsPerMessageHeadersSpecification
})
public class AzureFunctionHttpHttpServerTestSuite {
}
