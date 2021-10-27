package app;

import app.crd.ScaledObject;
import app.crd.ScaledObjectList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScaledObjectWatcher implements Watcher<ScaledObject> {
    private final KubernetesClient client;
    private MixedOperation<ScaledObject, ScaledObjectList, Resource<ScaledObject>> scaledResourcesOps;
    private final AtomicReference<Map<String, ScaledObject>> scaledObjects = new AtomicReference<>(new LinkedHashMap<>());
    private Watch watch;

    @PostConstruct
    public void start() {
        scaledResourcesOps = client.resources(ScaledObject.class, ScaledObjectList.class);
        watch = scaledResourcesOps.watch(this);
    }

    @PreDestroy
    public void stop() {
        watch.close();
    }

    public Collection<ScaledObject> scaledObjects() {
        return scaledObjects.get().values();
    }

    public void eventReceived(Action action, ScaledObject resource) {
        switch (action) {
            case ADDED:
            case MODIFIED:
                scaledObjects.updateAndGet(map -> {
                    map = new LinkedHashMap<>(map);
                    map.put(resource.getMetadata().getUid(), resource);
                    return map;
                });
                log.info("Watching {}/{}", resource.getMetadata().getNamespace(), resource.getMetadata().getName());
                break;
            case DELETED:
                scaledObjects.updateAndGet(map -> {
                    map = new LinkedHashMap<>(map);
                    map.remove(resource.getMetadata().getUid());
                    return map;
                });
                log.info("Stopped watching {}/{}", resource.getMetadata().getNamespace(), resource.getMetadata().getName());
                break;
            case ERROR:

                break;
        }
    }

    @Override
    public void onClose(WatcherException cause) {
        log.error("Watching failure", cause);
        System.exit(1);
    }
}
