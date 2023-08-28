/* CMauri - Italy - 2023 */
package net.perfjava;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@SpringBootTest
class PerfJavaTests {

  @Test
  public void fluxTestingConcat() {
    Flux<String> firstFluxOfProject =
        Flux.just("Honesty", "is").concatWith(Flux.just("best", "policy")).log();

    firstFluxOfProject.subscribe(
        System.out::println,
        e -> System.err.println("Exception got is :" + e),
        () -> System.out.println(" Finished the First-Flux."));
  }
}
