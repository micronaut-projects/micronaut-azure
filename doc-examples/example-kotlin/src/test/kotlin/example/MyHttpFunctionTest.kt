package example

import com.microsoft.azure.functions.HttpMethod
import io.micronaut.http.HttpStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MyHttpFunctionTest {

    @Test
    fun routesRequestsToControllers() {
        MyHttpFunction().use { function ->
            val response = function.request(HttpMethod.GET, "/hello").invoke()

            assertEquals(HttpStatus.OK.code, response.statusCode)
            assertEquals("Hello World", response.body)
        }
    }
}
