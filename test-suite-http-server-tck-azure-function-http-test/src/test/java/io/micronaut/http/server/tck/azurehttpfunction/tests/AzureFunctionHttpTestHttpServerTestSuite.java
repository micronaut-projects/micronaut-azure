package io.micronaut.http.server.tck.azurehttpfunction.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.http.server.tck.tests")
@ExcludeClassNamePatterns("io.micronaut.http.server.tck.tests.FilterProxyTest")
@SuiteDisplayName("HTTP Server TCK for Azure Functions")
public class AzureFunctionHttpTestHttpServerTestSuite {
}
