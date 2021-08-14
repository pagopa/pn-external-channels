package it.pagopa.pn.commons.mom;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface MomConsumer<T> {

    public void poll(Duration maxPollTime, Consumer<T> handler);

}
