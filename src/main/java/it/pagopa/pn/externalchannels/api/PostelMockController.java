package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.externalchannels.mock_postel.DeduplicaRequest;
import it.pagopa.pn.externalchannels.mock_postel.DeduplicaResponse;
import it.pagopa.pn.externalchannels.mock_postel.NormalizzazioneRequest;
import it.pagopa.pn.externalchannels.mock_postel.NormalizzazioneResponse;
import it.pagopa.pn.externalchannels.service.mockpostel.DeduplicaService;
import it.pagopa.pn.externalchannels.service.mockpostel.PostelService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@RestController
@lombok.CustomLog
public class PostelMockController implements DefaultApi {

    private final Scheduler scheduler;
    private final PostelService postelService;
    private final DeduplicaService deduplicaService;

    public PostelMockController(@Qualifier("externalChannelsScheduler") Scheduler scheduler,
                                PostelService postelService, DeduplicaService deduplicaService) {
        this.scheduler = scheduler;
        this.postelService = postelService;
        this.deduplicaService = deduplicaService;
    }

    /**
     * POST /postel-mock/normalizzazione : effettua una chiamata nei confronti di Postel condividendo il fileKey relativo al file caricato.
     *
     * @param normalizzazioneRequest (required)
     * @return Risposta di successo (status code 200)
     */
    @Override
    public Mono<ResponseEntity<NormalizzazioneResponse>> normalizzazione(String pnAddressManagerCxId, String xApiKey,
                                                                         Mono<NormalizzazioneRequest> normalizzazioneRequest, ServerWebExchange exchange) {
        return normalizzazioneRequest
                .flatMap(request -> {
                    log.info("requestId: {}", request.getRequestId());
                    return postelService.activateNormalizer(request);
                })
                .map(normalizzazioneResponse -> ResponseEntity.ok().body(normalizzazioneResponse))
                .publishOn(scheduler);
    }


    /**
     * POST /postel-mock/deduplica : Effettua la deduplicazione
     *
     * @param inputDeduplica (required)
     * @return Risposta di successo (status code 200)
     */
    @Override
    public Mono<ResponseEntity<DeduplicaResponse>> deduplica(String pnAddressManagerCxId, String xApiKey,
                                                             Mono<DeduplicaRequest> inputDeduplica, ServerWebExchange exchange) {
        return inputDeduplica
                .flatMap(deduplicaService::deduplica)
                .map(deduplicateResponse -> ResponseEntity.ok().body(deduplicateResponse))
                .publishOn(scheduler);
    }
}
