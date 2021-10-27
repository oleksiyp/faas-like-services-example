package app;

import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

@SpringBootApplication
public class StreamingConsumerApplication {
	public static void main(String[] args) {
		SpringApplication.run(StreamingConsumerApplication.class, args);
	}

	@Bean
	public Consumer<Person> peopleConsumer() {
		return person -> {
			long end = System.currentTimeMillis();
			System.out.println(end - person.start);
		};
	}

	@Data
	public static class Person {
		String name;
		long start;
	}
}
