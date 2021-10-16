package app;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.DeclarableCustomizer;
import org.springframework.stereotype.Component;

@Component
public class KedaScaler implements DeclarableCustomizer {
    @Override
    public Declarable apply(Declarable declarable) {
        System.out.println(declarable);
        return declarable;
    }
}
