package net.perfjava.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@SpringBootApplication
@RestController
public class SlowServer {
    private static final Logger log = LoggerFactory.getLogger(SlowServer.class);

    public static void main(String[] args) {
        SpringApplication.run(SlowServer.class, args);
    }

    @GetMapping("/")
    public Flux<Map.Entry<String, String>> getData() {
        log.info("Received request {}",Thread.currentThread().getId());
        return Flux.fromStream(
                IntStream.range(1, 6).mapToObj(it -> Map.entry(it + " of 5", "ok")))
                .delayElements(Duration.of(ThreadLocalRandom.current().nextLong(1000), ChronoUnit.MILLIS));
    }
}
