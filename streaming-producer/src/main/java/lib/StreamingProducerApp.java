package lib;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@SpringBootApplication
public class StreamingProducerApp {
	public static void main(String[] args) {
		SpringApplication.run(StreamingProducerApp.class, args);
	}

	@Bean
	public Supplier<Person> peopleProducer() {
		AtomicLong count = new AtomicLong();
		return () -> Person.builder()
				.name("name" + count.incrementAndGet())
				.build();
	}

	@Value
	@Builder(toBuilder = true)
	@Jacksonized
	public static class Person {
		String name;
	}
}
