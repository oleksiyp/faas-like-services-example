package app;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rabbitmq")
@Getter
@Setter
public class RabbitMQClientProperties {
    private String httpApiUrl = "http://127.0.0.1:15672/api/";
    private String vHost = "/";
    private String username = "guest";
    private String password = "guest";
}
