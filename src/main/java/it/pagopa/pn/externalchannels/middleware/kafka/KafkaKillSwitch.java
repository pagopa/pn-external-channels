package it.pagopa.pn.externalchannels.middleware.kafka;

import lombok.CustomLog;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@CustomLog
public class KafkaKillSwitch {
    private final AtomicBoolean enabled;
    private final ProducerFactory<?, ?> producerFactory;

    public KafkaKillSwitch(ProducerFactory<?, ?> producerFactory) {
        this.enabled = new AtomicBoolean(true); // Di default è attivo.
        this.producerFactory = producerFactory;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void enable() {
        if (!enabled.getAndSet(true)) {
            log.info("Kafka producer riabilitato");
        }
    }

    public void disable() {
        if (enabled.getAndSet(false)) {
            log.warn("Kafka producer disabilitato");
            if (producerFactory instanceof DefaultKafkaProducerFactory<?, ?> f) {
                f.reset(); // chiude il KafkaProducer e ferma i tentativi di riconnessione
            }
        }
    }
}
