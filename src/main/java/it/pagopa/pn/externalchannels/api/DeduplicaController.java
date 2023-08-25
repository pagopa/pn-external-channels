package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.externalchannels.mock_postel.InputDeduplica;
import it.pagopa.pn.externalchannels.mock_postel.RequestCheckUploadFile;
import it.pagopa.pn.externalchannels.mock_postel.ResponseCheckUploadFile;
import it.pagopa.pn.externalchannels.mock_postel.RisultatoDeduplica;
import it.pagopa.pn.externalchannels.service.mockpostel.AddressManagerService;
import it.pagopa.pn.externalchannels.service.mockpostel.DeduplicaService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@RestController
@lombok.CustomLog
public class DeduplicaController implements MockPostelApi {
	private final Scheduler scheduler;
	private final DeduplicaService deduplicaService;
	private final AddressManagerService addressManagerService;
	public DeduplicaController(@Qualifier ("externalChannelsScheduler") Scheduler scheduler,
							   DeduplicaService deduplicatesService,
							   AddressManagerService addressManagerService){
		this.scheduler = scheduler;
		this.deduplicaService = deduplicatesService;
		this.addressManagerService = addressManagerService;
	}

	/**
	 * POST /mockPostel/checkUploadFile : effettua una chiamata nei confronti di Postel condividendo il fileKey relativo al file caricato.
	 *
	 * @param requestCheckUploadFile  (required)
	 * @return Risposta di successo (status code 200)
	 */
	@Override
	public Mono<ResponseEntity<ResponseCheckUploadFile>> checkUploadFile(Mono<RequestCheckUploadFile> requestCheckUploadFile, ServerWebExchange exchange){
		return requestCheckUploadFile
				.flatMap(inputDeduplica -> addressManagerService.checkUploadFile(inputDeduplica)
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
