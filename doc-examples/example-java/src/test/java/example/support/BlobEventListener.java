package example.support;

import example.BlobFunction;
import io.micronaut.context.event.ApplicationEventListener;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
public class BlobEventListener implements ApplicationEventListener<BlobFunction.BlobEvent> {

    private final List<String> received = new CopyOnWriteArrayList<>();

    @Override
    public void onApplicationEvent(BlobFunction.BlobEvent event) {
        received.add((String) event.getSource());
    }

    public List<String> getReceived() {
        return received;
    }
}
