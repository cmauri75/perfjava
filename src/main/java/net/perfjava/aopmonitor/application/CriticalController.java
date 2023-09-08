/* CMauri - Italy - 2023 */
package net.perfjava.aopmonitor.application;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class CriticalController {

  private final CriticalService criticalService;

  public CriticalController(CriticalService criticalService) {
    this.criticalService = criticalService;
  }

  @GetMapping(value = "/critical", produces = MediaType.TEXT_PLAIN_VALUE)
  public Mono<String> critical() throws InterruptedException {
    return Mono.just(
        "Compute result is: "
            + criticalService.getComputeResultA()
            + "+"
            + criticalService.getComputeResultB());
  }
}
