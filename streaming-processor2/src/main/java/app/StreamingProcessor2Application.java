package app;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;

@SpringBootApplication
public class StreamingProcessor2Application {
    public static void main(String[] args) {
        SpringApplication.run(StreamingProcessor2Application.class, args);
    }

    @Bean
    public Function<Person, Person> peopleProcessor2() {
        return person -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            return person.toBuilder()
                    .name("processor2:" + person.name)
                    .build();
        };
    }

    @Value
    @Builder(toBuilder = true)
    @Jacksonized
    public static class Person {
        String name;
    }
}
