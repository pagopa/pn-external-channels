package it.pagopa.pn.externalchannels.service;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import it.pagopa.pn.externalchannels.dto.HistoricalRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class HistoricalRequestService {


    private final AsyncCache<String, HistoricalRequest> cacheHistoricalRequest;

    private final boolean cacheEnabled;

    public HistoricalRequestService(PnExternalChannelsProperties pnExternalChannelsProperties) {

        cacheHistoricalRequest = Caffeine.newBuilder()
                .expireAfterAccess(pnExternalChannelsProperties.getCacheExpireAfterDays(), TimeUnit.DAYS)
                .maximumSize(pnExternalChannelsProperties.getCacheMaxSize())
                .buildAsync();

        this.cacheEnabled = pnExternalChannelsProperties.getCacheExpireAfterDays() > 0;
    }

    public Flux<HistoricalRequest> findAll() {
        if (cacheEnabled) {
            return Flux.fromIterable(cacheHistoricalRequest.synchronous().asMap().values());
        }
        else {
            log.warn("Cache not enabled");
            return Flux.empty();
        }

    }

    public Mono<HistoricalRequest> findByIun(String iun) {
        if (cacheEnabled) {
            CompletableFuture<HistoricalRequest> valueInCache = cacheHistoricalRequest.getIfPresent(iun);
            if (valueInCache == null) {
                return Mono.empty();
            }
            return Mono.fromFuture(valueInCache);
        }
        else {
            log.warn("Cache not enabled");
            return Mono.empty();
        }
    }

    public void save(String iun, String requestId, String codeSent) {
        if(cacheEnabled) {
            CompletableFuture<HistoricalRequest> ifPresent = cacheHistoricalRequest.getIfPresent(iun);
            if(ifPresent != null) {
                CompletableFuture<HistoricalRequest> historicalRequestCompletableFuture = ifPresent.thenApply(request -> {
                    request.getCodesSent().add(codeSent);
                    request.setLastUpdateInCache(Instant.now());
                    return request;
                });

                cacheHistoricalRequest.put(iun, historicalRequestCompletableFuture);
            }
            else {
                cacheHistoricalRequest.put(iun, CompletableFuture.supplyAsync(() ->
                        new HistoricalRequest(iun, requestId, new ArrayList<>(List.of(codeSent)), Instant.now())));
            }
        }
        else {
            log.warn("Cache not enabled");
        }

    }
}
