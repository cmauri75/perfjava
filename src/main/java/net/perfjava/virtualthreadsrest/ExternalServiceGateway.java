/* CMauri - Italy - 2023 */
package net.perfjava.virtualthreadsrest;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
public class ExternalServiceGateway {
  private static final Logger log = LoggerFactory.getLogger(ExternalServiceGateway.class);
  public static final Random rand = new Random();

  public String fakeGetResponse() {
    // Call external service
    try {
      TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(1000) + 500);
    } catch (InterruptedException e) {
      log.error("Unexpected interrupt", e);
    }
    return "Current Thread: " + Thread.currentThread();
  }

  public String getRealResponse() {
    return WebClient.builder()
        .baseUrl("http://127.0.0.1:8000")
        .build()
        .get()
        .exchangeToMono(resp -> resp.bodyToMono(String.class))
        .block();
  }

  public Flux<String> getRealFluxResponse() {
    return WebClient.builder()
        .baseUrl("http://127.0.0.1:8000")
        .build()
        .get()
        .exchangeToFlux(resp -> resp.bodyToFlux(String.class));
  }
}
