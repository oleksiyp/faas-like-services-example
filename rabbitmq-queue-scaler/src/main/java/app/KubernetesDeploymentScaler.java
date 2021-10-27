package app;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class KubernetesDeploymentScaler {
    private final KubernetesClient client;
    private final Map<DeploymentName, CooldownData> cooldownData = new LinkedHashMap<>();
    @Value("${scaling.cooldownIntervalMs:5000}")
    private long cooldownIntervalMs;

    private final Map<String, CompletableFuture<String>> scaledDown = new LinkedHashMap<>();

    private boolean isScaledDown(String namespace, String name) {
        synchronized (scaledDown) {
            String id = namespace + "/" + name;
            return scaledDown.containsKey(id);
        }
    }


    public void addAsScaledDown(String namespace, String name) {
        synchronized (scaledDown) {
            String id = namespace + "/" + name;

            if (scaledDown.containsKey(id)) {
                return;
            }

            scaledDown.put(id, new CompletableFuture<>());
        }
    }

    public void removeFromScaledDown(String namespace, String name) {
        synchronized (scaledDown) {
            String id = namespace + "/" + name;
            CompletableFuture<String> future = scaledDown.remove(id);
            if (future != null) {
                future.complete("DONE");
            }
        }
    }

    public CompletableFuture<String> futureFor(String namespace, String name) {
        synchronized (scaledDown) {
            String id = namespace + "/" + name;
            CompletableFuture<String> future = scaledDown.get(id);
            System.out.println(id + " " + future);
            if (future == null) {
                return CompletableFuture.completedFuture("DONE");
            }
            return future;
        }
    }

    public void scale(String namespace, String name, int replicas) {
        DeploymentName deploymentName = new DeploymentName(namespace, name);
        RollableScalableResource<Deployment> deployment = client.apps()
                .deployments()
                .inNamespace(namespace)
                .withName(name);
        Integer currentCount = deployment.scale().getSpec().getReplicas();
        if (currentCount == null) {
            currentCount = 0;
        }
        synchronized (scaledDown) {
            System.out.println(scaledDown);
            System.out.println(currentCount);
            System.out.println(replicas);
        }
        if (replicas > currentCount ||
                (isScaledDown(
                        deploymentName.getNamespace(),
                        deploymentName.getName()
                ) && replicas > 0)) {

            log.info("Switching replicaCount for {}/{} from {} to {}",
                    namespace,
                    name,
                    currentCount,
                    replicas
            );

            removeFromScaledDown(deploymentName.getNamespace(), deploymentName.getName());

            deployment.scale(replicas);

            synchronized (cooldownData) {
                cooldownData.remove(deploymentName);
            }
        } else if (replicas < currentCount) {
            if (replicas == 0 &&
                    isScaledDown(
                            deploymentName.getNamespace(),
                            deploymentName.getName()
                    )
            ) {
                System.out.println("z");
                return;
            }
            System.out.println("x");

            synchronized (cooldownData) {
                CooldownData data = this.cooldownData.get(deploymentName);
                if (data != null) {
                    data = data.toBuilder()
                            .newReplicaCount(replicas)
                            .build();
                } else {
                    data = CooldownData.builder()
                            .timestamp(System.currentTimeMillis())
                            .newReplicaCount(replicas)
                            .build();
                }
                cooldownData.put(deploymentName, data);
            }
        }
    }

    @Scheduled(fixedDelay = 1000L)
    public void run() {
        long now = System.currentTimeMillis();
        synchronized (cooldownData) {
            for (DeploymentName deploymentName : new LinkedHashSet<>(cooldownData.keySet())) {
                tryCoolDown(now, deploymentName);
            }
        }
    }

    private void tryCoolDown(long now, DeploymentName deploymentName) {
        CooldownData data = cooldownData.get(deploymentName);

        long started = data.getTimestamp();

        if (now - started <= cooldownIntervalMs) {
            return;
        }

        RollableScalableResource<Deployment> deployment = client.apps()
                .deployments()
                .inNamespace(deploymentName.getNamespace())
                .withName(deploymentName.getName());

        Integer currentCount = deployment.scale().getSpec().getReplicas();

        if (currentCount == null) {
            currentCount = 0;
        }

        int targetReplicaCount = data.getNewReplicaCount();
        System.out.println("a " + currentCount);
        System.out.println("a " + targetReplicaCount);

        if (targetReplicaCount == 0) {
            if (!isScaledDown(deploymentName.getNamespace(), deploymentName.getName())) {
                log.info("Cooling down and scaling down to zero for {}/{}",
                        deploymentName.getNamespace(),
                        deploymentName.getName()
                );
                deployment.scale(0, true);
                log.info("Starting warmup pod {}/{}",
                        deploymentName.getNamespace(),
                        deploymentName.getName()
                );
                addAsScaledDown(deploymentName.getNamespace(), deploymentName.getName());
                deployment.scale(1);
            }
        } else if (currentCount != targetReplicaCount) {
            log.info("Cooling down, switching replicaCount for {}/{} from {} to {}",
                    deploymentName.getNamespace(),
                    deploymentName.getName(),
                    currentCount,
                    targetReplicaCount
            );

            deployment.scale(targetReplicaCount);
        }

        cooldownData.remove(deploymentName);
    }
}
