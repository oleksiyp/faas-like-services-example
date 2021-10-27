package app;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CooldownData {
    long timestamp;
    int newReplicaCount;
}
