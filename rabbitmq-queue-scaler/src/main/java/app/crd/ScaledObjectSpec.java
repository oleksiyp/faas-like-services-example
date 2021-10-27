package app.crd;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import lombok.Getter;
import lombok.Setter;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@Getter
@Setter
public class ScaledObjectSpec implements KubernetesResource {
    private String queueName;
    private int messageThreshold = 20;
    private int rateThreshold = 0;
    private int minReplicaCount = 0;
    private int maxReplicaCount = 100;
}
