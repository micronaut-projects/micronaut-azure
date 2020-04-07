package example;

import com.microsoft.azure.functions.HttpStatus;
import io.micronaut.azure.function.http.HttpRequestMessageBuilder;
import io.micronaut.http.HttpMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for Function class.
 */
public class FunctionTest {
    /**
     * Unit test for HttpTriggerJava method.
     */
    @Test
    public void testHttpTriggerJava() throws Exception {
        Function function = new Function();
        HttpRequestMessageBuilder.AzureHttpResponseMessage responseMessage = function.request(HttpMethod.GET, "/pets/Dino")
                .invoke();

        assertEquals(
                HttpStatus.OK, responseMessage.getStatus()
        );
        assertEquals(
                "{\"name\":\"Dino\",\"age\":12}",
                responseMessage.getBodyAsString()
        );
    }
}
