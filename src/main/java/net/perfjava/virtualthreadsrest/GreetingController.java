/* CMauri - Italy - 2023 */
package net.perfjava.virtualthreadsrest;

import java.util.concurrent.atomic.AtomicLong;
import net.perfjava.virtualthreadsrest.dto.Greeting;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

  // CLASSICAL
  private static final String template = "Hello, %s!";
  private final AtomicLong counter = new AtomicLong();

  private final ExternalServiceGateway externalServiceGateway;

  public GreetingController(ExternalServiceGateway externalServiceGateway) {
    this.externalServiceGateway = externalServiceGateway;
  }

  @GetMapping("/")
  public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
    return new Greeting(counter.incrementAndGet(), String.format(template, name));
  }

  // THREADING
  @GetMapping("/threading")
  public String getResponse() {
    return externalServiceGateway.getResponse();
  }
}
