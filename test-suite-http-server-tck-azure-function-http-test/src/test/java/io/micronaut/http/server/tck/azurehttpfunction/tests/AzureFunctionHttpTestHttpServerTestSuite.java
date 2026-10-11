package io.micronaut.http.server.tck.azurehttpfunction.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@ExcludeTags({"multipart", "max-request-size"}) // multipart form fields are not bound from an Azure function request; the CORS tests post force=true as multipart to /refresh; the request body is handed over whole, so max-request-size is not enforced
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.filter.FilterMutatedRequestTest", // the mutable request view of a filter is not supported by the Azure function request
    "io.micronaut.http.server.tck.tests.BodyTest",
    "io.micronaut.http.server.tck.tests.FilterProxyTest",
    "io.micronaut.http.server.tck.tests.forms.FormBindingDeadlockTest",
    "io.micronaut.http.server.tck.tests.forms.UploadTest"
})
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for Azure Functions HTTP Test")
public class AzureFunctionHttpTestHttpServerTestSuite {
}
