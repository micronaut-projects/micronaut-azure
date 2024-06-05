package example.micronaut;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import org.reactivestreams.Publisher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Controller("/reactive")
public class ReactiveController {

    @Produces(MediaType.TEXT_PLAIN)
    @Get
    public Publisher<String> reactive() {
        Executor executor = Executors.newSingleThreadExecutor();
        return (s) -> {
            IntStream.range(0, 5).forEach(i -> CompletableFuture.supplyAsync(() -> String.valueOf(i), executor)
                .thenAccept(s::onNext));
            CompletableFuture.runAsync(s::onComplete, executor);
        };
    }
}
