package app.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

import static app.crd.ScaledObject.GROUP;
import static app.crd.ScaledObject.VERSION;

@Group(GROUP)
@Version(VERSION)
public class ScaledObject extends CustomResource<ScaledObjectSpec, Void> implements Namespaced {
    public static final String GROUP = "oleksiyp.github.io";
    public static final String VERSION = "v1";
}
