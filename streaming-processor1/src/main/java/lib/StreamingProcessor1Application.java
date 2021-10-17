package lib;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;

@SpringBootApplication
public class StreamingProcessor1Application {
    public static void main(String[] args) {
        SpringApplication.run(StreamingProcessor1Application.class, args);
    }

    @Bean
    public Function<Person, Person> peopleProcessor1() {
        return person -> {
            System.out.println(Thread.currentThread().getName());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            return person.toBuilder()
                    .name("processor1:" + person.name)
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
