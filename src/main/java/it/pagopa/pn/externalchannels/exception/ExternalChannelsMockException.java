package it.pagopa.pn.externalchannels.exception;

public class ExternalChannelsMockException extends RuntimeException {

    public ExternalChannelsMockException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ExternalChannelsMockException(String message) {
        super(message);
    }
}
