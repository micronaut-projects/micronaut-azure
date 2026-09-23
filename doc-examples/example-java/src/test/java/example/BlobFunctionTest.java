package example;

import example.support.BlobEventListener;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlobFunctionTest {

    @Test
    void copiesTheBlobAndPublishesAnEvent() {
        try (BlobFunction function = new BlobFunction()) {
            String result = function.copy("blob content");

            assertEquals("blob content", result);
            BlobEventListener listener = function.getApplicationContext().getBean(BlobEventListener.class);
            assertEquals(List.of("blob content"), listener.getReceived());
        }
    }
}
