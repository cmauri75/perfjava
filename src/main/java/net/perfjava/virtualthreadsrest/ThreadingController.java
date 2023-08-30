/* CMauri - Italy - 2023 */
package net.perfjava.virtualthreadsrest;

import java.util.concurrent.atomic.AtomicLong;
import net.perfjava.virtualthreadsrest.dto.Greeting;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ThreadingController {

  @Value("${app.useVirtualThread}")
  boolean userVirtualThread;

  private static final String HELLO_TEMPLATE = "Hello, %s!. Using virtual threads? %s";
  private static final String THREAD_TEMPLATE = """
            {"Result" : "%s"}
            """;

  private final AtomicLong counter = new AtomicLong();

  private final ExternalServiceGateway externalServiceGateway;

  public ThreadingController(ExternalServiceGateway externalServiceGateway) {
    this.externalServiceGateway = externalServiceGateway;
  }

  @GetMapping("/")
  public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
    return new Greeting(
        counter.incrementAndGet(), String.format(HELLO_TEMPLATE, name, userVirtualThread));
  }

  // THREADING
  @GetMapping(value = "/threading", produces = MediaType.APPLICATION_JSON_VALUE)
  public String threading() {
    return String.format(THREAD_TEMPLATE, externalServiceGateway.fakeGetResponse());
  }

  @GetMapping(value = "/fluxthreading", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> fluxthreading() {
    return Mono.just(String.format(THREAD_TEMPLATE, externalServiceGateway.fakeGetResponse()));
  }

  // SECOND USE CASE
  @GetMapping(value = "/threading2", produces = MediaType.APPLICATION_JSON_VALUE)
  public String threading2() {
    return externalServiceGateway.getRealResponse();
  }

  @GetMapping(value = "/fluxthreading2", produces = MediaType.APPLICATION_JSON_VALUE)
  public Flux<String> fluxthreading2() {
    return externalServiceGateway.getRealFluxResponse();
  }
}
