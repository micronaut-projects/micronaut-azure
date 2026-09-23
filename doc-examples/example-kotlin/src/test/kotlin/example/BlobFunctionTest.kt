package example

import example.support.BlobEventListener
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BlobFunctionTest {

    @Test
    fun copiesTheBlobAndPublishesAnEvent() {
        BlobFunction().use { function ->
            val result = function.copy("blob content")

            assertEquals("blob content", result)
            val listener = function.applicationContext.getBean(BlobEventListener::class.java)
            assertEquals(listOf("blob content"), listener.received)
        }
    }
}
