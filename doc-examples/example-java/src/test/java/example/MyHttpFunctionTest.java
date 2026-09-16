package example;

import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpResponseMessage;
import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MyHttpFunctionTest {

    @Test
    void routesRequestsToControllers() {
        try (MyHttpFunction function = new MyHttpFunction()) {
            HttpResponseMessage response = function.request(HttpMethod.GET, "/hello").invoke();

            assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
            assertEquals("Hello World", response.getBody());
        }
    }
}
