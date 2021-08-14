package it.pagopa.pn.commons.mom;

import java.util.concurrent.CompletableFuture;

public interface MomProducer<T> {

    void push(T msg );

}
