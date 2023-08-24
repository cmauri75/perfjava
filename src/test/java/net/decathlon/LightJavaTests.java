/* Decathlon Italy - Tacos Team(C) 2023 */
package net.decathlon;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

class LightJavaTests {

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
