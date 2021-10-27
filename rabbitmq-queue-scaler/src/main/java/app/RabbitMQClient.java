package app;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;
import com.rabbitmq.http.client.domain.QueueInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

@Component
@EnableConfigurationProperties(RabbitMQClientProperties.class)
@RequiredArgsConstructor
public class RabbitMQClient {
    private final RabbitMQClientProperties properties;
    private Client client;

    @PostConstruct
    public void start() throws MalformedURLException, URISyntaxException {
        client = new Client(
                new ClientParameters()
                        .url(properties.getHttpApiUrl())
                        .username(properties.getUsername())
                        .password(properties.getPassword())
        );
    }

    public QueueInfo queueInfo(String name) {
        return client.getQueue(properties.getVHost(), name);
    }
}
