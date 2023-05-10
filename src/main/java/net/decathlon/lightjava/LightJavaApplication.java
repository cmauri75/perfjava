package net.decathlon.lightjava;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class LightJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(LightJavaApplication.class, args);
	}

	@RestController
	public class GreetingController {

		private static final String template = "Hello, %s!";
		private final AtomicLong counter = new AtomicLong();

		@GetMapping("/")
		public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
			return new Greeting(counter.incrementAndGet(), String.format(template, name));
		}
	}

	public record Greeting(long counter, String data){};
}
