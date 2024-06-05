package example.micronaut

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.MountableFile
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import spock.util.environment.Jvm

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Issue("https://github.com/micronaut-projects/micronaut-azure/issues/696")
class HeaderFunctionSpec extends Specification {

    private static final String DOCKER_IMAGE_NAME = "mcr.microsoft.com/azure-functions/java:4-java${Jvm.current.javaSpecificationVersion}"

    @Shared
    @AutoCleanup
    GenericContainer azureFunctionContainer = new GenericContainer(DOCKER_IMAGE_NAME)
            .withEnv("AzureWebJobsScriptRoot", "/home/site/wwwroot")
            .withEnv("AzureFunctionsJobHost__Logging__Console__IsEnabled", "true")
            .withLogConsumer { log -> print("${log.getUtf8String()}") }
            .withExposedPorts(80)
            .waitingFor(Wait.forLogMessage(".*Host lock lease acquired by instance ID.*", 1))

    def setupSpec() {
        azureFunctionContainer
                .tap { copyDirectory(it, new File("build/azure-functions/test-suite"), "/home/site/wwwroot") }
                .start()
    }

    void "headers in Azure function context should not cause conflicts"() {
        when:
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:${azureFunctionContainer.getMappedPort(80)}/reactive"))
                .GET()
                .build()

        then:
        def response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        response.statusCode() == 200
        response.body() == '[0, 1, 2, 3, 4]'

        and:
        response.headers().allValues("Transfer-Encoding").size() == 1
        response.headers().firstValue("Transfer-Encoding").get() == "chunked"
    }

    void copyDirectory(GenericContainer container, File dir, String root) {
        println("Copying directory: ${dir.canonicalPath} to ${DOCKER_IMAGE_NAME}:$root")
        dir.traverse { file ->
            if (file.isFile()) {
                println("Copying file: $file to $root${file.path.substring(dir.path.size())}")
                container.withCopyFileToContainer(MountableFile.forHostPath(file.canonicalPath), root + file.path.substring(dir.path.size()))
            }
        }
    }
}
