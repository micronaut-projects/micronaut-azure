package io.micronaut.http.server.tck.azurehttpfunction.tests;

import org.junit.platform.suite.api.*;

@Suite
@ExcludeClassNamePatterns({
        "io.micronaut.http.server.tck.tests.FilterProxyTest",
    "io.micronaut.http.server.tck.tests.ErrorHandlerFluxTest", // test fails testErrorHandlerWithFluxChunkedSignaledDelayedError
    "io.micronaut.http.server.tck.tests.forms.FormsJacksonAnnotationsTest", // test fails httpClientFormSubmissionsDoesNotSupportJacksonAnnotations"
    "io.micronaut.http.server.tck.tests.forms.UploadTest",
    "io.micronaut.http.server.tck.tests.forms.FormBindingDeadlockTest"
})
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for Azure Functions")
public class AzureFunctionHttpHttpServerTestSuite {
}
