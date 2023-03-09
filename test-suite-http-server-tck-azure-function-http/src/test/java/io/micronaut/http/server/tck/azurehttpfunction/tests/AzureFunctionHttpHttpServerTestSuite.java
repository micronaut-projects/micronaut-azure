package io.micronaut.http.server.tck.azurehttpfunction.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for Azure Functions")
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.VersionTest", // Matches more than one route
    "io.micronaut.http.server.tck.tests.filter.ResponseFilterTest", // Request is immutable
    "io.micronaut.http.server.tck.tests.OctetTest", // Response is badly encoded (mime encoding?)
    "io.micronaut.http.server.tck.tests.ParameterTest", // ???
    "io.micronaut.http.server.tck.tests.filter.RequestFilterExceptionHandlerTest", // Request is immutable
    "io.micronaut.http.server.tck.tests.FiltersTest", // Cors filter works fails with 'error'
    "io.micronaut.http.server.tck.tests.filter.ClientRequestFilterTest", // Multiple failures
    "io.micronaut.http.server.tck.tests.ErrorHandlerTest", // Cors headers
    "io.micronaut.http.server.tck.tests.FluxTest", // ???
    "io.micronaut.http.server.tck.tests.filter.RequestFilterTest", // Request is immutable
    "io.micronaut.http.server.tck.tests.MiscTest", // Matches more than one route and 'error' failure
    "io.micronaut.http.server.tck.tests.BodyTest", // testCustomListBodyPOJOReactiveTypes
    "io.micronaut.http.server.tck.tests.cors.CorsSimpleRequestTest", // There's no 'port' for the server under test
    "io.micronaut.http.server.tck.tests.DeleteWithoutBodyTest", // 'error'
    "io.micronaut.http.server.tck.tests.cors.SimpleRequestWithCorsNotEnabledTest", // corsSimpleRequestNotAllowedForLocalhostAndAny: no exception thrown
    "io.micronaut.http.server.tck.tests.CookiesTest", // 'error'
    "io.micronaut.http.server.tck.tests.FilterErrorTest", // testErrorsEmittedFromSecondFilterInteractingWithExceptionHandlers
    "io.micronaut.http.server.tck.tests.filter.ClientResponseFilterTest" // multiple failures
})
public class AzureFunctionHttpHttpServerTestSuite {
}
