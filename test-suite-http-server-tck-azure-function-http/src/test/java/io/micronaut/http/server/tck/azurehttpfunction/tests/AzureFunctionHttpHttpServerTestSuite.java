package io.micronaut.http.server.tck.azurehttpfunction.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@ExcludeClassNamePatterns({
        "io.micronaut.http.server.tck.tests.RequestUriContainsQueryValueTest",
        "io.micronaut.http.server.tck.tests.RequestUriTest",
        "io.micronaut.http.server.tck.tests.FilterProxyTest",
        "io.micronaut.http.server.tck.tests.HeadersTest"
})
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for Azure Functions")
public class AzureFunctionHttpHttpServerTestSuite {
}
