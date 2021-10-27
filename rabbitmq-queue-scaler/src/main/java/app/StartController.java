package app;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/start/v1")
@RequiredArgsConstructor
public class StartController {
    private final KubernetesDeploymentScaler scaler;

    @GetMapping("await")
    public Mono<String> await(
            @RequestParam String namespace,
            @RequestParam String name
    ) {
        return Mono.fromFuture(scaler.futureFor(namespace, name));
    }
}
