package example.micronaut

import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.MountableFile
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

@Issue("https://github.com/micronaut-projects/micronaut-azure/issues/611")
class ConcurrentFunctionSpec extends Specification {

    private static final int NUMBER_OF_THREADS = 3
    private static final int NUMBER_OF_REQUESTS = 100

    @Shared
    @AutoCleanup
    GenericContainer azureFunctionContainer = new GenericContainer("mcr.microsoft.com/azure-functions/java:4-java17")
            .withEnv("AzureWebJobsScriptRoot", "/home/site/wwwroot")
            .withEnv("AzureFunctionsJobHost__Logging__Console__IsEnabled", "true")
            .withExposedPorts(80)
            .waitingFor(Wait.forLogMessage(".*Host lock lease acquired by instance ID.*", 1))

    def setupSpec() {
        azureFunctionContainer
                .tap { copyDirectory(it, new File("build/azure-functions/test-suite"), "/home/site/wwwroot") }
                .start()
    }

    def "should work"() {
        when:
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:${azureFunctionContainer.getMappedPort(80)}/default"))
                .POST(HttpRequest.BodyPublishers.ofString('{"name":"Fred"}'))
                .build()

        AtomicInteger failures = new AtomicInteger()

        then:
        (1..NUMBER_OF_THREADS).collect {
            Thread.start {
                (1..NUMBER_OF_REQUESTS).each {
                    def response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
                    if (response.statusCode() != 200) {
                        failures.incrementAndGet()
                    }
                }
            }
        }*.join()

        failures.get() == 0
    }

    void copyDirectory(GenericContainer container, File dir, String root) {
        println("Copying directory: ${dir.canonicalPath} to $root")
        dir.traverse { file ->
            if (file.isFile()) {
                println("Copying file: $file to $root${file.path.substring(dir.path.size())}")
                container.withCopyFileToContainer(MountableFile.forHostPath(file.canonicalPath), root + file.path.substring(dir.path.size()))
            }
        }
    }
}
