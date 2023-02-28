package io.micronaut.http.server.tck.azurehttpfunction.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for Azure Functions")
@ExcludeClassNamePatterns(
    "io.micronaut.http.server.tck.tests.ErrorHandlerTest" +
    "|io.micronaut.http.server.tck.tests.MiscTest" +
    "|io.micronaut.http.server.tck.tests.cors.CorsSimpleRequestTest" +
    "|io.micronaut.http.server.tck.tests.VersionTest" +
    "|io.micronaut.http.server.tck.tests.CookiesTest" +
    "|io.micronaut.http.server.tck.tests.FilterErrorTest" +
    "|io.micronaut.http.server.tck.tests.FiltersTest" +
    "|io.micronaut.http.server.tck.tests.cors.SimpleRequestWithCorsNotEnabledTest" +
    "|io.micronaut.http.server.tck.tests.OctetTest" +
    "|io.micronaut.http.server.tck.tests.ParameterTest" +
    "|io.micronaut.http.server.tck.tests.DeleteWithoutBodyTest" +
    "|io.micronaut.http.server.tck.tests.filter.HttpServerFilterTest"
)
public class AzureFunctionHttpHttpServerTestSuite {
}
