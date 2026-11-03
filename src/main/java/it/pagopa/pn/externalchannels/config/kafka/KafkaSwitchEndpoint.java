package it.pagopa.pn.externalchannels.config.kafka;

import it.pagopa.pn.externalchannels.middleware.kafka.KafkaKillSwitch;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Endpoint(id = "kafkaswitch")
public class KafkaSwitchEndpoint {
    private final KafkaKillSwitch killSwitch;

    public KafkaSwitchEndpoint(KafkaKillSwitch killSwitch) {
        this.killSwitch = killSwitch;
    }

    @ReadOperation
    public Map<String, Boolean> status() {
        return Map.of("enabled", killSwitch.isEnabled());
    }

    @WriteOperation
    public Map<String, Boolean> set(boolean enabled) {
        if (enabled) killSwitch.enable(); else killSwitch.disable();
        return status();
    }
}
