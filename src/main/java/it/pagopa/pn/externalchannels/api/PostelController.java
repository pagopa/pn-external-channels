package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.externalchannels.mock_postel.InputDeduplica;
import it.pagopa.pn.externalchannels.mock_postel.RequestActivatePostel;
import it.pagopa.pn.externalchannels.mock_postel.ResponseActivatePostel;
import it.pagopa.pn.externalchannels.mock_postel.RisultatoDeduplica;
import it.pagopa.pn.externalchannels.service.mockpostel.PostelService;
import it.pagopa.pn.externalchannels.service.mockpostel.DeduplicaService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@RestController
@lombok.CustomLog
public class PostelController implements MockPostelApi {
	private final Scheduler scheduler;
	private final DeduplicaService deduplicaService;
	private final PostelService postelService;
	public PostelController(@Qualifier ("externalChannelsScheduler") Scheduler scheduler,
							DeduplicaService deduplicatesService,
							PostelService postelService){
		this.scheduler = scheduler;
		this.deduplicaService = deduplicatesService;
		this.postelService = postelService;
	}

	/**
	 * POST /mockPostel/checkUploadFile : effettua una chiamata nei confronti di Postel condividendo il fileKey relativo al file caricato.
	 *
	 * @param requestCheckUploadFile  (required)
	 * @return Risposta di successo (status code 200)
	 */
	@Override
	public Mono<ResponseEntity<ResponseActivatePostel>> activatePostel(Mono<RequestActivatePostel> requestCheckUploadFile, ServerWebExchange exchange){
		return requestCheckUploadFile
				.flatMap(inputDeduplica -> postelService.checkUploadFile(inputDeduplica)
						.map(deduplicateResponse -> ResponseEntity.ok().body(deduplicateResponse))
						.publishOn(scheduler));
	}


	/**
	 * POST /deduplica : Effettua la deduplicazione
	 *
	 * @param inputDeduplicates  (required)
	 * @return Risposta di successo (status code 200)
	 */
	@Override
	public Mono<ResponseEntity<RisultatoDeduplica>> deduplica(Mono<InputDeduplica> inputDeduplicates,ServerWebExchange exchange){
		return inputDeduplicates
				.flatMap(inputDeduplica -> deduplicaService.deduplica(inputDeduplica)
						.map(deduplicateResponse -> ResponseEntity.ok().body(deduplicateResponse))
						.publishOn(scheduler));
	}
}
