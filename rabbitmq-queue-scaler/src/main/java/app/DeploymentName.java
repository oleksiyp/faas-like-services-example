package app;

import lombok.Value;

@Value
public class DeploymentName {
    String namespace;
    String name;
}
