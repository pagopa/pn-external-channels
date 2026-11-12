package it.pagopa.pn.externalchannels.exception;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;

public class KafkaUnavailableException extends PnRuntimeException {
    public KafkaUnavailableException() {
        this(null);
    }

    public KafkaUnavailableException(Throwable cause) {
        super("Service Unavailable", "Problem with Kafka connection", HttpStatus.SERVICE_UNAVAILABLE.value(), "KAFKA_UNAVAILABLE", null, null, cause);
    }
}
