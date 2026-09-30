package example.support

import example.BlobFunction
import io.micronaut.context.event.ApplicationEventListener
import jakarta.inject.Singleton
import java.util.concurrent.CopyOnWriteArrayList

@Singleton
class BlobEventListener : ApplicationEventListener<BlobFunction.BlobEvent> {

    val received: MutableList<String> = CopyOnWriteArrayList()

    override fun onApplicationEvent(event: BlobFunction.BlobEvent) {
        received.add(event.source as String)
    }
}
