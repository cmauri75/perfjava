/* CMauri - Italy - 2023 */
package net.perfjava;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import net.perfjava.service.ExternalServiceGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class PerfjavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PerfjavaApplication.class, args);
    }

    @RestController
    public class GreetingController {

        // CLASSICAL
        private static final String template = "Hello, %s!";
        private final AtomicLong counter = new AtomicLong();

        @GetMapping("/")
        public Greeting greeting(
                @RequestParam(value = "name", defaultValue = "World") String name) {
            return new Greeting(counter.incrementAndGet(), String.format(template, name));
        }

        // THREADING
        private ExternalServiceGateway externalServiceGateway;

        @Autowired
        public void setHomeService(ExternalServiceGateway externalServiceGateway) {
            this.externalServiceGateway = externalServiceGateway;
        }

        @GetMapping("/threading")
        public String getResponse() {
            return externalServiceGateway.getResponse();
        }
    }

    // Enables virtualthread management
    @Bean
    TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }

    public record Greeting(long counter, String data) {}
}
