package example.support

import example.BlobFunction
import io.micronaut.context.event.ApplicationEventListener
import jakarta.inject.Singleton

import java.util.concurrent.CopyOnWriteArrayList

@Singleton
class BlobEventListener implements ApplicationEventListener<BlobFunction.BlobEvent> {

    final List<String> received = new CopyOnWriteArrayList<>()

    @Override
    void onApplicationEvent(BlobFunction.BlobEvent event) {
        received.add((String) event.source)
    }
}
