package app;

import app.crd.ScaledObject;
import app.crd.ScaledObjectSpec;
import com.rabbitmq.http.client.domain.MessageStats;
import com.rabbitmq.http.client.domain.QueueInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.Executor;

@Component
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class RabbitMQScaler {
    private final RabbitMQClient client;
    private final ScaledObjectWatcher watcher;
    private final Executor executor;
    private final KubernetesDeploymentScaler deploymentScaler;

    @Scheduled(fixedDelay = 300)
    public void run() {
        Collection<ScaledObject> objs = watcher.scaledObjects();
        log.debug("Polling iteration");
        for (ScaledObject obj : objs) {
            executor.execute(() -> {
                try {
                    ScaledObjectSpec spec = obj.getSpec();

                    QueueInfo queueInfo = client.queueInfo(spec.getQueueName());
                    if (queueInfo == null) {
                        log.debug("Missing queue {}", spec.getQueueName());
                        deploymentScaler.scale(
                                obj.getMetadata().getNamespace(),
                                obj.getMetadata().getName(),
                                1
                        );
                        return;
                    }
                    double messages = (double) queueInfo.getTotalMessages();
                    double units = messages / spec.getMessageThreshold();
                    MessageStats messageStats = queueInfo.getMessageStats();
                    if (spec.getRateThreshold() > 0 && messageStats != null && messageStats.getBasicPublishDetails() != null) {
                        double rate = messageStats.getBasicPublishDetails().getRate() * 1000.0;
                        units = Math.max(units, rate / spec.getRateThreshold());
                    }

                    long nUnits = (long) Math.ceil(units);

                    nUnits = Math.min(spec.getMaxReplicaCount(), nUnits);
                    nUnits = Math.max(spec.getMinReplicaCount(), nUnits);

                    deploymentScaler.scale(
                            obj.getMetadata().getNamespace(),
                            obj.getMetadata().getName(),
                            (int) nUnits
                    );
                } catch (Exception ex) {
                    log.error("Failed to query queue length", ex);
                }
            });
        }
    }
}
